package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.AnnotatedEventType;
import episode.finance.EpisodePattern;
import episode.finance.ParallelEpisodePattern;
import episode.finance.SerialEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import episode.finance.storage.EpisodeTrie;

public abstract class EpisodePatternGenerator<E extends EpisodePattern> {

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
		Map<List<AnnotatedEventType>,List<AnnotatedEventType>> blocks = new HashMap<>();
		for(E curPattern : frequentLengthK){
			List<AnnotatedEventType> curEpisode = curPattern.getCanonicalListRepresentation();
			assert(episodeLength == curEpisode.size());
			List<AnnotatedEventType> block = curEpisode.subList(0,episodeLength-1);
			AnnotatedEventType lastEvent = curEpisode.get(episodeLength-1);
			if(blocks.containsKey(block)){
				assert(!blocks.get(block).contains(lastEvent));
				blocks.get(block).add(lastEvent);
			} else{
				List<AnnotatedEventType> endings = new ArrayList<>();
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
	
	private Map<AnnotatedEventType, Integer> createSize1Map(AnnotatedEventType e) {
		HashMap<AnnotatedEventType,Integer> map = new HashMap<>();
		map.put(e, 1);
		return map;
	}

	public void insertNewCandidates(Iterator<EpisodeIdentifier<List<Boolean>>> frequentLengthK,EpisodeTrie<List<Boolean>> frequentTrie,int oldLength) {
		System.out.println("generating candidates for length " +(oldLength+1));
		if(oldLength==1){
			ArrayList<AnnotatedEventType> endings = new ArrayList<>();
			frequentLengthK.forEachRemaining(e -> endings.add(e.getCanonicalEpisodeRepresentation().get(0)));
			generateCandidates(new ArrayList<>(),endings).forEach(e -> addToTrie(e, frequentTrie));
		} else{
			Map<List<AnnotatedEventType>,List<AnnotatedEventType>> blocks = new HashMap<>();
			//TODO: top
			while (frequentLengthK.hasNext()) {
				EpisodeIdentifier<List<Boolean>> curPattern = frequentLengthK.next();
				List<AnnotatedEventType> curEpisode = curPattern.getCanonicalEpisodeRepresentation();
				assert(oldLength == curEpisode.size());
				List<AnnotatedEventType> block = curEpisode.subList(0,oldLength-1);
				AnnotatedEventType lastEvent = curEpisode.get(oldLength-1);
				if(blocks.containsKey(block)){
					assert(!blocks.get(block).contains(lastEvent));
					blocks.get(block).add(lastEvent);
				} else{
					List<AnnotatedEventType> endings = new ArrayList<>();
					endings.add(lastEvent);
					blocks.put(block,endings);
				}
				
			}
			blocks.forEach( (k,v) -> generateCandidates(k,v).forEach(e -> addToTrie(e,frequentTrie)));
		}
	}

	protected abstract void addToTrie(E e, EpisodeTrie<List<Boolean>> frequentTrie);

	protected abstract List<E> generateCandidates(List<AnnotatedEventType> k, List<AnnotatedEventType> v);


}
