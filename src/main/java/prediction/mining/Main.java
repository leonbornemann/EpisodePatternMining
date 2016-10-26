package prediction.mining;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import data.AnnotatedEvent;
import data.AnnotatedEventType;
import data.Change;
import data.stream.AnnotatedEventStream;
import data.stream.FixedStreamWindow;
import data.stream.InMemoryAnnotatedEventStream;
import data.stream.InMemoryMultiTimeSeriesAnnotatedEventStream;
import data.stream.MultiFileAnnotatedEventStream;
import data.stream.PredictorPerformance;
import data.stream.StreamWindow;
import data.stream.StreamWindowSlider;
import episode.finance.EpisodePattern;
import prediction.evaluation.CompanyBasedResultSerializer;
import prediction.evaluation.DayBasedResultSerializer;
import prediction.evaluation.EvaluationFiles;
import prediction.evaluation.EvaluationResult;
import prediction.evaluation.NoAggregationEvaluator;
import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;
import semantic.SemanticKnowledgeCollector;
import util.Pair;

public class Main {


	private static File streamDirLaptop = new File("C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\");
	
	private static File streamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
	private static File unchangedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\");
	
	private static File annotatedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series\\");
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Method method = Method.FBSWC;
		int d = 90;
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		//HashSet<String> toEvaluate = new HashSet<>(Arrays.asList("AAPL"));
		buildAndApplyModel(method, d, annotatedCompanyCodes,null);

		runEvaluation(d, annotatedCompanyCodes,method);
		printEvaluationResult(annotatedCompanyCodes,method);
				
//		DayBasedResultSerializer dayBasedSerializer = new DayBasedResultSerializer();
//		dayBasedSerializer.toCSV(annotatedCompanyCodes,method);
//		
//		CompanyBasedResultSerializer serializer = new CompanyBasedResultSerializer();
//		serializer.toCSV(annotatedCompanyCodes,method);
//		
//		method = Method.FBSWC;
//		
//		runEvaluation(d, annotatedCompanyCodes,method);
//		printEvaluationResult(annotatedCompanyCodes,method);
//				
//		dayBasedSerializer.toCSV(annotatedCompanyCodes,method);
//		
//		serializer.toCSV(annotatedCompanyCodes,method);
	}

	private static void buildAndApplyModel(Method method, int d, Set<String> annotatedCompanyCodes,HashSet<String> codesToEvaluate)
			throws IOException, ClassNotFoundException {
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		List<AnnotatedEventType> toDo = eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList());
		for(int i=0;i<toDo.size();i++){
			AnnotatedEventType toPredict = toDo.get(i);
			if(codesToEvaluate == null || codesToEvaluate.contains(toPredict.getCompanyID())){
				System.out.println("Iteration "+i + "out of "+toDo.size());
				System.out.println("beginning company " + toPredict.getCompanyID());
				//parameters:
				int m = 100;
				int s=75;
				InMemoryMultiTimeSeriesAnnotatedEventStream stream = new InMemoryMultiTimeSeriesAnnotatedEventStream(annotatedStreamDirDesktop);
				System.out.println("beginning to mine windows");
				WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
				System.out.println("done mining windows");
				//perms(d, s, eventAlphabet, stream,toPredict, winMiner);
				PredictiveModel model = null;
				if(method==Method.PERMS){
					model = perms(d, s, eventAlphabet, stream,toPredict, winMiner);
				} else{
					model = featureBasedPrediction(d, s, toPredict, eventAlphabet, stream, winMiner);
				}
				applyPredictor(d, toPredict, stream, model,method);
			}
		}
	}

	private static void printEvaluationResult(Set<String> annotatedCompanyCodes, Method method) throws FileNotFoundException, IOException, ClassNotFoundException {
		BigDecimal avg = BigDecimal.ZERO;
		for (String id : annotatedCompanyCodes) {
			EvaluationResult a = EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method));
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
			System.out.println("Up-Precision without equal: " + a.getTotalImprovedPerformance().getEqualIgnoredPrecision(Change.UP));
			System.out.println("DOWN-Precision without equal: " + a.getTotalImprovedPerformance().getEqualIgnoredPrecision(Change.DOWN));
			System.out.println("Accuracy: " + a.getTotalImprovedPerformance().getAccuracy());
			System.out.println("Accuracy without equal: " + a.getTotalImprovedPerformance().getEqualIgnoredAccuracy());
			a.getTotalImprovedPerformance().printConfusionMatrix();
			System.out.println("-----------------------------------------------------------------------------");
		}
		System.out.println("avg Return: " + avg.divide(new BigDecimal(annotatedCompanyCodes.size()),100,RoundingMode.FLOOR));
	}

	private static void runEvaluation(int d, Set<String> annotatedCompanyCodes, Method method) throws IOException {
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(d,lowLevelStreamDirDesktop);
		List<EvaluationFiles> evaluationFiles = annotatedCompanyCodes.stream().map(id -> new EvaluationFiles(id, IOService.buildPredictionsTargetFile(id,method),IOService.getEvaluationResultFile(id,method))).collect(Collectors.toList());
		evaluator.eval(evaluationFiles);
	}

	private static void printWindows(List<FixedStreamWindow> windows) {
		windows.stream().
			sorted((a,b) -> a.getWindowBorders().getFirst().compareTo(b.getWindowBorders().getFirst())).
			forEachOrdered(w -> System.out.println(w.getWindowBorders()));
	}

	private static FBSWCModel featureBasedPrediction(int d, int s, AnnotatedEventType toPredict,
			Set<AnnotatedEventType> eventAlphabet, AnnotatedEventStream stream,WindowMiner winMiner) throws IOException, ClassNotFoundException {
		
		//find frequent Episodes
		
		//do first method predictive mining
		//feature based predictive mining:
		FBSWCModel featureBasedPredictor;
		File featurebasedPredictorFile = IOService.getFeatureBasedPredictorFile(toPredict.getCompanyID());
//		if(featurebasedPredictorFile.exists()){
//			featureBasedPredictor = new FeatureBasedPredictor(featurebasedPredictorFile);
//		} else{
			featureBasedPredictor = new FBSWCModel(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, s);
			featureBasedPredictor.saveModel(featurebasedPredictorFile);
//		}
		//window Sliding
		return featureBasedPredictor;
	}

	private static void applyPredictor(int d, AnnotatedEventType toPredict, AnnotatedEventStream stream,
			PredictiveModel featureBasedPredictor, Method method) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,d);
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		do{
			StreamWindow currentWindow = slider.getCurrentWindow();
			Change predicted = featureBasedPredictor.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getSecond();
			predictions.add(new Pair<>(curTs,predicted));
			slider.slideForward();
		} while(slider.canSlide());
		//serialize results
		serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID(),method));
	}

	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	private static PredictiveModel perms(int d, int s, Set<AnnotatedEventType> eventAlphabet,
			AnnotatedEventStream stream, AnnotatedEventType toPredict, WindowMiner winMiner) throws IOException, ClassNotFoundException {
		int n=20;
		File predictorsFile = IOService.buildPredictorsFilePath(toPredict.getCompanyID());
		File inversePredictorsFile = IOService.buildInversePredictorsFilePath(toPredict.getCompanyID());
		PERMSTrainer miner = new PERMSTrainer(winMiner,eventAlphabet,s,n);
		Map<EpisodePattern, Double> predictors;
		Map<EpisodePattern, Double> inversePredictors;
//		if(predictorsFile.exists()){
//			predictors = IOService.loadEpisodeMap(predictorsFile);
//		} else{
			predictors = miner.getInitialPreditiveEpisodes();
			IOService.serializeEpisodeMap(predictors,predictorsFile);
//		}
//		if(inversePredictorsFile.exists()){
//			inversePredictors = IOService.loadEpisodeMap(inversePredictorsFile);
//		} else{
			inversePredictors = miner.getInitialInversePreditiveEpisodes();
			IOService.serializeEpisodeMap(inversePredictors,inversePredictorsFile);
//		}
		predictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + predictors.get(p) + " confidence" ));
		System.out.println("inverse");
		inversePredictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + inversePredictors.get(p) + " confidence" ));

		return new PERSMModel(predictors,inversePredictors);
	}

	/*private static void singleStream() throws IOException {
		File testDay = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\NASDAQ_2016-05-09_annotated.csv");
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		AnnotatedEventStream stream = AnnotatedEventStream.read(testDay).filter(e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes),100,15,10,50);
		Map<SerialEpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		predictors.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}*/

}
