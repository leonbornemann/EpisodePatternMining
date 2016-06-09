package episode.finance;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class ParallelEpisodePatternMiner extends EpisodePatternMiner<ParallelEpisodePattern>{

	public ParallelEpisodePatternMiner(List<StreamWindow> precedingTargetWindows, List<StreamWindow> precedingInverseTargetWindows,Set<AnnotatedEventType> eventAlphabet){
		super(precedingTargetWindows,precedingInverseTargetWindows,eventAlphabet);
	}

	@Override
	protected Map<ParallelEpisodePattern, Integer> countSupport(List<ParallelEpisodePattern> candidates,List<StreamWindow> windows) {
		Map<ParallelEpisodePattern,Integer> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, 0));
		for(StreamWindow window : windows){
			List<SimpleParallelEpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getSimpleDFA()).collect(Collectors.toList());
			//TODO: this
			Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits = buildWaits(dfas);
			window.getEvents().forEach(e -> processEventArrival(e.getEventType(),waits,frequencies));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
		}
		return frequencies;
	}

	private void processEventArrival(AnnotatedEventType eventType,Map<AnnotatedEventType, Set<SimpleParallelEpisodeRecognitionDFA>> waits,Map<ParallelEpisodePattern, Integer> frequencies) {
		if(waits.containsKey(eventType)){
			Set<SimpleParallelEpisodeRecognitionDFA> toRemove = new HashSet<>();
			for(SimpleParallelEpisodeRecognitionDFA dfa : waits.get(eventType)){
				assert(dfa.waitsFor(eventType));
				dfa.processEvent(eventType);
				if(!dfa.waitsFor(eventType)){
					toRemove.add(dfa);
					if(dfa.isDone()){
						frequencies.put(dfa.getEpisodePattern(), frequencies.get(dfa.getEpisodePattern())+1);
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
	
	
}
