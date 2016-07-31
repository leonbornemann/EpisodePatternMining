package reallife_data.finance.yahoo.stock.mining;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.MultiFileAnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.PredictorPerformance;
import reallife_data.finance.yahoo.stock.stream.StreamMonitor;
import semantic.SemanticKnowledgeCollector;

public class Main {

	private static final String APPLE = "AAPL"; //TODO: make these enums?

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
		File streamDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
		System.out.println(annotatedCompanyCodes.size());
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDir.listFiles()).sorted().collect(Collectors.toList()),d,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		//mine relevant windows:
		WindowMiner winMiner = new WindowMiner(stream,toPredict,m,d);
		
		//find frequent Episodes
		
		//do first method predictive mining
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),eventAlphabet,100,s,20,d);
		Map<EpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		Map<EpisodePattern, Integer> inversePredictors = miner.getInitialInversePreditiveEpisodes();
		//feature based predictive mining:
		FeatureBasedMiner featureBasedMiner = new FeatureBasedMiner(winMiner.getPredictiveWindows(), winMiner.getInversePredictiveWindows(), winMiner.getNothingWindows(), eventAlphabet, s);
		//monitoring
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
