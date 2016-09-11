package episode.finance.mining;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.ParallelEpisodePattern;
import episode.finance.SerialEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import episode.finance.storage.EpisodeTrie;
import prediction.data.AnnotatedEventType;

public class SerialEpisodePatternGenerator extends EpisodePatternGenerator<SerialEpisodePattern>{

	protected Set<AnnotatedEventType> eventAlphabet;

	public SerialEpisodePatternGenerator(Set<AnnotatedEventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	protected List<SerialEpisodePattern> generateCandidates(List<AnnotatedEventType> block, List<AnnotatedEventType> endings) {
		assert(new HashSet<>(endings).size()==endings.size());
		List<SerialEpisodePattern> candidates = new ArrayList<>();
		for(int i=0;i<endings.size();i++){
			for(int j=i;j<endings.size();j++){
				SerialEpisodePattern c1 = new SerialEpisodePattern(new ArrayList<>(block));
				c1.addEventType(endings.get(i));
				c1.addEventType(endings.get(j));
				candidates.add(c1);
				if(i!=j){
					SerialEpisodePattern c2 = new SerialEpisodePattern(new ArrayList<>(block));
					c2.addEventType(endings.get(j));
					c2.addEventType(endings.get(i));
					candidates.add(c2);
				}
			}
		}
		return candidates;
	}

	public List<SerialEpisodePattern> generateSize1Candidates() {
		return eventAlphabet.stream().map(e -> new SerialEpisodePattern(e)).collect(Collectors.toList());
	}

	protected void addToTrie(SerialEpisodePattern e, EpisodeTrie<List<Boolean>> frequentTrie) {
		assert(!frequentTrie.hasValue(e));
		//TODO: assert size?
		frequentTrie.setValue(e, null);
	}
}
