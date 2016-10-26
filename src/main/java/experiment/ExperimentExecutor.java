package experiment;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
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
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	private EvaluationConfig config;
	private Set<String> annotatedCompanyCodes;
	private File resultDir;
	
	public ExperimentExecutor(EvaluationConfig config,File resultDir) throws IOException, ClassNotFoundException{
		this.config = config;
		annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		this.resultDir = resultDir;
		fsbwc();
		perms();
	}

	private void perms() throws ClassNotFoundException, IOException {
		buildAndApplyModel(Method.PERMS);
		runEvaluation(Method.PERMS);
	}

	private void fsbwc() throws ClassNotFoundException, IOException {
		buildAndApplyModel(Method.FBSWC);
		runEvaluation(Method.FBSWC);
	}
	
	private void buildAndApplyModel(Method method) throws IOException, ClassNotFoundException {
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		List<AnnotatedEventType> toDo = eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList());
		for(int i=0;i<toDo.size();i++){
			AnnotatedEventType toPredict = toDo.get(i);
			System.out.println("Iteration "+i + "out of "+toDo.size());
			System.out.println("beginning company " + toPredict.getCompanyID());
			//parameters:
			InMemoryMultiTimeSeriesAnnotatedEventStream stream = new InMemoryMultiTimeSeriesAnnotatedEventStream(annotatedStreamDirDesktop);
			System.out.println("beginning to mine windows");
			WindowMiner winMiner = new WindowMiner(stream,toPredict,config.getNumWindows(),config.getWindowSizeInSeconds());
			System.out.println("done mining windows");
			//perms(d, s, eventAlphabet, stream,toPredict, winMiner);
			PredictiveModel model = null;
			if(method==Method.PERMS){
				model = perms(stream,toPredict, winMiner,eventAlphabet);
			} else{
				model = fbscw(stream,toPredict, winMiner,eventAlphabet);
			}
			applyPredictor(toPredict, stream, model,method);
		}
	}
	
	private void applyPredictor(AnnotatedEventType toPredict, AnnotatedEventStream stream,PredictiveModel featureBasedPredictor, Method method) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,config.getWindowSizeInSeconds());
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		do{
			StreamWindow currentWindow = slider.getCurrentWindow();
			Change predicted = featureBasedPredictor.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getSecond();
			predictions.add(new Pair<>(curTs,predicted));
			slider.slideForward();
		} while(slider.canSlide());
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
