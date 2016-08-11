package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.ParallelEpisodePattern;
import prediction.data.AnnotatedEventType;

public class ParallelEpisodePatternGenerator implements EpisodePatternGenerator<ParallelEpisodePattern>{

	protected Set<AnnotatedEventType> eventAlphabet;

	public ParallelEpisodePatternGenerator(Set<AnnotatedEventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	/***
	 * Given the frequent Episodes of length k, this will output all possible candidate episodes of size k+1 that may be frequent
	 * @param frequentLengthK a list of frequent epsiodes (must all have the same size)
	 * @return
	 */
	public List<ParallelEpisodePattern> generateNewCandidates(List<ParallelEpisodePattern> frequentLengthK) {
		int episodeLength = frequentLengthK.get(0).length();
		System.out.println("found " + frequentLengthK.size() +" candidates for length " +episodeLength);
		System.out.println("generating candidates for length " +(episodeLength+1));
		if(episodeLength==1){
			return generateCandidates(new ArrayList<>(),frequentLengthK.stream().map(e -> e.getCanonicalListRepresentation().get(0) ).collect(Collectors.toList()));
		}
		Map<List<AnnotatedEventType>,List<AnnotatedEventType>> blocks = new HashMap<>();
		for(ParallelEpisodePattern curPattern : frequentLengthK){
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
		List<ParallelEpisodePattern> candidates = new ArrayList<>();
		blocks.forEach( (k,v) -> candidates.addAll(generateCandidates(k,v)));
		candidates.forEach(e -> assertLengthEquals(e,episodeLength+1));
		return candidates;
	}

	private void assertLengthEquals(ParallelEpisodePattern e, int expectedLength) {
		assert(e.length()==expectedLength);
	}

	private List<ParallelEpisodePattern> generateCandidates(List<AnnotatedEventType> block, List<AnnotatedEventType> endings) {
		assert(new HashSet<>(endings).size()==endings.size());
		List<ParallelEpisodePattern> candidates = new ArrayList<>();
		for(int i=0;i<endings.size();i++){
			for(int j=i;j<endings.size();j++){
				List<AnnotatedEventType> pattern = new ArrayList<>(block);
				pattern.add(endings.get(i));
				pattern.add(endings.get(j));
				candidates.add(new ParallelEpisodePattern(pattern));
			}
		}
		return candidates;
	}

	public List<ParallelEpisodePattern> generateSize1Candidates() {
		return eventAlphabet.stream().map(e -> new ParallelEpisodePattern(createSize1Map(e))).collect(Collectors.toList());
	}

	private Map<AnnotatedEventType, Integer> createSize1Map(AnnotatedEventType e) {
		HashMap<AnnotatedEventType,Integer> map = new HashMap<>();
		map.put(e, 1);
		return map;
	}
	
}
