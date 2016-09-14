package prediction.mining;

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
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
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

	private static final AnnotatedEventType APPLE = new AnnotatedEventType("AAPL", Change.UP);

	private static File streamDirLaptop = new File("C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\");
	
	private static File streamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\");
	
	private static File featurebasedPredictorFile = new File("resources/saved program states/featureBasedModel.object");

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		//singleStream();
		int d = 90;
		//new PredictorPerformance().printConfusionMatrix();
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
//		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);
//		for(AnnotatedEventType toPredict : eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList())){
//			multiStream(d,toPredict);
//		}
		//NoAggregationEvaluator evaluator = new NoAggregationEvaluator(d,lowLevelStreamDirDesktop);
		//List<EvaluationFiles> evaluationFiles = annotatedCompanyCodes.stream().map(id -> new EvaluationFiles(id, IOService.buildPredictionsTargetFile(id),IOService.getEvaluationResultFile(id))).collect(Collectors.toList());
		//evaluator.eval(evaluationFiles);
		BigDecimal avg = BigDecimal.ZERO;
		for (String id : annotatedCompanyCodes) {
			EvaluationResult a = EvaluationResult.deserialize(IOService.getEvaluationResultFile(id));
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
//		EvaluationResult result = EvaluationResult.deserialize(IOService.getEvaluationResultFile(APPLE.getCompanyID()));
//		//System.out.println(result.getTotalReturn());
//		System.out.println("Results for Method PERMS: ");
//		System.out.println();
//		System.out.println("Total Return of Investment [%]: " + result.getSummedReturn().multiply(new BigDecimal(100)));
//		System.out.println("Confusion Matrix:");
//		result.getTotalPerformance().printConfusionMatrix();
//		System.out.println();
//		System.out.println("Precision for Up Movement: " +result.getTotalPerformance().getPrecision(Change.UP));
//		System.out.println("Precision for Down Movement: " +result.getTotalPerformance().getPrecision(Change.DOWN));
//		System.out.println();
//		System.out.println("Precision values when ignoring Examples that were for Equal:");
//		System.out.println();
//		System.out.println("Precision for Up Movement: " +result.getTotalPerformance().getEqualIgnoredPrecision(Change.UP));
//		System.out.println("Precision for Down Movement: " +result.getTotalPerformance().getEqualIgnoredPrecision(Change.DOWN));
//		//System.out.println(result.getTotalPerformance().getNumClassifiedExamples());
	}

	private static void multiStream(int d,AnnotatedEventType toPredict) throws IOException, ClassNotFoundException {
		System.out.println("beginning company " + toPredict.getCompanyID());
		//parameters:
		int m = 100;
		int s=75;
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);

		//setup stream:
		System.out.println(annotatedCompanyCodes.size());
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDirDesktop.listFiles()).sorted().collect(Collectors.toList()),d*2,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		System.out.println("beginning to mine windows");
		WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
		System.out.println("done mining windows");
		printWindows(winMiner.getPredictiveWindows());
		printWindows(winMiner.getInversePredictiveWindows());
		
		//featureBasedPrediction(d, s, toPredict, eventAlphabet, stream,winMiner);
		firstNaiveStrategy(d, s, eventAlphabet, stream,toPredict, winMiner);
	}


	private static void printWindows(List<FixedStreamWindow> windows) {
		windows.stream().
			sorted((a,b) -> a.getWindowBorders().getFirst().compareTo(b.getWindowBorders().getFirst())).
			forEachOrdered(w -> System.out.println(w.getWindowBorders()));
	}

	private static void featureBasedPrediction(int d, int s, AnnotatedEventType toPredict,
			Set<AnnotatedEventType> eventAlphabet, MultiFileAnnotatedEventStream stream,WindowMiner winMiner) throws IOException, ClassNotFoundException {
		
		//find frequent Episodes
		
		//do first method predictive mining
		//feature based predictive mining:
		FeatureBasedPredictor featureBasedPredictor;
		if(featurebasedPredictorFile.exists()){
			featureBasedPredictor = new FeatureBasedPredictor(featurebasedPredictorFile);
		} else{
			featureBasedPredictor = new FeatureBasedPredictor(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNeutralWindows(), eventAlphabet, s);
			featureBasedPredictor.saveModel(featurebasedPredictorFile);
		}
		//window Sliding
		applyPredictor(d, toPredict, stream, featureBasedPredictor);
	}

	private static void applyPredictor(int d, AnnotatedEventType toPredict, MultiFileAnnotatedEventStream stream,
			PredictiveModel featureBasedPredictor) throws IOException {
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
		serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID()));
		serializePairList(targetMovement,IOService.buildTargetMovementFile(toPredict.getCompanyID()));
	}

	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	private static void firstNaiveStrategy(int d, int s, Set<AnnotatedEventType> eventAlphabet,
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

		PredictiveEpisodeModel model = new PredictiveEpisodeModel(predictors,inversePredictors);
		applyPredictor(d,toPredict,stream,model);
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
