package episode.pattern.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.events.CategoricalEventType;
import episode.pattern.ParallelEpisodePattern;
import episode.pattern.storage.EpisodeTrie;

public class ParallelEpisodeCandidatePatternGenerator extends EpisodePatternCandidateGenerator<ParallelEpisodePattern>{

	protected Set<CategoricalEventType> eventAlphabet;

	public ParallelEpisodeCandidatePatternGenerator(Set<CategoricalEventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	protected List<ParallelEpisodePattern> generateCandidates(List<CategoricalEventType> block, List<CategoricalEventType> endings) {
		assert(new HashSet<>(endings).size()==endings.size());
		List<ParallelEpisodePattern> candidates = new ArrayList<>();
		for(int i=0;i<endings.size();i++){
			for(int j=i;j<endings.size();j++){
				List<CategoricalEventType> pattern = new ArrayList<>(block);
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

	private Map<CategoricalEventType, Integer> createSize1Map(CategoricalEventType e) {
		HashMap<CategoricalEventType,Integer> map = new HashMap<>();
		map.put(e, 1);
		return map;
	}

	protected void addToTrie(ParallelEpisodePattern e, EpisodeTrie<List<Boolean>> frequentTrie) {
		assert(!frequentTrie.hasValue(e));
		//TODO: assert size?
		frequentTrie.setValue(e, null);
	}


}
