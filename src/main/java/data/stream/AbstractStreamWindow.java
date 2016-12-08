package data.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.events.CategoricalEvent;
import episode.pattern.EpisodePattern;
import episode.pattern.recognition.SimpleEpisodeRecognitionDFA;

public abstract class AbstractStreamWindow implements StreamWindow {

	protected List<CategoricalEvent> window;
	
	@Override
	public List<CategoricalEvent> getEvents() {
		return window;
	}

	@Override
	public Map<LocalDateTime, List<CategoricalEvent>> getEventTypesByTimestamp() {
		return window.stream().collect(Collectors.groupingBy(CategoricalEvent::getTimestamp));
	}

	@Override
	public boolean containsPattern(EpisodePattern pattern) {
		SimpleEpisodeRecognitionDFA dfa = pattern.getSimpleRecognitionDFA();
		for(CategoricalEvent event : window){
			dfa.processEvent(event.getEventType());
			if(dfa.isDone()){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public String toString(){
		return window.toString();
	}

}
