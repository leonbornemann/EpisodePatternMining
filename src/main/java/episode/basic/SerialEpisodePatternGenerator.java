package episode.basic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.java.tuple.Tuple2;

public class SerialEpisodePatternGenerator {

	protected Set<EventType> eventAlphabet;

	public SerialEpisodePatternGenerator(Set<EventType> eventAlphabet) {
		this.eventAlphabet = eventAlphabet;
	}

	/***
	 * Given the frequent Episodes of length k, this will output all possible candidate episodes of size k+1 that may be frequent
	 * @param frequentLengthK a list of frequent epsiodes (must all have the same size)
	 * @return
	 */
	public List<SerialEpisode> generateNewCandidates(List<SerialEpisode> frequentLengthK) {
		int episodeLength = frequentLengthK.get(0).getLength();
		System.out.println("found " + frequentLengthK.size() +" candidates for length " +episodeLength);
		System.out.println("generating candidates for length " +(episodeLength+1));
		if(episodeLength==1){
			return generateCandidates(new ArrayList<>(),frequentLengthK.stream().map(e -> e.get(0) ).collect(Collectors.toList()));
		}
		Map<List<EventType>,List<EventType>> blocks = new HashMap<>();
		for(SerialEpisode curEpisode : frequentLengthK){
			assert(episodeLength == curEpisode.getLength());
			List<EventType> block = curEpisode.subList(0,episodeLength-1);
			EventType lastEvent = curEpisode.get(episodeLength-1);
			if(blocks.containsKey(block)){
				assert(!blocks.get(block).contains(lastEvent));
				blocks.get(block).add(lastEvent);
			} else{
				List<EventType> endings = new ArrayList<>();
				endings.add(lastEvent);
				blocks.put(block,endings);
			}
		}
		List<SerialEpisode> candidates = new ArrayList<>();
		blocks.forEach( (k,v) -> candidates.addAll(generateCandidates(k,v)));
		candidates.forEach(e -> assertLengthEquals(e,episodeLength+1));
		return candidates;
	}

	private void assertLengthEquals(SerialEpisode e, int expectedLength) {
		assert(e.getLength()==expectedLength);
	}

	private List<SerialEpisode> generateCandidates(List<EventType> block, List<EventType> endings) {
		assert(new HashSet<>(endings).size()==endings.size());
		List<SerialEpisode> candidates = new ArrayList<>();
		for(int i=0;i<endings.size();i++){
			for(int j=i;j<endings.size();j++){
				SerialEpisode c1 = new SerialEpisode(new ArrayList<>(block));
				c1.addEventType(endings.get(i));
				c1.addEventType(endings.get(j));
				candidates.add(c1);
				if(i!=j){
					SerialEpisode c2 = new SerialEpisode(new ArrayList<>(block));
					c2.addEventType(endings.get(j));
					c2.addEventType(endings.get(i));
					candidates.add(c2);
				}
			}
		}
		return candidates;
	}

	public List<SerialEpisode> generateSize1Candidates() {
		return eventAlphabet.stream().map(e -> new SerialEpisode(e)).collect(Collectors.toList());
	}
}
