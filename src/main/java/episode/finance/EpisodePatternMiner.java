package episode.finance;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class EpisodePatternMiner {

	private List<StreamWindow> pred;
	private List<StreamWindow> inversePred;
	private SerialEpisodePatternGenerator patternGen;

	public EpisodePatternMiner(List<StreamWindow> precedingTargetWindows, List<StreamWindow> precedingInverseTargetWindows,Set<AnnotatedEventType> eventAlphabet){
		this.pred = precedingTargetWindows;
		this.inversePred = precedingInverseTargetWindows;
		this.patternGen = new SerialEpisodePatternGenerator(eventAlphabet);
	}
	
	public Map<SerialEpisodePattern,Integer> mineSerialEpisodes(int s, int n){
		System.out.println("Starting to mine serial episodes out of "+pred.size() + " windows with support "+s );
		List<SerialEpisodePattern> candidates = patternGen.generateSize1Candidates();
		Map<SerialEpisodePattern,Integer> frequent = new HashMap<>();
		while(true){
			System.out.println("new Iteration");
			Map<SerialEpisodePattern,Integer> frequencies = countSupport(candidates, pred);
			Map<SerialEpisodePattern,Integer> newFrequent = frequencies.entrySet().stream().filter(e -> e.getValue() >=s).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
			if(newFrequent.isEmpty()){
				break;
			} else{
				frequent.putAll(newFrequent);
				candidates = patternGen.generateNewCandidates(newFrequent.keySet().stream().collect(Collectors.toList()));
			}
		}
		return getBestPredictors(frequent,n);
	}

	private Map<SerialEpisodePattern,Integer> getBestPredictors(Map<SerialEpisodePattern, Integer> frequent, int n) {
		Map<SerialEpisodePattern, Integer> supportForInverse = countSupport(frequent.keySet().stream().collect(Collectors.toList()), inversePred);
		Map<SerialEpisodePattern, Integer> trustScore = frequent.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() - supportForInverse.get(e.getKey())));
		Map<SerialEpisodePattern,Integer> best = trustScore.entrySet().stream().sorted((e1,e2) -> ascending(e1.getValue(),e2.getValue())).limit(n).collect(Collectors.toMap( e -> e.getKey(), e -> e.getValue()));
		return best;
	}

	private int ascending(Integer arg1, Integer arg2) {
		return arg2.compareTo(arg1);
	}

	private Map<SerialEpisodePattern,Integer> countSupport(List<SerialEpisodePattern> candidates, List<StreamWindow> windows) {
		Map<SerialEpisodePattern,Integer> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, 0));
		for(StreamWindow window : windows){
			List<SimpleEpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getSimpleDFA()).collect(Collectors.toList());
			Map<AnnotatedEventType, List<SimpleEpisodeRecognitionDFA>> waits = dfas.stream().collect(Collectors.groupingBy(SimpleEpisodeRecognitionDFA::waitsFor));
			Map<LocalDateTime, List<AnnotatedEvent>> byTimestamp = window.getEventTypesByTimestamp();
			byTimestamp.keySet().stream().sorted().forEachOrdered(ts -> processEventArrival(frequencies,waits,ts,byTimestamp.get(ts)));
			/*for(LocalDateTime ts: byTimestamp.keySet()){
				processEventArrival(frequencies, waits, event);
			}*/
		}
		return frequencies;
	}

	private void processEventArrival(Map<SerialEpisodePattern, Integer> frequencies,Map<AnnotatedEventType, List<SimpleEpisodeRecognitionDFA>> waits, LocalDateTime ts, List<AnnotatedEvent> events) {
		Map<AnnotatedEventType,List<SimpleEpisodeRecognitionDFA>> bag = new HashMap<>();
		for(AnnotatedEvent event : events){
			assert(event.getTimestamp().equals(ts));
			AnnotatedEventType curEvent = event.getEventType();
			if(waits.containsKey(curEvent)){
				for(SimpleEpisodeRecognitionDFA dfa : waits.get(curEvent)){
					assert(dfa.waitsFor().equals(curEvent));
					dfa.transition();
					if(dfa.isDone()){
						frequencies.put(dfa.getEpisodePattern(), frequencies.get(dfa.getEpisodePattern())+1);
					} else {
						AnnotatedEventType nextEvent = dfa.waitsFor();
						if(bag.containsKey(nextEvent)){
							bag.get(nextEvent).add(dfa);
						} else{
							List<SimpleEpisodeRecognitionDFA> newList = new ArrayList<>();
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

	private void addTo(Map<AnnotatedEventType, List<SimpleEpisodeRecognitionDFA>> waits, AnnotatedEventType k,List<SimpleEpisodeRecognitionDFA> v) {
		if(waits.containsKey(k)){
			waits.get(k).addAll(v);
		} else{
			waits.put(k, v);
		}
	}

	private void processEventArrival(Map<SerialEpisodePattern, Integer> frequencies,Map<AnnotatedEventType, List<SimpleEpisodeRecognitionDFA>> waits, AnnotatedEvent event) {
		AnnotatedEventType curEvent = event.getEventType();
		if(waits.containsKey(curEvent)){
			List<SimpleEpisodeRecognitionDFA> bag = new ArrayList<>();
			for(SimpleEpisodeRecognitionDFA dfa : waits.get(curEvent)){
				assert(dfa.waitsFor().equals(curEvent));
				dfa.transition();
				if(dfa.isDone()){
					frequencies.put(dfa.getEpisodePattern(), frequencies.get(dfa.getEpisodePattern())+1);
				} else if(dfa.waitsFor().equals(curEvent)){
					bag.add(dfa);
				} else{
					AnnotatedEventType nextEvent = dfa.waitsFor();
					if(waits.containsKey(nextEvent)){
						waits.get(nextEvent).add(dfa);
					} else{
						List<SimpleEpisodeRecognitionDFA> newList = new ArrayList<>();
						newList.add(dfa);
						waits.put(nextEvent,newList);
					}
				}
			}
			waits.get(curEvent).clear();
			waits.get(curEvent).addAll(bag);
		}
	}

}
