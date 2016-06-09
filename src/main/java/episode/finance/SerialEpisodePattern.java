package episode.finance;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class SerialEpisodePattern implements EpisodePattern {
	
	private List<AnnotatedEventType> events;
	
	public SerialEpisodePattern(AnnotatedEventType... events) {
		this.events =Arrays.asList(events);
	}
	
	public SerialEpisodePattern(List<AnnotatedEventType> events) {
		this.events =events;
	}

	@Override
	public int length() {
		return events.size();
	}

	public AnnotatedEventType get(int i) {
		return events.get(i);
	}

	public List<AnnotatedEventType> subList(int i, int j) {
		return events.subList(i, j);
	}

	public void addEventType(AnnotatedEventType annotatedEventType) {
		events.add(annotatedEventType);
	}

	public SimpleSerialEpisodeRecognitionDFA getSimpleDFA() {
		return new SimpleSerialEpisodeRecognitionDFA(this);
	}
	
	public ContinousSerialEpisodeRecognitionDFA getContinousDFA(){
		return new ContinousSerialEpisodeRecognitionDFA(this);
	}
	
	@Override
	public String toString(){
		return events.stream().map(Object::toString).reduce((a,b) -> a + " -> " + b).get();
	}

	@Override
	public SimpleEpisodeRecognitionDFA getSimpleRecognitionDFA() {
		return new SimpleSerialEpisodeRecognitionDFA(this);
	}

}
