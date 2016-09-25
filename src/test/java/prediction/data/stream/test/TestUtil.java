package prediction.data.stream.test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.AnnotatedEvent;
import data.AnnotatedEventType;
import data.stream.AnnotatedEventStream;
import data.stream.InMemoryAnnotatedEventStream;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class TestUtil {

	public static AnnotatedEventStream buildStream(int stepLengthInSeconds, AnnotatedEventType ...eventTypes ) {
		LocalDateTime currentTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		List<AnnotatedEvent> events = new ArrayList<>();
		for(int i=0;i<eventTypes.length;i++){
			currentTime = currentTime.plus(1, ChronoUnit.SECONDS);
			events.add(new AnnotatedEvent(eventTypes[i].getCompanyID(),eventTypes[i].getChange(),currentTime));
		}
		return new InMemoryAnnotatedEventStream(events);
	}

	@SafeVarargs
	public static AnnotatedEventStream buildStream(Pair<AnnotatedEventType,Integer>... eventTimePairs) {
		LocalDateTime startTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		List<AnnotatedEvent> events = new ArrayList<>();
		for(int i=0;i<eventTimePairs.length;i++){
			LocalDateTime currentTime = startTime.plus(eventTimePairs[i].getSecond(), ChronoUnit.SECONDS);
			events.add(new AnnotatedEvent(eventTimePairs[i].getFirst().getCompanyID(),eventTimePairs[i].getFirst().getChange(),currentTime));
		}
		return new InMemoryAnnotatedEventStream(events);
	}

}
