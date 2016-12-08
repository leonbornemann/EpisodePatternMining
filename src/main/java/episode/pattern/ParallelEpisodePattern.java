package episode.pattern;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.events.CategoricalEventType;
import episode.pattern.recognition.SimpleEpisodeRecognitionDFA;
import episode.pattern.recognition.SimpleParallelEpisodeIdentifierRecognitionDFA;
import episode.pattern.storage.EpisodeTrie;

public class ParallelEpisodePattern implements EpisodePattern{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<CategoricalEventType,Integer> events;
	
	private transient EpisodeTrie<?> trieForSelf = null;
	
	public ParallelEpisodePattern(Map<CategoricalEventType,Integer> events) {
		this.events =events;
	}
	
	public ParallelEpisodePattern(List<CategoricalEventType> pattern) {
		events = new HashMap<>();
		pattern.forEach(e -> addToEvents(e));
	}

	public ParallelEpisodePattern(CategoricalEventType... types) {
		this(Arrays.asList(types));
	}

	private void addToEvents(CategoricalEventType e) {
		if(events.containsKey(e)){
			events.put(e, events.get(e)+1);
		} else{
			events.put(e,1);
		}
	}

	@Override
	public int length() {
		return events.values().stream().mapToInt(e -> e.intValue()).sum();
	}

	/***
	 * Returns the Event Types in this parallel episodes in a sorted List (including all dublicates)
	 * @return
	 */
	public List<CategoricalEventType> getCanonicalListRepresentation() {
		List<CategoricalEventType> list = new ArrayList<>();
		events.keySet().stream().sorted().forEach(e -> addToCanonicalList(e,list));
		return list;
	}

	private void addToCanonicalList(CategoricalEventType e, List<CategoricalEventType> list) {
		int count = events.get(e);
		for(int i=0;i<count ;i++){
			list.add(e);
		}
	}

	@Override
	public SimpleEpisodeRecognitionDFA<?> getSimpleRecognitionDFA() {
		return getSimpleDFA();
	}

	public SimpleParallelEpisodeIdentifierRecognitionDFA<?> getSimpleDFA(){
		if(trieForSelf==null){
			trieForSelf = new EpisodeTrie<Object>();
			trieForSelf.setValue(this, null);
		}
		return new SimpleParallelEpisodeIdentifierRecognitionDFA<>(trieForSelf.bfsIterator().next());
	}

	public Map<CategoricalEventType,Integer> getEvents() {
		return events;
	}
	
	@Override
	public String toString(){
		return getCanonicalListRepresentation().toString();
	}

	@Override
	public Set<CategoricalEventType> getAllContainedTypes() {
		return events.keySet();
	}
	
	@Override
	public boolean containsType(CategoricalEventType e) {
		return events.keySet().contains(e);
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof ParallelEpisodePattern){
			return events.equals(((ParallelEpisodePattern) o).getEvents());
		} else if( o instanceof SerialEpisodePattern){
			SerialEpisodePattern other = (SerialEpisodePattern) o;
			if(length()==1 && other.length()==1){
				return events.keySet().iterator().next().equals(other.get(0));
			} else{
				return false;
			}
		} else{
			return false;
		}
	}
	
	@Override
	public int hashCode(){
		int code = 0;
		for (CategoricalEventType elem : events.keySet()) {
			code += elem.hashCode()*events.get(elem);
		}
		return code;
	}

}
