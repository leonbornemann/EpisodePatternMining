package episode.finance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import episode.finance.recognition.ContinousSerialEpisodeRecognitionDFA;
import episode.finance.recognition.SimpleEpisodeRecognitionDFA;
import episode.finance.recognition.SimpleParallelEpisodeIdentifierRecognitionDFA;
import episode.finance.recognition.SimpleSerialEpisodeIdentifierRecognitionDFA;
import episode.finance.storage.EpisodeTrie;
import prediction.data.AnnotatedEventType;

public class SerialEpisodePattern implements EpisodePattern {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<AnnotatedEventType> events;
	private EpisodeTrie<Object> trieForSelf;
	
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

	public SimpleSerialEpisodeIdentifierRecognitionDFA<?> getSimpleDFA() {
		if(trieForSelf==null){
			trieForSelf = new EpisodeTrie<Object>();
			trieForSelf.setValue(this, null);
		}
		return new SimpleSerialEpisodeIdentifierRecognitionDFA<>(trieForSelf.bfsIterator().next());
	}
	
	public ContinousSerialEpisodeRecognitionDFA getContinousDFA(){
		return new ContinousSerialEpisodeRecognitionDFA(this);
	}
	
	@Override
	public String toString(){
		return events.stream().map(Object::toString).reduce((a,b) -> a + " -> " + b).get();
	}

	@Override
	public SimpleEpisodeRecognitionDFA<?> getSimpleRecognitionDFA() {
		return getSimpleDFA();
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
	public int hashCode(){
		int code = 0;
		for(int i=0;i<events.size();i++){
			code += events.get(i).hashCode();
		}
		return code;
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

	@Override
	public List<AnnotatedEventType> getCanonicalListRepresentation() {
		return new ArrayList<>(events);
	}

}
