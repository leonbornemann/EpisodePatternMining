package reallife_data.finance.yahoo.stock.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import episode.finance.SimpleEpisodeRecognitionDFA;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public abstract class AbstractStreamWindow implements StreamWindow {

	protected List<AnnotatedEvent> window;
	
	@Override
	public List<AnnotatedEvent> getEvents() {
		return window;
	}

	@Override
	public Map<LocalDateTime, List<AnnotatedEvent>> getEventTypesByTimestamp() {
		return window.stream().collect(Collectors.groupingBy(AnnotatedEvent::getTimestamp));
	}

	@Override
	public boolean containsPattern(EpisodePattern pattern) {
		SimpleEpisodeRecognitionDFA dfa = pattern.getSimpleRecognitionDFA();
		for(AnnotatedEvent event : window){
			dfa.processEvent(event.getEventType());
			if(dfa.isDone()){
				return true;
			}
		}
		return false;
	}

}
