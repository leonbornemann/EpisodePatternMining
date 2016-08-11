package prediction.data.stream.test;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import episode.finance.EpisodePattern;
import episode.finance.SerialEpisodePattern;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.AnnotatedEventStream;
import prediction.data.stream.PredictorPerformance;
import prediction.data.stream.StreamMonitor;

public class StreamMonitorTest {
	
	//some event types for testing
	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	private static AnnotatedEventType E = new AnnotatedEventType("company", Change.EQUAL);
	private static AnnotatedEventType F = new AnnotatedEventType("names", Change.DOWN);
	private static AnnotatedEventType G = new AnnotatedEventType("names", Change.UP);
	//some predefined Episodes
	private static SerialEpisodePattern episode1 = new SerialEpisodePattern(A);
	private static SerialEpisodePattern episode2 = new SerialEpisodePattern(A,D);
	private static SerialEpisodePattern episode3 = new SerialEpisodePattern(A,B,C);
	
	@Test
	public void singlePredictor() throws IOException {
		Map<EpisodePattern, Integer> predictors = new HashMap<>();
		predictors.put(episode3, 0);
		int d = 5;
		AnnotatedEventStream stream = TestUtil.buildStream(1,
				A,B,C,F,E,E, //true positive
				A,B,C,E,E,F, //true positive
				A,E,B,E,C,F, //true positive
				A,B,C,E,E,G, //false positive
				E,E,E,E,E,F, //false negative
				E,E,E,E,E,G  //true negative
				);
		StreamMonitor monitor = new StreamMonitor(predictors,new HashMap<>(),stream,F,d);
		monitor.monitor();
		PredictorPerformance result = monitor.getCurrentTrustScores().get(episode3);
		assertEquals(4.0/6.0,result.getAccuracy(),Double.MIN_VALUE);
		assertEquals(3.0/4.0,result.getPrecision(),Double.MIN_VALUE);
		assertEquals(3.0/4.0,result.getRecall(),Double.MIN_VALUE);
	}
	
	@Test
	public void multi() throws IOException {
		Map<EpisodePattern, Integer> predictors = new HashMap<>();
		predictors.put(episode3, 0);
		predictors.put(episode2, 0);
		int d = 5;
		AnnotatedEventStream stream = TestUtil.buildStream(1,
				A,B,C,D,E,F, //both should fire
				A,D,E,E,E,F, //A->D should fire
				A,B,C,E,E,F); //A->B->C should fire
		StreamMonitor monitor = new StreamMonitor(predictors,new HashMap<>(),stream,F,d);
		monitor.monitor();
		Map<EpisodePattern, PredictorPerformance> results = monitor.getCurrentTrustScores();
		assertEquals(2.0/3.0,results.get(episode3).getAccuracy(),Double.MIN_VALUE);
		assertEquals(2.0/3.0,results.get(episode2).getAccuracy(),Double.MIN_VALUE);
		assertEquals(2.0/3.0,results.get(episode3).getRecall(),Double.MIN_VALUE);
		assertEquals(2.0/3.0,results.get(episode2).getRecall(),Double.MIN_VALUE);
		assertEquals(2.0/2.0,results.get(episode3).getPrecision(),Double.MIN_VALUE);
		assertEquals(2.0/2.0,results.get(episode2).getPrecision(),Double.MIN_VALUE);
	}

}
