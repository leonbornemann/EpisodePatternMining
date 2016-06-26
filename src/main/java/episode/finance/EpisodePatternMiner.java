package episode.finance;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public abstract class EpisodePatternMiner<E extends EpisodePattern> {

	private List<StreamWindow> pred;
	private List<StreamWindow> inversePred;
	private EpisodePatternGenerator<E> patternGen;
	private List<StreamWindow> precedingNothingWindows;

	public EpisodePatternMiner(List<StreamWindow> precedingTargetWindows, List<StreamWindow> precedingInverseTargetWindows,List<StreamWindow> precedingNothingWindows, Set<AnnotatedEventType> eventAlphabet){
		this.pred = precedingTargetWindows;
		this.inversePred = precedingInverseTargetWindows;
		this.precedingNothingWindows = precedingNothingWindows;
		this.patternGen = createPatternGen(eventAlphabet);
	}
	
	public Map<E,Integer> mineEpisodePatterns(int s, int n){
		System.out.println("Starting to mine "+getEpisodeTypeName()+" out of "+pred.size() + " windows with support "+s );
		List<E> candidates = patternGen.generateSize1Candidates();
		Map<E,Integer> frequent = new HashMap<>();
		while(true){
			Map<E,Integer> frequencies = countSupport(candidates,pred);
			Map<E,Integer> newFrequent = frequencies.entrySet().stream().filter(e -> e.getValue() >=s).collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue()));
			if(newFrequent.isEmpty()){
				break;
			} else{
				frequent.putAll(newFrequent);
				candidates = patternGen.generateNewCandidates(newFrequent.keySet().stream().collect(Collectors.toList()));
			}
		}
		return getBestPredictors(frequent,n);
	}

	protected abstract String getEpisodeTypeName();

	private Map<E,Integer> getBestPredictors(Map<E, Integer> frequent, int n) {
		Map<E, Integer> supportForInverse = countSupport(frequent.keySet().stream().collect(Collectors.toList()), inversePred);
		Map<E, Integer> supportForNothing = countSupport(frequent.keySet().stream().collect(Collectors.toList()), precedingNothingWindows);
		Map<E, Integer> trustScore = frequent.entrySet().stream().collect(Collectors.toMap(e -> e.getKey(), e -> e.getValue() - supportForInverse.get(e.getKey()) /*- supportForNothing.get(e.getKey())*/));
		Map<E,Integer> best = trustScore.entrySet().stream().sorted((e1,e2) -> ascending(e1.getValue(),e2.getValue())).limit(n).collect(Collectors.toMap( e -> e.getKey(), e -> e.getValue()));
		return best;
	}

	private int ascending(Integer arg1, Integer arg2) {
		return arg2.compareTo(arg1);
	}
	
	protected abstract Map<E, Integer> countSupport(List<E> candidates,List<StreamWindow> windows);

	protected abstract EpisodePatternGenerator<E> createPatternGen(Set<AnnotatedEventType> eventAlphabet);
}
