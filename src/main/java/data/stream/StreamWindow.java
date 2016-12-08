package data.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import data.events.CategoricalEvent;
import episode.pattern.EpisodePattern;
import util.Pair;

/***
 * Basic Interface for a window of a categorical event stream
 * @author Leon Bornemann
 *
 */
public interface StreamWindow {

	List<CategoricalEvent> getEvents();

	Map<LocalDateTime, List<CategoricalEvent>> getEventTypesByTimestamp();

	Pair<LocalDateTime, LocalDateTime> getWindowBorders();

	boolean containsPattern(EpisodePattern pattern);

}