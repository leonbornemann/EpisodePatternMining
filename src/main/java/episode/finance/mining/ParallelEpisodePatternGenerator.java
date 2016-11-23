package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.AnnotatedEventType;
import episode.finance.ParallelEpisodePattern;
import episode.finance.storage.EpisodeTrie;

public class ParallelEpisodePatternGenerator extends EpisodePatternGenerator<ParallelEpisodePattern>{

	protected Set<AnnotatedEventType> eventAlphabet;

	public ParallelEpisodePatternGenerator(Set<AnnotatedEventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	protected List<ParallelEpisodePattern> generateCandidates(List<AnnotatedEventType> block, List<AnnotatedEventType> endings) {
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

	protected void addToTrie(ParallelEpisodePattern e, EpisodeTrie<List<Boolean>> frequentTrie) {
		assert(!frequentTrie.hasValue(e));
		//TODO: assert size?
		frequentTrie.setValue(e, null);
	}


}
