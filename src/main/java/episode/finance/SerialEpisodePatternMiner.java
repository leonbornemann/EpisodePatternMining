package episode.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class SerialEpisodePatternMiner extends EpisodePatternMiner<SerialEpisodePattern>{

	public SerialEpisodePatternMiner(List<StreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet) {
		super(precedingTargetWindows,eventAlphabet);
	}

	protected Map<SerialEpisodePattern,List<Boolean>> countSupport(List<SerialEpisodePattern> candidates, List<StreamWindow> windows) {
		Map<SerialEpisodePattern,List<Boolean>> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, new ArrayList<>(windows.size())));
		for(int i=0;i<windows.size();i++){
			StreamWindow window = windows.get(i);
			List<SimpleSerialEpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getSimpleDFA()).collect(Collectors.toList());
			Map<AnnotatedEventType, List<SimpleSerialEpisodeRecognitionDFA>> waits = dfas.stream().collect(Collectors.groupingBy(SimpleSerialEpisodeRecognitionDFA::peek));
			Map<LocalDateTime, List<AnnotatedEvent>> byTimestamp = window.getEventTypesByTimestamp();
			final int windowIndex = i;
			byTimestamp.keySet().stream().sorted().forEachOrdered(ts -> processEventArrival(frequencies,waits,ts,byTimestamp.get(ts),windowIndex));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
		}
		return frequencies;
	}

	private void processEventArrival(Map<SerialEpisodePattern, List<Boolean>> frequencies,Map<AnnotatedEventType, List<SimpleSerialEpisodeRecognitionDFA>> waits, LocalDateTime ts, List<AnnotatedEvent> events,int windowIndex) {
		frequencies.values().forEach(list -> list.set(windowIndex, false));
		Map<AnnotatedEventType,List<SimpleSerialEpisodeRecognitionDFA>> bag = new HashMap<>();
		for(AnnotatedEvent event : events){
			assert(event.getTimestamp().equals(ts));
			AnnotatedEventType curEvent = event.getEventType();
			if(waits.containsKey(curEvent)){
				for(SimpleSerialEpisodeRecognitionDFA dfa : waits.get(curEvent)){
					assert(dfa.waitsFor(curEvent));
					dfa.processEvent(curEvent);
					if(dfa.isDone()){
						List<Boolean> occurrenceList = frequencies.get(dfa.getEpisodePattern());
						assert(occurrenceList.get(windowIndex)==false);
						occurrenceList.set(windowIndex,true);
					} else {
						AnnotatedEventType nextEvent = dfa.peek();
						if(bag.containsKey(nextEvent)){
							bag.get(nextEvent).add(dfa);
						} else{
							List<SimpleSerialEpisodeRecognitionDFA> newList = new ArrayList<>();
							newList.add(dfa);
							bag.put(nextEvent,newList);
						}
					}
				}
				waits.get(curEvent).clear();
			}
		}
		bag.forEach((k,v) -> addTo(waits,k,v));
	}

	private void addTo(Map<AnnotatedEventType, List<SimpleSerialEpisodeRecognitionDFA>> waits, AnnotatedEventType k,List<SimpleSerialEpisodeRecognitionDFA> v) {
		if(waits.containsKey(k)){
			waits.get(k).addAll(v);
		} else{
			waits.put(k, v);
		}
	}

	@Override
	protected EpisodePatternGenerator<SerialEpisodePattern> createPatternGen(Set<AnnotatedEventType> eventAlphabet) {
		return new SerialEpisodePatternGenerator(eventAlphabet);
	}

	@Override
	protected String getEpisodeTypeName() {
		return "Serial Episodes";
	}

}
