package reallife_data.finance.yahoo.stock.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public interface StreamWindow {

	List<AnnotatedEvent> getEvents();

	Map<LocalDateTime, List<AnnotatedEvent>> getEventTypesByTimestamp();

	Pair<LocalDateTime, LocalDateTime> getWindowBorders();

	boolean containsPattern(EpisodePattern pattern);

}