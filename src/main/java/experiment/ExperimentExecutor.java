package experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.CategoricalEventStream;
import data.stream.InMemoryMultiFileCategoricalEventStream;
import data.stream.StreamWindow;
import data.stream.StreamWindowSlider;
import episode.pattern.EpisodePattern;
import evaluation.CompanyBasedResultSerializer;
import evaluation.DayBasedResultSerializer;
import evaluation.EvaluationFiles;
import evaluation.EvaluationResult;
import evaluation.Evaluator;
import prediction.mining.WindowMiner;
import prediction.models.FBSWCModel;
import prediction.models.Method;
import prediction.models.PERMSTrainer;
import prediction.models.PERMSModel;
import prediction.models.PredictiveModel;
import prediction.models.RandomGuessingModel;
import prediction.models.SimpleMovingAverageForecastingModel;
import semantic.SemanticKnowledgeCollector;
import util.IOService;
import util.Pair;

/***
 * Main Class to execute a full program run for a specific configuration, meaning for both PERMS as well as FBSWC models:
 * 	Training
 * 	Evaluation
 * 	Serialization of Evaluation Results into specified files
 * @author Leon Bornemann
 *
 */
public class ExperimentExecutor {

	private File annotatedStreamDirDesktop;
	private File annotatedSectorStreamDirDesktop;
	
	private File timeSeriesDir;
	private File sectorTimeSeriesDir;
	
	private EvaluationConfig config;
	private Set<String> annotatedCompanyCodes;
	private File resultDir;
	private Set<String> toIgnore = new HashSet<>();
	
	public ExperimentExecutor(EvaluationConfig config,File resultDir, File categoricalStremDir, File categoricalSectorStreamDir, File timeSeriesDir, File sectorTimeSeriesDir) throws IOException, ClassNotFoundException{
		this.config = config;
		initAllCmpCodes();
		this.annotatedStreamDirDesktop = categoricalStremDir;
		this.annotatedSectorStreamDirDesktop = categoricalSectorStreamDir;
		this.timeSeriesDir = timeSeriesDir;
		this.sectorTimeSeriesDir = sectorTimeSeriesDir;
		this.resultDir = resultDir;
	}

	private void initAllCmpCodes() throws IOException {
		SemanticKnowledgeCollector collector = new SemanticKnowledgeCollector();
		annotatedCompanyCodes = collector.getAnnotatedCompanyCodes();
		if(config.isUseSemantics()){
			annotatedCompanyCodes.addAll(collector.getSectorCodes());
		}
	}

	public void execute() throws ClassNotFoundException, IOException{
		config.serialize(new File(resultDir.getAbsoluteFile()+File.separator+"config.prop"));
		execute(Method.FBSWC);
		initAllCmpCodes();
		execute(Method.PERMS);
	}

	public void execute(Method method) throws ClassNotFoundException, IOException {
		Map<String, Pair<Long, Long>> times = buildAndApplyModel(method);
//		Map<String, Pair<Long, Long>> times = new HashMap<String, Pair<Long, Long>>();
//		for(String id : annotatedCompanyCodes){
//			times.put(id, new Pair<>(-1L,-1L));
//		}
		annotatedCompanyCodes.removeAll(toIgnore);
		runEvaluation(method,times);
		printEvaluationResult(method);
		DayBasedResultSerializer dayBasedSerializer = new DayBasedResultSerializer();
		dayBasedSerializer.toCSV(annotatedCompanyCodes,method,resultDir);
		CompanyBasedResultSerializer serializer = new CompanyBasedResultSerializer();
		serializer.toCSV(annotatedCompanyCodes,method,resultDir);
	}
	
	public void printEvaluationResult(Method method) throws FileNotFoundException, IOException, ClassNotFoundException {
		BigDecimal avg = BigDecimal.ZERO;
		for (String id : annotatedCompanyCodes) {
			EvaluationResult a = EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method,resultDir));
			System.out.println("-----------------------------------------------------------------------------");
			System.out.println("results for " + id);
			System.out.println("total Return: " + a.getSummedReturn());
			avg = avg.add(a.getSummedReturn());
			System.out.println("Up-Precision: " + a.getTotalPerformance().getPrecision(Change.UP));
			System.out.println("DOWN-Precision: " + a.getTotalPerformance().getPrecision(Change.DOWN));
			System.out.println("Up-Precision without equal: " + a.getTotalPerformance().getEqualIgnoredPrecision(Change.UP));
			System.out.println("DOWN-Precision without equal: " + a.getTotalPerformance().getEqualIgnoredPrecision(Change.DOWN));
			System.out.println("Accuracy: " + a.getTotalPerformance().getAccuracy());
			System.out.println("Accuracy without equal: " + a.getTotalPerformance().getEqualIgnoredAccuracy());
			a.getTotalPerformance().printConfusionMatrix();
			System.out.println("-----------------------------------------------------------------------------");
		}
		System.out.println("avg Return: " + avg.divide(new BigDecimal(annotatedCompanyCodes.size()),100,RoundingMode.FLOOR));
	}

	private void runEvaluation(Method method, Map<String, Pair<Long, Long>> times) throws IOException {
		List<File> timeSeriesDirs = new ArrayList<>();
		timeSeriesDirs.add(timeSeriesDir);
		if(config.isUseSemantics()){
			timeSeriesDirs.add(sectorTimeSeriesDir);
		}
		Evaluator evaluator = new Evaluator(timeSeriesDirs);
		List<EvaluationFiles> evaluationFiles = annotatedCompanyCodes.stream().map(id -> new EvaluationFiles(id, IOService.buildPredictionsTargetFile(id,method,resultDir),IOService.buildTimeTargetFile(id,method,resultDir),IOService.getEvaluationResultFile(id,method,resultDir))).collect(Collectors.toList());
		evaluator.eval(evaluationFiles,times);
	}
	
	private Map<String, Pair<Long, Long>> buildAndApplyModel(Method method) throws IOException, ClassNotFoundException {
		Set<CategoricalEventType> eventAlphabet = CategoricalEventType.loadEventAlphabet(annotatedCompanyCodes);
		//List<String> done = Arrays.asList("AAPL","ALSK","CERN","CSCO","CSTE","EA","ELNK","EYES","GNCMA","NK","OHGI","ON","OPTT","SODA","SP");
		List<CategoricalEventType> toDo = eventAlphabet.stream().filter(e -> e.getChange()==Change.UP ).collect(Collectors.toList());
		Map<String,Pair<Long,Long>> times = new HashMap<>();
		for(int i=0;i<toDo.size();i++){
			CategoricalEventType toPredict = toDo.get(i);
			System.out.println("Iteration "+i + "out of "+toDo.size());
			System.out.println("beginning company " + toPredict.getCompanyID());
			//parameters:
			List<File> streamDirs = new ArrayList<>();
			streamDirs.add(annotatedStreamDirDesktop);
			if(config.isUseSemantics()){
				streamDirs.add(annotatedSectorStreamDirDesktop);
			}
			InMemoryMultiFileCategoricalEventStream stream = new InMemoryMultiFileCategoricalEventStream(streamDirs);
			System.out.println("beginning to mine windows");
			long beforeTrainingNs = getCpuTime();
			WindowMiner winMiner = new WindowMiner(stream,toPredict,config.getNumWindows(),config.getWindowSizeInSeconds());
			System.out.println("done mining windows");
			if(winMiner.getInversePredictiveWindows().size()!=config.getNumWindows() || winMiner.getNeutralWindows().size()!=config.getNumWindows() || winMiner.getPredictiveWindows().size()!= config.getNumWindows()){
				System.out.println("skipping Company");
				times.put(toPredict.getCompanyID(), new Pair<>(0L,0L));
				toIgnore.add(toPredict.getCompanyID());
			} else{
				//perms(d, s, eventAlphabet, stream,toPredict, winMiner);
				PredictiveModel model = null;
				if(method==Method.PERMS){
					model = perms(stream,toPredict, winMiner,eventAlphabet);
				} else if(method == Method.FBSWC){
					model = fbscw(stream,toPredict, winMiner,eventAlphabet);
				} else if(method==Method.RandomGuessing){
					assert(method == Method.RandomGuessing);
					model = new RandomGuessingModel(new Random(13));
				} else if(method == Method.SimpleAverageForecasting){
					model = new SimpleMovingAverageForecastingModel(toPredict,timeSeriesDir,config.getWindowSizeInSeconds());
				}
				long afterTrainingNs = getCpuTime();
				long trainingTimeNs = afterTrainingNs - beforeTrainingNs;
				long beforeTestTimeNs = getCpuTime();
				applyPredictor(toPredict, stream, model,method);
				long afterTestTimeNs = getCpuTime();
				long testTimeNs = afterTestTimeNs - beforeTestTimeNs;
				//TODO: use training and test time?
				serializeTimes(trainingTimeNs,testTimeNs,IOService.buildTimeTargetFile(toPredict.getCompanyID(),method,resultDir));
				times.put(toPredict.getCompanyID(), new Pair<>(trainingTimeNs,testTimeNs));
			}
		}
		return times;
	}
	
	private void serializeTimes(long trainingTimeNs, long testTimeNs, File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		pr.println("TrainingTimeNS,TestTimeNS");
		pr.println(trainingTimeNs + "," + testTimeNs);
		pr.close();
	}

	public static long getCpuTime() {
	    ThreadMXBean bean = ManagementFactory.getThreadMXBean( );
	    return bean.getCurrentThreadCpuTime();
	}
	
	private void applyPredictor(CategoricalEventType toPredict, CategoricalEventStream stream,PredictiveModel featureBasedPredictor, Method method) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,config.getWindowSizeInSeconds());
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		while(slider.canSlide()){
			StreamWindow currentWindow = slider.getCurrentWindow();
			Change predicted = featureBasedPredictor.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getSecond();
			predictions.add(new Pair<>(curTs,predicted));
			slider.slideForward();
		}
		//serialize results
		IOService.serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID(),method,resultDir));
	}
	
	private FBSWCModel fbscw(CategoricalEventStream stream,CategoricalEventType toPredict,WindowMiner winMiner,Set<CategoricalEventType> eventAlphabet) throws IOException, ClassNotFoundException {
		FBSWCModel featureBasedPredictor;
		File featurebasedPredictorFile = IOService.getFeatureBasedPredictorFile(toPredict.getCompanyID(),resultDir);
		featureBasedPredictor = new FBSWCModel(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, config.getSupportSerial(),config.getSupportParallel());
		featureBasedPredictor.saveModel(featurebasedPredictorFile);
		return featureBasedPredictor;
	}
	
	private PredictiveModel perms(CategoricalEventStream stream, CategoricalEventType toPredict, WindowMiner winMiner, Set<CategoricalEventType> eventAlphabet) throws IOException, ClassNotFoundException {
		File predictorsFile = IOService.buildPredictorsFilePath(toPredict.getCompanyID(),resultDir);
		File inversePredictorsFile = IOService.buildInversePredictorsFilePath(toPredict.getCompanyID(),resultDir);
		PERMSTrainer miner = new PERMSTrainer(winMiner,eventAlphabet,config.getSupportSerial(),config.getSupportParallel(),config.getPERMSNumPredictors());
		Map<EpisodePattern, Double> predictors;
		Map<EpisodePattern, Double> inversePredictors;
		predictors = miner.getInitialPreditiveEpisodes();
		IOService.serializeEpisodeMap(predictors,predictorsFile);
		inversePredictors = miner.getInitialInversePreditiveEpisodes();
		IOService.serializeEpisodeMap(inversePredictors,inversePredictorsFile);
		predictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + predictors.get(p) + " confidence" ));
		System.out.println("inverse");
		inversePredictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + inversePredictors.get(p) + " confidence" ));
		return new PERMSModel(predictors,inversePredictors);
	}
}
