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
import java.util.Set;
import java.util.stream.Collectors;

import data.AnnotatedEventType;
import data.Change;
import data.stream.AnnotatedEventStream;
import data.stream.InMemoryMultiTimeSeriesAnnotatedEventStream;
import data.stream.StreamWindow;
import data.stream.StreamWindowSlider;
import episode.finance.EpisodePattern;
import prediction.evaluation.CompanyBasedResultSerializer;
import prediction.evaluation.DayBasedResultSerializer;
import prediction.evaluation.EvaluationFiles;
import prediction.evaluation.EvaluationResult;
import prediction.evaluation.NoAggregationEvaluator;
import prediction.mining.FBSWCModel;
import prediction.mining.Method;
import prediction.mining.PERSMModel;
import prediction.mining.PERMSTrainer;
import prediction.mining.PredictiveModel;
import prediction.mining.WindowMiner;
import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;
import semantic.SemanticKnowledgeCollector;
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

	private static File annotatedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series\\");
	private static File annotatedSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series\\");
	
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	private static File lowLevelSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\");
	
	private static File lowLevelSmoothedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series Smoothed\\");
	private static File lowLevelSmoothedSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series Smoothed\\");
	
	private EvaluationConfig config;
	private Set<String> annotatedCompanyCodes;
	private File resultDir;
	
	public ExperimentExecutor(EvaluationConfig config,File resultDir) throws IOException, ClassNotFoundException{
		this.config = config;
		SemanticKnowledgeCollector collector = new SemanticKnowledgeCollector();
		annotatedCompanyCodes = collector.getAnnotatedCompanyCodes();
		if(config.isUseSemantics()){
			annotatedCompanyCodes.addAll(collector.getSectorCodes());
		}
		this.resultDir = resultDir;
	}
	
	public void execute() throws ClassNotFoundException, IOException{
		config.serialize(new File(resultDir.getAbsoluteFile()+File.separator+"config.prop"));
		execute(Method.FBSWC);
		execute(Method.PERMS);
	}

	private void execute(Method method) throws ClassNotFoundException, IOException {
		Map<String, Pair<Long, Long>> times = buildAndApplyModel(method);
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
			System.out.println("Improved Performance Metric:");
			System.out.println("Up-Precision: " + a.getTotalImprovedPerformance().getPrecision(Change.UP));
			System.out.println("DOWN-Precision: " + a.getTotalImprovedPerformance().getPrecision(Change.DOWN));
			System.out.println("Up-Recall: " + a.getTotalImprovedPerformance().getRecall(Change.UP));
			System.out.println("DOWN-Recall: " + a.getTotalImprovedPerformance().getRecall(Change.DOWN));
			a.getTotalImprovedPerformance().printConfusionMatrix();
			System.out.println("-----------------------------------------------------------------------------");
		}
		System.out.println("avg Return: " + avg.divide(new BigDecimal(annotatedCompanyCodes.size()),100,RoundingMode.FLOOR));
	}

	private void runEvaluation(Method method, Map<String, Pair<Long, Long>> times) throws IOException {
		List<File> lowLevelStreamDirs = new ArrayList<>();
		lowLevelStreamDirs.add(lowLevelStreamDirDesktop);
		if(config.isUseSemantics()){
			lowLevelStreamDirs.add(lowLevelSectorStreamDirDesktop);
		}
		List<File> smoothedStreamDirs = new ArrayList<>();
		smoothedStreamDirs.add(lowLevelSmoothedStreamDirDesktop);
		if(config.isUseSemantics()){
			smoothedStreamDirs.add(lowLevelSmoothedSectorStreamDirDesktop);
		}
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(config.getWindowSizeInSeconds(),lowLevelStreamDirs,smoothedStreamDirs);
		List<EvaluationFiles> evaluationFiles = annotatedCompanyCodes.stream().map(id -> new EvaluationFiles(id, IOService.buildPredictionsTargetFile(id,method,resultDir),IOService.buildTimeTargetFile(id,method,resultDir),IOService.getEvaluationResultFile(id,method,resultDir))).collect(Collectors.toList());
		evaluator.eval(evaluationFiles,times);
	}
	
	private Map<String, Pair<Long, Long>> buildAndApplyModel(Method method) throws IOException, ClassNotFoundException {
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		List<AnnotatedEventType> toDo = eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList());
		Map<String,Pair<Long,Long>> times = new HashMap<>();
		for(int i=0;i<toDo.size();i++){
			AnnotatedEventType toPredict = toDo.get(i);
			System.out.println("Iteration "+i + "out of "+toDo.size());
			System.out.println("beginning company " + toPredict.getCompanyID());
			//parameters:
			List<File> streamDirs = new ArrayList<>();
			streamDirs.add(annotatedStreamDirDesktop);
			if(config.isUseSemantics()){
				streamDirs.add(annotatedSectorStreamDirDesktop);
			}
			InMemoryMultiTimeSeriesAnnotatedEventStream stream = new InMemoryMultiTimeSeriesAnnotatedEventStream(streamDirs);
			System.out.println("beginning to mine windows");
			long beforeTrainingNs = getCpuTime();
			WindowMiner winMiner = new WindowMiner(stream,toPredict,config.getNumWindows(),config.getWindowSizeInSeconds());
			System.out.println("done mining windows");
			//perms(d, s, eventAlphabet, stream,toPredict, winMiner);
			PredictiveModel model = null;
			if(method==Method.PERMS){
				model = perms(stream,toPredict, winMiner,eventAlphabet);
			} else{
				model = fbscw(stream,toPredict, winMiner,eventAlphabet);
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
	
	private void applyPredictor(AnnotatedEventType toPredict, AnnotatedEventStream stream,PredictiveModel featureBasedPredictor, Method method) throws IOException {
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
		serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID(),method,resultDir));
	}

	private void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}
	
	private FBSWCModel fbscw(AnnotatedEventStream stream,AnnotatedEventType toPredict,WindowMiner winMiner,Set<AnnotatedEventType> eventAlphabet) throws IOException, ClassNotFoundException {
		FBSWCModel featureBasedPredictor;
		File featurebasedPredictorFile = IOService.getFeatureBasedPredictorFile(toPredict.getCompanyID(),resultDir);
		featureBasedPredictor = new FBSWCModel(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, config.getSupportSerial(),config.getSupportParallel());
		featureBasedPredictor.saveModel(featurebasedPredictorFile);
		return featureBasedPredictor;
	}
	
	private PredictiveModel perms(AnnotatedEventStream stream, AnnotatedEventType toPredict, WindowMiner winMiner, Set<AnnotatedEventType> eventAlphabet) throws IOException, ClassNotFoundException {
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
		return new PERSMModel(predictors,inversePredictors);
	}
}
