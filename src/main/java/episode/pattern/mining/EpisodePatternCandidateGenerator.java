package episode.pattern.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.events.CategoricalEventType;
import episode.pattern.EpisodePattern;
import episode.pattern.storage.EpisodeIdentifier;
import episode.pattern.storage.EpisodeTrie;

/***
 * Abstract class for generating episode pattern candidates from previously frequent patterns.
 * @author Leon Bornemann
 *
 * @param <E>
 */
public abstract class EpisodePatternCandidateGenerator<E extends EpisodePattern> {

	public abstract List<E> generateSize1Candidates();

	/***
	 * Generates the length k+1 candidates from the specified length k candidates
	 * @param sizeKCandidates
	 * @return
	 */
	public List<E> generateNewCandidates(List<E> frequentLengthK){
		int episodeLength = frequentLengthK.get(0).length();
		System.out.println("found " + frequentLengthK.size() +" candidates for length " +episodeLength);
		System.out.println("generating candidates for length " +(episodeLength+1));
		if(episodeLength==1){
			return generateCandidates(new ArrayList<>(),frequentLengthK.stream().map(e -> e.getCanonicalListRepresentation().get(0) ).collect(Collectors.toList()));
		}
		Map<List<CategoricalEventType>,List<CategoricalEventType>> blocks = new HashMap<>();
		for(E curPattern : frequentLengthK){
			List<CategoricalEventType> curEpisode = curPattern.getCanonicalListRepresentation();
			assert(episodeLength == curEpisode.size());
			List<CategoricalEventType> block = curEpisode.subList(0,episodeLength-1);
			CategoricalEventType lastEvent = curEpisode.get(episodeLength-1);
			if(blocks.containsKey(block)){
				assert(!blocks.get(block).contains(lastEvent));
				blocks.get(block).add(lastEvent);
			} else{
				List<CategoricalEventType> endings = new ArrayList<>();
				endings.add(lastEvent);
				blocks.put(block,endings);
			}
		}
		List<E> candidates = new ArrayList<>();
		blocks.forEach( (k,v) -> candidates.addAll(generateCandidates(k,v)));
		candidates.forEach(e -> assertLengthEquals(e,episodeLength+1));
		return candidates;
	}
	
	private void assertLengthEquals(E e, int expectedLength) {
		assert(e.length()==expectedLength);
	}
	
	private Map<CategoricalEventType, Integer> createSize1Map(CategoricalEventType e) {
		HashMap<CategoricalEventType,Integer> map = new HashMap<>();
		map.put(e, 1);
		return map;
	}

	public void insertNewCandidates(Iterator<EpisodeIdentifier<List<Boolean>>> frequentLengthK,EpisodeTrie<List<Boolean>> frequentTrie,int oldLength) {
		System.out.println("generating candidates for length " +(oldLength+1));
		if(oldLength==1){
			ArrayList<CategoricalEventType> endings = new ArrayList<>();
			frequentLengthK.forEachRemaining(e -> endings.add(e.getCanonicalEpisodeRepresentation().get(0)));
			generateCandidates(new ArrayList<>(),endings).forEach(e -> addToTrie(e, frequentTrie));
		} else{
			Map<List<CategoricalEventType>,List<CategoricalEventType>> blocks = new HashMap<>();
			//TODO: top
			while (frequentLengthK.hasNext()) {
				EpisodeIdentifier<List<Boolean>> curPattern = frequentLengthK.next();
				List<CategoricalEventType> curEpisode = curPattern.getCanonicalEpisodeRepresentation();
				assert(oldLength == curEpisode.size());
				List<CategoricalEventType> block = curEpisode.subList(0,oldLength-1);
				CategoricalEventType lastEvent = curEpisode.get(oldLength-1);
				if(blocks.containsKey(block)){
					assert(!blocks.get(block).contains(lastEvent));
					blocks.get(block).add(lastEvent);
				} else{
					List<CategoricalEventType> endings = new ArrayList<>();
					endings.add(lastEvent);
					blocks.put(block,endings);
				}
				
			}
			blocks.forEach( (k,v) -> generateCandidates(k,v).forEach(e -> addToTrie(e,frequentTrie)));
		}
	}

	protected abstract void addToTrie(E e, EpisodeTrie<List<Boolean>> frequentTrie);

	protected abstract List<E> generateCandidates(List<CategoricalEventType> k, List<CategoricalEventType> v);


}
