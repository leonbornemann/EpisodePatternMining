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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.FixedStreamWindow;
import prediction.data.stream.MultiFileAnnotatedEventStream;
import prediction.data.stream.StreamWindow;
import prediction.data.stream.StreamWindowSlider;
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
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\");
	
	public static void main(String[] args) throws IOException, ClassNotFoundException {
		Method method = Method.FBSWC;
		int d = 90;
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		//buildAndApplyModel(method, d, annotatedCompanyCodes);
		//runEvaluation(d, annotatedCompanyCodes,method);
		//printEvaluationResult(annotatedCompanyCodes,method);
		evaluationResultToCSV(annotatedCompanyCodes,method);
	}

	private static void evaluationResultToCSV(Set<String> annotatedCompanyCodes, Method method) throws FileNotFoundException, ClassNotFoundException, IOException {
		Map<String,EvaluationResult> results = new HashMap<>();
		for (String id : annotatedCompanyCodes) {
			results.put(id,EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method)));
		}
		Set<LocalDate> allDays = results.values().stream().flatMap(r -> r.getAllDays().stream()).collect(Collectors.toSet());
		for(String id: annotatedCompanyCodes){
			File csvResultFile = IOService.getCSVResultFile(id,method);
			PrintWriter writer = new PrintWriter(new FileWriter(csvResultFile));
			List<LocalDate> orderedDates = allDays.stream().sorted().collect(Collectors.toList());
			writer.println(buildHeadLine());
			for(LocalDate date : orderedDates){
				writer.println(buildResultString(date,results.get(date)));
			}
			writer.close();
		}
	}

	private static String buildResultString(LocalDate date,EvaluationResult evaluationResult) {
		int roundTo = 5;
		String dateString = date.format(StandardDateTimeFormatter.getStandardDateFormatter());
		return dateString+ "," +
				getAsRoundedString(evaluationResult.getSummedReturn(),roundTo) + "," +
				getAsRoundedString(evaluationResult.getTotalPerformance().getPrecision(Change.UP),roundTo) + "," +
				getAsRoundedString(evaluationResult.getTotalPerformance().getPrecision(Change.DOWN),roundTo) + "," + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getRecall(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getEqualIgnoredPrecision(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getEqualIgnoredPrecision(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getEqualIgnoredRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getTotalPerformance().getEqualIgnoredRecall(Change.DOWN),roundTo);
	}

	private static String getAsRoundedString(double val, int roundTo) {
		return getAsRoundedString(new BigDecimal(val), roundTo);
	}

	private static String getAsRoundedString(BigDecimal summedReturn, int roundTo) {
		return summedReturn.setScale(roundTo, RoundingMode.FLOOR).toString();
	}

	private static String buildHeadLine() {
		String result = "date,"+
				"precision_" + Change.UP + "," +
				"precision_" + Change.DOWN + "," +
				"recall_" + Change.UP + "," +
				"recall_" + Change.DOWN + "," +
				"precisionIgnoreEqual_" + Change.UP + "," +
				"precisionIgnoreEqual_" + Change.DOWN + "," +
				"recallIgnoreEqual_" + Change.UP + "," +
				"recallIgnoreEqual_" + Change.DOWN;
		System.out.println(result);
		return result;
	}

	private static void buildAndApplyModel(Method method, int d, Set<String> annotatedCompanyCodes)
			throws IOException, ClassNotFoundException {
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
		for(AnnotatedEventType toPredict : eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList())){
			System.out.println("beginning company " + toPredict.getCompanyID());
			//parameters:
			int m = 100;
			int s=75;
			MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDirDesktop.listFiles()).sorted().collect(Collectors.toList()),d*2,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
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
			a.getTotalPerformance().printConfusionMatrix();
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

	private static FeatureBasedPredictor featureBasedPrediction(int d, int s, AnnotatedEventType toPredict,
			Set<AnnotatedEventType> eventAlphabet, MultiFileAnnotatedEventStream stream,WindowMiner winMiner) throws IOException, ClassNotFoundException {
		
		//find frequent Episodes
		
		//do first method predictive mining
		//feature based predictive mining:
		FeatureBasedPredictor featureBasedPredictor;
		File featurebasedPredictorFile = IOService.getFeatureBasedPredictorFile(toPredict.getCompanyID());
		if(featurebasedPredictorFile.exists()){
			featureBasedPredictor = new FeatureBasedPredictor(featurebasedPredictorFile);
		} else{
			featureBasedPredictor = new FeatureBasedPredictor(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, s);
			featureBasedPredictor.saveModel(featurebasedPredictorFile);
		}
		//window Sliding
		return featureBasedPredictor;
	}

	private static void applyPredictor(int d, AnnotatedEventType toPredict, MultiFileAnnotatedEventStream stream,
			PredictiveModel featureBasedPredictor, Method method) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,d);
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		List<Pair<LocalDateTime,Change>> targetMovement = new ArrayList<>();
		do{
			StreamWindow currentWindow = slider.getCurrentWindow();
			Change predicted = featureBasedPredictor.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getFirst();
			predictions.add(new Pair<>(curTs,predicted));
			List<AnnotatedEvent> droppedOut = slider.slideForward();
			droppedOut.stream().filter(e -> e.getEventType().getCompanyID().equals(toPredict.getCompanyID())).forEach(e -> targetMovement.add(new Pair<>(e.getTimestamp(),e.getEventType().getChange())));
		} while(slider.canSlide());
		//serialize results
		serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID(),method));
		serializePairList(targetMovement,IOService.buildTargetMovementFile(toPredict.getCompanyID(),method));
	}

	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	private static PredictiveModel perms(int d, int s, Set<AnnotatedEventType> eventAlphabet,
			MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, WindowMiner winMiner) throws IOException, ClassNotFoundException {
		int n=20;
		File predictorsFile = IOService.buildPredictorsFilePath(toPredict.getCompanyID());
		File inversePredictorsFile = IOService.buildInversePredictorsFilePath(toPredict.getCompanyID());
		PredictiveMiner miner = new PredictiveMiner(winMiner,eventAlphabet,s,n);
		Map<EpisodePattern, Double> predictors;
		Map<EpisodePattern, Double> inversePredictors;
		if(predictorsFile.exists()){
			predictors = IOService.loadEpisodeMap(predictorsFile);
		} else{
			predictors = miner.getInitialPreditiveEpisodes();
			IOService.serializeEpisodeMap(predictors,predictorsFile);
		}
		if(inversePredictorsFile.exists()){
			inversePredictors = IOService.loadEpisodeMap(inversePredictorsFile);
		} else{
			inversePredictors = miner.getInitialInversePreditiveEpisodes();
			IOService.serializeEpisodeMap(inversePredictors,inversePredictorsFile);
		}
		predictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + predictors.get(p) + " confidence" ));
		System.out.println("inverse");
		inversePredictors.keySet().stream().forEach(p -> System.out.println("found predictor" + p + "with " + inversePredictors.get(p) + " confidence" ));

		return new PredictiveEpisodeModel(predictors,inversePredictors);
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
