package reallife_data.finance.yahoo.stock.stream;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;

public class StreamMonitorTest {
	
	//some event types for testing
	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	private static AnnotatedEventType E = new AnnotatedEventType("company", Change.EQUAL);
	private static AnnotatedEventType F = new AnnotatedEventType("names", Change.DOWN);
	//some predefined Episodes
	private static SerialEpisodePattern episode1 = new SerialEpisodePattern(A);
	private static SerialEpisodePattern episode2 = new SerialEpisodePattern(A,D);
	private static SerialEpisodePattern episode3 = new SerialEpisodePattern(A,B,C);
	
	@Test
	public void singlePredictor() throws IOException {
		Map<SerialEpisodePattern, Integer> predictors = new HashMap<>();
		predictors.put(episode3, 0);
		int d = 5;
		AnnotatedEventStream stream = buildStream(1,
				A,B,C,F,E,E,
				A,B,C,E,E,F,
				A,E,B,E,C,F);
		StreamMonitor monitor = new StreamMonitor(predictors,stream,F,d);
		monitor.monitor();
		predictors = monitor.getCurrentTrustScores();
		assertEquals(new Integer(3),predictors.get(episode3));
	}
	
	@Test
	public void multi() throws IOException {
		Map<SerialEpisodePattern, Integer> predictors = new HashMap<>();
		predictors.put(episode3, 0);
		predictors.put(episode2, 0);
		int d = 5;
		AnnotatedEventStream stream = buildStream(1,
				A,B,C,D,E,F, //both should fire
				A,D,E,E,E,F, //A->D should fire TODO: should they get penalized for not recognizing episodes?
				A,B,C,E,E,F); //A->B->C should fire
		StreamMonitor monitor = new StreamMonitor(predictors,stream,F,d);
		monitor.monitor();
		predictors = monitor.getCurrentTrustScores();
		assertEquals(new Integer(2),predictors.get(episode3));
		assertEquals(new Integer(2),predictors.get(episode2));
	}


	private AnnotatedEventStream buildStream(int stepLengthInSeconds, AnnotatedEventType ...eventTypes ) {
		LocalDateTime currentTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		List<AnnotatedEvent> events = new ArrayList<>();
		for(int i=0;i<eventTypes.length;i++){
			currentTime = currentTime.plus(1, ChronoUnit.SECONDS);
			events.add(new AnnotatedEvent(eventTypes[i].getCompanyID(),eventTypes[i].getChange(),currentTime));
		}
		return new InMemoryAnnotatedEventStream(events);
	}

}
