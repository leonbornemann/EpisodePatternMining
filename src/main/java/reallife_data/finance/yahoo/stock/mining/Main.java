package reallife_data.finance.yahoo.stock.mining;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.stream.MultiFileAnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.PredictorPerformance;
import reallife_data.finance.yahoo.stock.stream.StreamMonitor;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;
import reallife_data.finance.yahoo.stock.stream.StreamWindowSlider;
import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;
import semantic.SemanticKnowledgeCollector;
import util.Pair;

public class Main {

	private static final String APPLE = "AAPL"; //TODO: make these enums?
	private static File predictionsTargetFile = new File("resources/results/FeatureBased/predictions.csv");
	private static File targetMovementTargetFile = new File("resources/results/FeatureBased/targetMovement.csv");
	
	private static File streamDirLaptop = new File("C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\");
	
	private static File streamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");

	public static void main(String[] args) throws IOException {
		//singleStream();
		multiStream();
	}

	private static void multiStream() throws IOException {
		//parameters:
		int d = 180;
		int m = 100;
		int s=15;
		AnnotatedEventType toPredict = new AnnotatedEventType(APPLE, Change.UP);
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Set<AnnotatedEventType> eventAlphabet = AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes);

		//setup stream:
		System.out.println(annotatedCompanyCodes.size());
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDirLaptop.listFiles()).sorted().collect(Collectors.toList()),d,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		featureBasedPrediction(d, m, s, toPredict, eventAlphabet, stream);
		
		//firstNaiveStrategy(d, s, eventAlphabet, stream);
	}

	private static void featureBasedPrediction(int d, int m, int s, AnnotatedEventType toPredict,
			Set<AnnotatedEventType> eventAlphabet, MultiFileAnnotatedEventStream stream) throws IOException {
		//mine relevant windows:
		WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
		
		//find frequent Episodes
		
		//do first method predictive mining
		//feature based predictive mining:
		FeatureBasedPredictor featureBasedPredictor = new FeatureBasedPredictor(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNothingWindows(), eventAlphabet, s);
		//window Sliding
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
		serializePairList(predictions,predictionsTargetFile);
		serializePairList(targetMovement,targetMovementTargetFile);
	}

	private static void serializePairList(List<Pair<LocalDateTime, Change>> predictions,File file) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(file));
		for (Pair<LocalDateTime, Change> pair : predictions) {
			pr.println(pair.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter())+ "," + pair.getSecond());
		}
		pr.close();
	}

	private static void firstNaiveStrategy(int d, int s, Set<AnnotatedEventType> eventAlphabet,
			MultiFileAnnotatedEventStream stream) throws IOException {
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),eventAlphabet,100,s,20,d);
		Map<EpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		Map<EpisodePattern, Integer> inversePredictors = miner.getInitialInversePreditiveEpisodes();
		StreamMonitor monitor = new StreamMonitor(predictors,inversePredictors, stream, new AnnotatedEventType(APPLE, Change.UP), d,new File("resources/logs/performanceLog.txt"));
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());
		monitor.monitor();
		Map<EpisodePattern, PredictorPerformance> trustScores = monitor.getCurrentTrustScores();
		printTrustScores(trustScores);
		System.out.println(monitor.getInvestmentTracker().netWorth());
		System.out.println(monitor.getInvestmentTracker().getPrice());
	}

	private static void printTrustScores(Map<EpisodePattern, PredictorPerformance> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Precision: "+v.getPrecision() + " and Recall: " + v.getRecall() + " and accuracy: " + v.getAccuracy()));
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
