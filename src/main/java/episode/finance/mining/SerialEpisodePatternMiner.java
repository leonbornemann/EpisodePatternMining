package episode.finance.mining;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import data.AnnotatedEvent;
import data.AnnotatedEventType;
import data.stream.FixedStreamWindow;
import data.stream.StreamWindow;
import episode.finance.EpisodePattern;
import episode.finance.SerialEpisodePattern;
import episode.finance.recognition.SimpleParallelEpisodeIdentifierRecognitionDFA;
import episode.finance.recognition.SimpleSerialEpisodeIdentifierRecognitionDFA;
import episode.finance.storage.EpisodeIdentifier;
import episode.finance.storage.EpisodeTrie;
import prediction.util.StandardDateTimeFormatter;

public class SerialEpisodePatternMiner extends EpisodePatternMiner<SerialEpisodePattern>{

	public SerialEpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet) {
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
			Map<AnnotatedEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits = dfas.stream().collect(Collectors.groupingBy(SimpleSerialEpisodeIdentifierRecognitionDFA::peek));
			Map<LocalDateTime, List<AnnotatedEvent>> byTimestamp = window.getEventTypesByTimestamp();
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

	private void processEventArrival(Map<AnnotatedEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits, LocalDateTime ts, List<AnnotatedEvent> events,int windowIndex) {
		//add a default false value to the current index in the list, if it does not already exist
		waits.values().forEach(
				dfaSet -> dfaSet.stream().map(dfa -> dfa.getEpisodePattern().getAssociatedValue())
				.filter(list -> list.size()<windowIndex+1)
				.forEach(list -> list.add(windowIndex, false))
		);
		Map<AnnotatedEventType,List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> bag = new HashMap<>();
		for(AnnotatedEvent event : events){
			assert(event.getTimestamp().equals(ts));
			AnnotatedEventType curEvent = event.getEventType();
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
						AnnotatedEventType nextEvent = dfa.peek();
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

	private void addTo(Map<AnnotatedEventType, List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits, AnnotatedEventType k,List<SimpleSerialEpisodeIdentifierRecognitionDFA<List<Boolean>>> v) {
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

	@Override
	protected SerialEpisodePattern buildPattern(List<AnnotatedEventType> canonicalEpisodeRepresentation) {
		return new SerialEpisodePattern(canonicalEpisodeRepresentation);
	}

}
