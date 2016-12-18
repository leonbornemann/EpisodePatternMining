package episode.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import data.events.CategoricalEventType;
import episode.pattern.recognition.EpisodeRecognitionDFA;
import episode.pattern.recognition.SerialEpisodeRecognitionDFA;
import episode.pattern.storage.EpisodeTrie;

public class SerialEpisodePattern implements EpisodePattern {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<CategoricalEventType> events;
	private transient EpisodeTrie<Object> trieForSelf;
	
	public SerialEpisodePattern(CategoricalEventType... events) {
		this.events =Arrays.asList(events);
	}
	
	public SerialEpisodePattern(List<CategoricalEventType> events) {
		this.events =events;
	}

	@Override
	public int length() {
		return events.size();
	}

	public CategoricalEventType get(int i) {
		return events.get(i);
	}

	public List<CategoricalEventType> subList(int i, int j) {
		return events.subList(i, j);
	}

	public void addEventType(CategoricalEventType annotatedEventType) {
		events.add(annotatedEventType);
	}

	public SerialEpisodeRecognitionDFA<?> getSimpleDFA() {
		if(trieForSelf==null){
			trieForSelf = new EpisodeTrie<Object>();
			trieForSelf.setValue(this, null);
		}
		return new SerialEpisodeRecognitionDFA<>(trieForSelf.bfsIterator().next());
	}
	
	@Override
	public String toString(){
		return events.stream().map(Object::toString).reduce((a,b) -> a + " -> " + b).get();
	}

	@Override
	public EpisodeRecognitionDFA<?> getSimpleRecognitionDFA() {
		return getSimpleDFA();
	}

	@Override
	public Set<CategoricalEventType> getAllContainedTypes() {
		return new HashSet<>(events);
	}

	@Override
	public boolean containsType(CategoricalEventType e) {
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
	public List<CategoricalEventType> getCanonicalListRepresentation() {
		return new ArrayList<>(events);
	}

}
