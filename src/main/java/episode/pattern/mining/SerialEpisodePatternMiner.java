package episode.pattern.mining;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.stream.FixedStreamWindow;
import data.stream.StreamWindow;
import episode.pattern.SerialEpisodePattern;
import episode.pattern.recognition.SimpleSerialEpisodeIdentifierRecognitionDFA;
import episode.pattern.storage.EpisodeIdentifier;

/***
 * Implements the mining of frequent serial episode patterns from windows
 * @author Leon Bornemann
 *
 */
public class SerialEpisodePatternMiner extends EpisodePatternMiner<SerialEpisodePattern>{

	public SerialEpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<CategoricalEventType> eventAlphabet) {
		super(precedingTargetWindows,eventAlphabet);
	}
	
	@Override
	protected void addSupportToTrie(Iterator<EpisodeIdentifier<List<Boolean>>> candidates,List<FixedStreamWindow> windows) {
		List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>> dfas = new ArrayList<>();
		while (candidates.hasNext()) {
			EpisodeIdentifier<List<Boolean>> episodeIdentifier = candidates.next();
			episodeIdentifier.setAssociatedValue(new ArrayList<>(windows.size()));
			dfas.add(new SimpleSerialEpisodeIdentifierRecognitionDFA<>(episodeIdentifier));
		}
		for(int i=0;i<windows.size();i++){
			StreamWindow window = windows.get(i);
			assert(window.getEvents().size()!=0);
			Map<CategoricalEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits = dfas.stream().collect(Collectors.groupingBy(SimpleSerialEpisodeIdentifierRecognitionDFA::peek));
			Map<LocalDateTime, List<CategoricalEvent>> byTimestamp = window.getEventTypesByTimestamp();
			final int windowIndex = i;
			byTimestamp.keySet().stream().sorted().forEachOrdered(ts -> processEventArrival(waits,ts,byTimestamp.get(ts),windowIndex));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
			dfas.forEach(a -> a.reset());
		}
		for (SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>> dfa : dfas) {
			assert(dfa.getEpisodePattern().getAssociatedValue().size()==windows.size());
		}
	}

	private void processEventArrival(Map<CategoricalEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits, LocalDateTime ts, List<CategoricalEvent> events,int windowIndex) {
		//add a default false value to the current index in the list, if it does not already exist
		waits.values().forEach(
				dfaSet -> dfaSet.stream().map(dfa -> dfa.getEpisodePattern().getAssociatedValue())
				.filter(list -> list.size()<windowIndex+1)
				.forEach(list -> list.add(windowIndex, false))
		);
		Map<CategoricalEventType,List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> bag = new HashMap<>();
		for(CategoricalEvent event : events){
			assert(event.getTimestamp().equals(ts));
			CategoricalEventType curEvent = event.getEventType();
			if(waits.containsKey(curEvent)){
				for(SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>> dfa : waits.get(curEvent)){
					assert(dfa.waitsFor(curEvent));
					dfa.processEvent(curEvent);
					if(dfa.isDone()){
						List<Boolean> occurrenceList = dfa.getEpisodePattern().getAssociatedValue();
						assert(occurrenceList.get(windowIndex)==false);
						assert(occurrenceList.size()==windowIndex+1);
						occurrenceList.set(windowIndex,true);
					} else {
						CategoricalEventType nextEvent = dfa.peek();
						if(bag.containsKey(nextEvent)){
							bag.get(nextEvent).add(dfa);
						} else{
							List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>> newList = new ArrayList<>();
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

	private void addTo(Map<CategoricalEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits, CategoricalEventType k,List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>> v) {
		if(waits.containsKey(k)){
			waits.get(k).addAll(v);
		} else{
			waits.put(k, v);
		}
	}

	@Override
	protected EpisodePatternCandidateGenerator<SerialEpisodePattern> createPatternGen(Set<CategoricalEventType> eventAlphabet) {
		return new SerialEpisodePatternCandidateGenerator(eventAlphabet);
	}

	@Override
	protected String getEpisodeTypeName() {
		return "Serial Episodes";
	}

	@Override
	protected SerialEpisodePattern buildPattern(List<CategoricalEventType> canonicalEpisodeRepresentation) {
		return new SerialEpisodePattern(canonicalEpisodeRepresentation);
	}

}
