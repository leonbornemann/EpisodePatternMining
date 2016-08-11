package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.ParallelEpisodePattern;
import episode.finance.recognition.SimpleParallelEpisodeRecognitionDFA;
import prediction.data.AnnotatedEventType;
import prediction.data.stream.FixedStreamWindow;
import prediction.data.stream.StreamWindow;

public class ParallelEpisodePatternMiner extends EpisodePatternMiner<ParallelEpisodePattern>{

	public ParallelEpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet){
		super(precedingTargetWindows,eventAlphabet);
	}

	@Override
	protected Map<ParallelEpisodePattern, List<Boolean>> countSupport(List<ParallelEpisodePattern> candidates,List<FixedStreamWindow> windows) {
		Map<ParallelEpisodePattern,List<Boolean>> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, new ArrayList<>(windows.size())));
		for(int i=0;i<windows.size();i++){
			StreamWindow window = windows.get(i);
			List<SimpleParallelEpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getSimpleDFA()).collect(Collectors.toList());
			Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits = buildWaits(dfas);
			final int windowIndex = i;
			window.getEvents().forEach(e -> processEventArrival(e.getEventType(),waits,frequencies,windowIndex));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
		}
		return frequencies;
	}

	private void processEventArrival(AnnotatedEventType eventType,Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits,Map<ParallelEpisodePattern, List<Boolean>> frequencies, int windowIndex) {
		if(waits.containsKey(eventType)){
			frequencies.values().forEach(list -> list.add(windowIndex, false));
			Set<SimpleParallelEpisodeRecognitionDFA> toRemove = new HashSet<>();
			for(SimpleParallelEpisodeRecognitionDFA dfa : waits.get(eventType)){
				assert(dfa.waitsFor(eventType));
				dfa.processEvent(eventType);
				if(!dfa.waitsFor(eventType)){
					toRemove.add(dfa);
					if(dfa.isDone()){
						List<Boolean> occurrenceList = frequencies.get(dfa.getEpisodePattern());
						assert(occurrenceList.get(windowIndex)==false);
						occurrenceList.set(windowIndex,true);
					}
				}
			}
			toRemove.forEach(dfa -> waits.get(eventType).remove(dfa));
		}
	}

	private Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> buildWaits(List<SimpleParallelEpisodeRecognitionDFA> dfas) {
		Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits = new HashMap<>();
		dfas.forEach(e -> addToAll(e,waits));
		return waits;
	}

	private void addToAll(SimpleParallelEpisodeRecognitionDFA dfa,Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits) {
		dfa.getRemainingRequiredEventTypes().forEach(e -> addTo(dfa,e,waits));
	}

	private void addTo(SimpleParallelEpisodeRecognitionDFA dfa, AnnotatedEventType e,Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits) {
		if(waits.containsKey(e)){
			waits.get(e).add(dfa);
		} else{
			Set<SimpleParallelEpisodeRecognitionDFA> set = new HashSet<>();
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
	
	
}
