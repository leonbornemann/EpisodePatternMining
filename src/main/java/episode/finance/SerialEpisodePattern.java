package episode.finance;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import episode.finance.recognition.ContinousSerialEpisodeRecognitionDFA;
import episode.finance.recognition.SimpleEpisodeRecognitionDFA;
import episode.finance.recognition.SimpleSerialEpisodeRecognitionDFA;
import prediction.data.AnnotatedEventType;

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

	@Override
	public Set<AnnotatedEventType> getAllContainedTypes() {
		return new HashSet<>(events);
	}

	@Override
	public boolean containsType(AnnotatedEventType e) {
		return events.contains(e);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof SerialEpisodePattern){
			return events.equals(((SerialEpisodePattern) o).events);
		} else if( o instanceof ParallelEpisodePattern){
			ParallelEpisodePattern other = (ParallelEpisodePattern) o;
			if(length()==1 && other.length()==1){
				return events.get(0).equals(other.getEvents().keySet().iterator().next());
			} else{
				return false;
			}
		} else{
			return false;
		}
	}

}
