package data.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import data.AnnotatedEvent;
import episode.finance.EpisodePattern;
import util.Pair;

public interface StreamWindow {

	List<AnnotatedEvent> getEvents();

	Map<LocalDateTime, List<AnnotatedEvent>> getEventTypesByTimestamp();

	Pair<LocalDateTime, LocalDateTime> getWindowBorders();

	boolean containsPattern(EpisodePattern pattern);

}