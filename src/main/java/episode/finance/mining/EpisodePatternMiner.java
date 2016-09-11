package episode.finance.mining;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import episode.finance.EpisodePattern;
import episode.finance.SerialEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import episode.finance.storage.EpisodeTrie;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.FixedStreamWindow;

public abstract class EpisodePatternMiner<E extends EpisodePattern> {

	private List<FixedStreamWindow> pred;
	private EpisodePatternGenerator<E> patternGen;

	public EpisodePatternMiner(List<FixedStreamWindow> precedingTargetWindows, Set<AnnotatedEventType> eventAlphabet){
		this.pred = precedingTargetWindows;
		this.patternGen = createPatternGen(eventAlphabet);
	}
	
	public EpisodeTrie<List<Boolean>> mineFrequentEpisodePatterns(int s){
		System.out.println("Starting to mine "+getEpisodeTypeName()+" out of "+pred.size() + " windows with support "+s );
		List<E> initialCandidates = patternGen.generateSize1Candidates();
		EpisodeTrie<List<Boolean>> frequentTrie = new EpisodeTrie<>();
		initialCandidates.forEach(c -> frequentTrie.setValue(c, null));
		Set<EpisodeIdentifier<List<Boolean>>> candidates = frequentTrie.stream().collect(Collectors.toSet());
		int size=1;
		while(true){
			//TODO: insert support for each candidate!
			addSupportToTrie(candidates.iterator(),pred);
			List<Boolean> a = frequentTrie.getValue(new SerialEpisodePattern(Arrays.asList(new AnnotatedEventType("AAPL",Change.EQUAL))));
			filterAllBelowMinSup(candidates.iterator(),pred,s);
			a = frequentTrie.getValue(new SerialEpisodePattern(Arrays.asList(new AnnotatedEventType("AAPL",Change.EQUAL))));
			Iterator<EpisodeIdentifier<List<Boolean>>> newFrequent = frequentTrie.getAllOfSize(size).iterator();
			if(!newFrequent.hasNext()){
				break;
			} else{
				patternGen.insertNewCandidates(newFrequent,frequentTrie,size);
			}
			size++;
			candidates = frequentTrie.getAllOfSize(size);
		}
		//TODO: trie now contains all frequent episodes
		return frequentTrie;
	}
	
	private void filterAllBelowMinSup(Iterator<EpisodeIdentifier<List<Boolean>>> candidates,List<FixedStreamWindow> pred2, int s) {
		List<EpisodeIdentifier<List<Boolean>>> toDelete = new ArrayList<>();
		while (candidates.hasNext()) {
			EpisodeIdentifier<List<Boolean>> episodeIdentifier = candidates.next();
			assert(episodeIdentifier.getAssociatedValue().size()==pred.size());
			if(!isFrequent(episodeIdentifier.getAssociatedValue(), s)){
				toDelete.add(episodeIdentifier);
			}
		}
		toDelete.forEach(e -> e.deleteElement());		
	}

	protected abstract void addSupportToTrie(Iterator<EpisodeIdentifier<List<Boolean>>> candidates,List<FixedStreamWindow> windows);

	private Boolean isFrequent(List<Boolean> list,int s) {
		return countOccurrences(list) >=s;
	}

	private int countOccurrences(List<Boolean> list) {
		return (int) list.stream().filter(p -> p == true).count();
	}

	public Map<E,Double> mineBestEpisodePatterns(int s, int n,List<FixedStreamWindow> inversePred, List<FixedStreamWindow> precedingNothingWindows){
		return getBestPredictors(mineFrequentEpisodePatterns(s),n,inversePred,precedingNothingWindows);
	}

	protected abstract String getEpisodeTypeName();

	private Map<E,Double> getBestPredictors(EpisodeTrie<List<Boolean>> frequent, int n, List<FixedStreamWindow> inversePred, List<FixedStreamWindow> precedingNothingWindows) {
		EpisodeTrie<List<Boolean>> inverseFrequent = new EpisodeTrie<>();
		Iterator<EpisodeIdentifier<List<Boolean>>> allFrequentEpisodes = frequent.bfsIterator();
		while (allFrequentEpisodes.hasNext()) {
			inverseFrequent.setValue(buildPattern(allFrequentEpisodes.next().getCanonicalEpisodeRepresentation()), null);
		}
		addSupportToTrie(inverseFrequent.bfsIterator(),inversePred);
		//Map<E, List<Boolean>> supportForNothing = countSupport(frequent.keySet().stream().collect(Collectors.toList()), precedingNothingWindows);
		Stream<EpisodeIdentifier<List<Boolean>>> stream = StreamSupport.stream(Spliterators.spliteratorUnknownSize(frequent.bfsIterator(), Spliterator.ORDERED), false);
		Map<EpisodeIdentifier<List<Boolean>>, Double> confidence = stream.collect(Collectors.toMap(e ->e, e->calcConfidence(e.getCanonicalEpisodeRepresentation(), frequent,inverseFrequent)));
		Map<E,Double> best = confidence.entrySet().stream()
				.sorted((e1,e2) -> ascending(e1.getValue(),e2.getValue()))
				.limit(n).collect(Collectors.toMap( e -> buildPattern(e.getKey().getCanonicalEpisodeRepresentation()), e -> e.getValue()));
		return best;
	}

	protected abstract E buildPattern(List<AnnotatedEventType> canonicalEpisodeRepresentation);

	private double calcConfidence(List<AnnotatedEventType> canonical, EpisodeTrie<List<Boolean>> frequent, EpisodeTrie<List<Boolean>> inverseFrequent) {
		EpisodePattern pattern = buildPattern(canonical);
		int support = countOccurrences(frequent.getValue(pattern));
		int inverseSupport = countOccurrences(inverseFrequent.getValue(pattern));
		return support / (double) (inverseSupport+support);
	}

	private int ascending(Double arg1, Double arg2) {
		return arg2.compareTo(arg1);
	}
	

	protected abstract EpisodePatternGenerator<E> createPatternGen(Set<AnnotatedEventType> eventAlphabet);

}
