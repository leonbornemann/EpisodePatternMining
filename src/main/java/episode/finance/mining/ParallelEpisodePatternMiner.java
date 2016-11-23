package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.AnnotatedEventType;
import data.stream.FixedStreamWindow;
import data.stream.StreamWindow;
import episode.finance.ParallelEpisodePattern;
import episode.finance.recognition.SimpleParallelEpisodeIdentifierRecognitionDFA;
import episode.finance.storage.EpisodeIdentifier;

public class ParallelEpisodePatternMiner extends EpisodePatternMiner<ParallelEpisodePattern>{

	public ParallelEpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet){
		super(precedingTargetWindows,eventAlphabet);
	}

	private void processEventArrival(AnnotatedEventType eventType,Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits, int windowIndex) {
		//add a default false value to the current index in the list, if it does not already exist
		waits.values().forEach(
				dfaSet -> dfaSet.stream().map(dfa -> dfa.getEpisodePattern().getAssociatedValue())
				.filter(list -> list.size()<windowIndex+1)
				.forEach(list -> list.add(windowIndex, false))
		);
		if(waits.containsKey(eventType)){
			Set<SimpleParallelEpisodeIdentifierRecognitionDFA<?>> toRemove = new HashSet<>();
			for(SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>> dfa : waits.get(eventType)){
				assert(dfa.waitsFor(eventType));
				dfa.processEvent(eventType);
				if(!dfa.waitsFor(eventType)){
					toRemove.add(dfa);
					if(dfa.isDone()){
						List<Boolean> occurrenceList = dfa.getEpisodePattern().getAssociatedValue();
						assert(occurrenceList.get(windowIndex)==false);
						occurrenceList.set(windowIndex,true);
					}
				}
			}
			toRemove.forEach(dfa -> waits.get(eventType).remove(dfa));
		}
	}

	private Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> buildWaits(List<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>> dfas) {
		Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits = new HashMap<>();
		dfas.forEach(e -> addToAll(e,waits));
		return waits;
	}

	private void addToAll(SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>> dfa,Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits) {
		dfa.getRemainingRequiredEventTypes().forEach(e -> addTo(dfa,e,waits));
	}

	private void addTo(SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>> dfa, AnnotatedEventType e,Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits) {
		if(waits.containsKey(e)){
			waits.get(e).add(dfa);
		} else{
			Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>> set = new HashSet<>();
			boolean added = set.add(dfa);
			assert(true);
			waits.put(e,set);
		}
	}

	@Override
	protected EpisodePatternGenerator<ParallelEpisodePattern> createPatternGen(Set<AnnotatedEventType> eventAlphabet) {
		return new ParallelEpisodePatternGenerator(eventAlphabet);
	}

	@Override
	protected String getEpisodeTypeName() {
		return "Parallel Episodes";
	}

	@Override
	protected void addSupportToTrie(Iterator<EpisodeIdentifier<List<Boolean>>> candidates,List<FixedStreamWindow> windows) {
		List<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>> dfas = new ArrayList<>();
		while (candidates.hasNext()) {
			EpisodeIdentifier<List<Boolean>> episodeIdentifier = candidates.next();
			episodeIdentifier.setAssociatedValue(new ArrayList<>(windows.size()));
			dfas.add(new SimpleParallelEpisodeIdentifierRecognitionDFA<>(episodeIdentifier));
		}
		for(int i=0;i<windows.size();i++){
			StreamWindow window = windows.get(i);
			Map<AnnotatedEventType, Set<SimpleParallelEpisodeIdentifierRecognitionDFA<List<Boolean>>>> waits = buildWaits(dfas);
			final int windowIndex = i;
			window.getEvents().forEach(e -> processEventArrival(e.getEventType(),waits,windowIndex));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
			dfas.forEach(a -> a.reset());
		}
	}

	@Override
	protected ParallelEpisodePattern buildPattern(List<AnnotatedEventType> canonicalEpisodeRepresentation) {
		return new ParallelEpisodePattern(canonicalEpisodeRepresentation);
	}

	
	
}
