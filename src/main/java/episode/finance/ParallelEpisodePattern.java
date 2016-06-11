package episode.finance;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class ParallelEpisodePattern implements EpisodePattern{

	private Map<AnnotatedEventType,Integer> events;
	
	public ParallelEpisodePattern(Map<AnnotatedEventType,Integer> events) {
		this.events =events;
	}
	
	public ParallelEpisodePattern(List<AnnotatedEventType> pattern) {
		events = new HashMap<>();
		pattern.forEach(e -> addToEvents(e));
	}

	public ParallelEpisodePattern(AnnotatedEventType... types) {
		this(Arrays.asList(types));
	}

	private void addToEvents(AnnotatedEventType e) {
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
	public List<AnnotatedEventType> getCanonicalListRepresentation() {
		List<AnnotatedEventType> list = new ArrayList<>();
		events.keySet().stream().sorted().forEach(e -> addToCanonicalList(e,list));
		return list;
	}

	private void addToCanonicalList(AnnotatedEventType e, List<AnnotatedEventType> list) {
		int count = events.get(e);
		for(int i=0;i<count ;i++){
			list.add(e);
		}
	}

	@Override
	public SimpleEpisodeRecognitionDFA getSimpleRecognitionDFA() {
		return getSimpleDFA();
	}

	public SimpleParallelEpisodeRecognitionDFA getSimpleDFA(){
		return new SimpleParallelEpisodeRecognitionDFA(this);
	}

	public Map<AnnotatedEventType,Integer> getEvents() {
		return events;
	}
	
	@Override
	public String toString(){
		return getCanonicalListRepresentation().toString();
	}

	@Override
	public ContinousEpisodeRecognitionDFA getContinousDFA() {
		return new ContinousParallelEpisodeRecognitionDFA(this);
	}

}
