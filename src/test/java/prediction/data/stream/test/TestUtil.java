package prediction.data.stream.test;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.stream.CategoricalEventStream;
import data.stream.InMemoryCategoricalEventStream;
import util.Pair;
import util.StandardDateTimeFormatter;

public class TestUtil {

	public static CategoricalEventStream buildStream(int stepLengthInSeconds, CategoricalEventType ...eventTypes ) {
		LocalDateTime currentTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		List<CategoricalEvent> events = new ArrayList<>();
		for(int i=0;i<eventTypes.length;i++){
			currentTime = currentTime.plus(1, ChronoUnit.SECONDS);
			events.add(new CategoricalEvent(eventTypes[i].getCompanyID(),eventTypes[i].getChange(),currentTime));
		}
		return new InMemoryCategoricalEventStream(events);
	}

	@SafeVarargs
	public static CategoricalEventStream buildStream(Pair<CategoricalEventType,Integer>... eventTimePairs) {
		LocalDateTime startTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		List<CategoricalEvent> events = new ArrayList<>();
		for(int i=0;i<eventTimePairs.length;i++){
			LocalDateTime currentTime = startTime.plus(eventTimePairs[i].getSecond(), ChronoUnit.SECONDS);
			events.add(new CategoricalEvent(eventTimePairs[i].getFirst().getCompanyID(),eventTimePairs[i].getFirst().getChange(),currentTime));
		}
		return new InMemoryCategoricalEventStream(events);
	}

}
