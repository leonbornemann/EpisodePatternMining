package reallife_data.finance.yahoo.stock.mining;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;

public class Main {

	private static final String APPLE = "AAPL"; //TODO: make these enums?

	public static void main(String[] args) throws IOException {
		File testDay = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\NASDAQ_Stream_annotated.csv");
		AnnotatedEventStream stream = AnnotatedEventStream.read(testDay);
		PredictiveMiner miner = new PredictiveMiner(stream,new AnnotatedEventType(APPLE, Change.UP),AnnotatedEventType.loadEventAlphabet(),100,15,10,120);
		Map<SerialEpisodePattern, Integer> predictors = miner.getInitialPreditiveEpisodes();
		predictors.forEach( (k,v) -> System.out.println("found predictor " +k+" with Trust score: "+v));
	}

}
