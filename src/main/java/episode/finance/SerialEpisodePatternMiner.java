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

	public SerialEpisodePatternMiner(List<StreamWindow> precedingTargetWindows,List<StreamWindow> precedingInverseTargetWindows, List<StreamWindow> precedingNothingWindows, Set<AnnotatedEventType> eventAlphabet) {
		super(precedingTargetWindows, precedingInverseTargetWindows,precedingNothingWindows, eventAlphabet);
	}

	protected Map<SerialEpisodePattern,Integer> countSupport(List<SerialEpisodePattern> candidates, List<StreamWindow> windows) {
		Map<SerialEpisodePattern,Integer> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, 0));
		for(StreamWindow window : windows){
			List<SimpleSerialEpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getSimpleDFA()).collect(Collectors.toList());
			Map<AnnotatedEventType, List<SimpleSerialEpisodeRecognitionDFA>> waits = dfas.stream().collect(Collectors.groupingBy(SimpleSerialEpisodeRecognitionDFA::nextEvent));
			Map<LocalDateTime, List<AnnotatedEvent>> byTimestamp = window.getEventTypesByTimestamp();
			byTimestamp.keySet().stream().sorted().forEachOrdered(ts -> processEventArrival(frequencies,waits,ts,byTimestamp.get(ts)));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
		}
		return frequencies;
	}

	private void processEventArrival(Map<SerialEpisodePattern, Integer> frequencies,Map<AnnotatedEventType, List<SimpleSerialEpisodeRecognitionDFA>> waits, LocalDateTime ts, List<AnnotatedEvent> events) {
		Map<AnnotatedEventType,List<SimpleSerialEpisodeRecognitionDFA>> bag = new HashMap<>();
		for(AnnotatedEvent event : events){
			assert(event.getTimestamp().equals(ts));
			AnnotatedEventType curEvent = event.getEventType();
			if(waits.containsKey(curEvent)){
				for(SimpleSerialEpisodeRecognitionDFA dfa : waits.get(curEvent)){
					assert(dfa.waitsFor(curEvent));
					dfa.processEvent(curEvent);
					if(dfa.isDone()){
						frequencies.put(dfa.getEpisodePattern(), frequencies.get(dfa.getEpisodePattern())+1);
					} else {
						AnnotatedEventType nextEvent = dfa.nextEvent();
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
