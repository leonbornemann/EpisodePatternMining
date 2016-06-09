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
import reallife_data.finance.yahoo.stock.stream.StreamMonitor;
import semantic.SemanticKnowledgeCollector;

public class Main {

	private static final String APPLE = "AAPL"; //TODO: make these enums?

	public static void main(String[] args) throws IOException {
		//singleStream();
		multiStream();
	}

	private static void multiStream() throws IOException {
		File streamDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\");
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		System.out.println(annotatedCompanyCodes.size());
		int d = 240;
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(Arrays.stream(streamDir.listFiles()).sorted().collect(Collectors.toList()),d,e -> annotatedCompanyCodes.contains(e.getEventType().getCompanyID()));
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),AnnotatedEventType.loadEventAlphabet(annotatedCompanyCodes),100,15,10,d);
		Map<EpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		printTrustScores(predictors);
		StreamMonitor monitor = new StreamMonitor(predictors, stream, new AnnotatedEventType(APPLE, Change.UP), d);
		monitor.monitor();
		Map<EpisodePattern, Integer> trustScores = monitor.getCurrentTrustScores();
		printTrustScores(trustScores);
	}

	private static void printTrustScores(Map<EpisodePattern, Integer> trustScores) {
		trustScores.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
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
