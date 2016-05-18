package episode.finance;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
		Map<SerialEpisodePattern,Integer> best = trustScore.entrySet().stream().sorted(Comparator.comparing(Map.Entry::getValue)).limit(n).collect(Collectors.toMap( e -> e.getKey(), e -> e.getValue()));
		return best;
	}

	private Map<SerialEpisodePattern,Integer> countSupport(List<SerialEpisodePattern> candidates, List<StreamWindow> windows) {
		Map<SerialEpisodePattern,Integer> frequencies = new HashMap<>();
		candidates.forEach(e -> frequencies.put(e, 0));
		for(StreamWindow window : windows){
			List<EpisodeRecognitionDFA> dfas = candidates.stream().map(e -> e.getDFA()).collect(Collectors.toList());
			Map<AnnotatedEventType, List<EpisodeRecognitionDFA>> waits = dfas.stream().collect(Collectors.groupingBy(EpisodeRecognitionDFA::waitsFor));
			for(AnnotatedEvent event: window.getEvents()){
				processEventArrival(frequencies, waits, event);
			}
		}
		return frequencies;
	}

	private void processEventArrival(Map<SerialEpisodePattern, Integer> frequencies,Map<AnnotatedEventType, List<EpisodeRecognitionDFA>> waits, AnnotatedEvent event) {
		AnnotatedEventType curEvent = event.getEventType();
		if(waits.containsKey(curEvent)){
			List<EpisodeRecognitionDFA> bag = new ArrayList<>();
			for(EpisodeRecognitionDFA dfa : waits.get(curEvent)){
				assert(dfa.waitsFor().equals(curEvent));
				dfa.transition();
				if(dfa.isDone()){
					frequencies.put(dfa.getEpiosdePattern(), frequencies.get(dfa.getEpiosdePattern())+1);
				} else if(dfa.waitsFor().equals(curEvent)){
					bag.add(dfa);
				} else{
					AnnotatedEventType nextEvent = dfa.waitsFor();
					if(waits.containsKey(nextEvent)){
						waits.get(nextEvent).add(dfa);
					} else{
						List<EpisodeRecognitionDFA> newList = new ArrayList<>();
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
