package reallife_data.finance.yahoo.stock.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import episode.finance.SimpleEpisodeRecognitionDFA;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public class StreamWindow {

	List<AnnotatedEvent> window;

	public StreamWindow(List<AnnotatedEvent> window) {
		this.window = window;
	}

	public List<AnnotatedEvent> getEvents() {
		return window;
	}

	public Map<LocalDateTime, List<AnnotatedEvent>> getEventTypesByTimestamp() {
		return window.stream().collect(Collectors.groupingBy(AnnotatedEvent::getTimestamp));
		
	}

	public Pair<LocalDateTime,LocalDateTime> getWindowBorders() {
		return new Pair<>(window.get(0).getTimestamp(),window.get(window.size()-1).getTimestamp());
	}

	public boolean containsPattern(EpisodePattern pattern) {
		SimpleEpisodeRecognitionDFA dfa = pattern.getSimpleRecognitionDFA();
		for(int i=0;i<window.size();i++){
			dfa.processEvent(window.get(i).getEventType());
			if(dfa.isDone()){
				return true;
			}
		}
		return false;
	}
	
}
