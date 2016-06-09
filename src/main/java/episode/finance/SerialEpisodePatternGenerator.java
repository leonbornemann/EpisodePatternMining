package episode.finance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.java.tuple.Tuple2;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class SerialEpisodePatternGenerator implements EpisodePatternGenerator<SerialEpisodePattern>{

	protected Set<AnnotatedEventType> eventAlphabet;

	public SerialEpisodePatternGenerator(Set<AnnotatedEventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	/***
	 * Given the frequent Episodes of length k, this will output all possible candidate episodes of size k+1 that may be frequent
	 * @param frequentLengthK a list of frequent epsiodes (must all have the same size)
	 * @return
	 */
	public List<SerialEpisodePattern> generateNewCandidates(List<SerialEpisodePattern> frequentLengthK) {
		int episodeLength = frequentLengthK.get(0).length();
		System.out.println("found " + frequentLengthK.size() +" candidates for length " +episodeLength);
		System.out.println("generating candidates for length " +(episodeLength+1));
		if(episodeLength==1){
			return generateCandidates(new ArrayList<>(),frequentLengthK.stream().map(e -> e.get(0) ).collect(Collectors.toList()));
		}
		Map<List<AnnotatedEventType>,List<AnnotatedEventType>> blocks = new HashMap<>();
		for(SerialEpisodePattern curEpisode : frequentLengthK){
			assert(episodeLength == curEpisode.length());
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
		List<SerialEpisodePattern> candidates = new ArrayList<>();
		blocks.forEach( (k,v) -> candidates.addAll(generateCandidates(k,v)));
		candidates.forEach(e -> assertLengthEquals(e,episodeLength+1));
		return candidates;
	}

	private void assertLengthEquals(SerialEpisodePattern e, int expectedLength) {
		assert(e.length()==expectedLength);
	}

	private List<SerialEpisodePattern> generateCandidates(List<AnnotatedEventType> block, List<AnnotatedEventType> endings) {
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
}
