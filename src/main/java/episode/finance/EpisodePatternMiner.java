package episode.finance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.FixedStreamWindow;

public abstract class EpisodePatternMiner<E extends EpisodePattern> {

	private List<FixedStreamWindow> pred;
	private EpisodePatternGenerator<E> patternGen;

	public EpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet){
		this.pred = precedingTargetWindows;
		this.patternGen = createPatternGen(eventAlphabet);
	}
	
	public Map<E,List<Boolean>> mineFrequentEpisodePatterns(int s){
		System.out.println("Starting to mine "+getEpisodeTypeName()+" out of "+pred.size() + " windows with support "+s );
		List<E> candidates = patternGen.generateSize1Candidates();
		Map<E,List<Boolean>> frequent = new HashMap<>();
		while(true){
			Map<E,List<Boolean>> frequencies = countSupport(candidates,pred);
			Map<E,List<Boolean>> newFrequent = frequencies.entrySet().stream().filter(e -> isFrequent(e.getValue(),s)).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
			if(newFrequent.isEmpty()){
				break;
			} else{
				frequent.putAll(newFrequent);
				candidates = patternGen.generateNewCandidates(newFrequent.keySet().stream().collect(Collectors.toList()));
			}
		}
		return frequent;
	}
	
	private Boolean isFrequent(List<Boolean> list,int s) {
		return countOccurrences(list) >=s;
	}

	private int countOccurrences(List<Boolean> list) {
		return (int) list.stream().filter(p -> p == true).count();
	}

	public Map<E,Integer> mineBestEpisodePatterns(int s, int n,List<FixedStreamWindow> inversePred, List<FixedStreamWindow> precedingNothingWindows){
		return getBestPredictors(mineFrequentEpisodePatterns(s),n,inversePred,precedingNothingWindows);
	}

	protected abstract String getEpisodeTypeName();

	private Map<E,Integer> getBestPredictors(Map<E, List<Boolean>> frequent, int n, List<FixedStreamWindow> inversePred, List<FixedStreamWindow> precedingNothingWindows) {
		Map<E, List<Boolean>> supportForInverse = countSupport(frequent.keySet().stream().collect(Collectors.toList()), inversePred);
		Map<E, List<Boolean>> supportForNothing = countSupport(frequent.keySet().stream().collect(Collectors.toList()), precedingNothingWindows);
		Map<E, Integer> trustScore = frequent.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(),e -> new Integer(countOccurrences(e.getValue()) - countOccurrences(supportForInverse.get(e.getKey())))));
		Map<E,Integer> best = trustScore.entrySet().stream().sorted((e1,e2) -> ascending(e1.getValue(),e2.getValue())).limit(n).collect(Collectors.toMap( e -> e.getKey(), e -> e.getValue()));
		return best;
	}


	private int ascending(Integer arg1, Integer arg2) {
		return arg2.compareTo(arg1);
	}
	
	protected abstract Map<E, List<Boolean>> countSupport(List<E> candidates,List<FixedStreamWindow> windows);

	protected abstract EpisodePatternGenerator<E> createPatternGen(Set<AnnotatedEventType> eventAlphabet);
}
