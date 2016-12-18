package episode.unstable_experimental_lossy_counting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


import util.Pair;

public class LossyEpisodeMiner {

	private List<Pair<EventType, Integer>> source;
	private int curEndBucketNum;
	private SerialEpisodeTrie<FrequencyListElement> frequencyList;
	private SerialEpisodePatternGenerator patternGen;
	private int curStartBucketNum;
	private Set<EventType> eventAlphabet;

	public LossyEpisodeMiner(List<Pair<EventType, Integer>> source,int curStartBucketNum, int curEndBucketNum,SerialEpisodeTrie<FrequencyListElement> frequencyList, Set<EventType> eventAlphabet) {
		System.out.println("creating Miner for buckets " +curStartBucketNum + "-" +curEndBucketNum);
		this.source = source;
		this.curStartBucketNum = curStartBucketNum;
		this.curEndBucketNum = curEndBucketNum;
		this.frequencyList = frequencyList;
		this.patternGen = new SerialEpisodePatternGenerator(eventAlphabet);
		this.eventAlphabet = eventAlphabet;
	}
	
	public void mine() {
		List<SerialEpisode> candidates = patternGen.generateSize1Candidates();
		while(true){
			List<SerialEpisode> newFrequent = countFrequency(candidates);
			if(newFrequent.isEmpty()){
				break;
			} else{
				candidates = patternGen.generateNewCandidates(newFrequent);
			}
		}
	}
	
	protected List<SerialEpisode> countFrequency(List<SerialEpisode> candidates) {
		Map<EventType,List<Pair<SerialEpisode,Integer>>> waits = new HashMap<>();
		for(EventType A : eventAlphabet){
			waits.put(A, new ArrayList<>());
		}
		for(SerialEpisode alpha : candidates){
			waits.get(alpha.get(0)).add(new Pair<SerialEpisode, Integer>(alpha,0));
		}
		for(int i=0;i<source.size();i++){
			assertWaitsCondidtion(waits,candidates.size());
			Set<Pair<SerialEpisode,Integer>> bag = new HashSet<>();
			EventType e_i= source.get(i).getFirst();
			for(Pair<SerialEpisode,Integer> cur : waits.get(e_i)){
				SerialEpisode alpha = cur.getFirst();
				Integer j = cur.getSecond();
				int j_new = j+1;
				if(j_new == alpha.getLength()){
					j_new = 0;
				}
				if(alpha.get(j_new).equals(e_i)){
					bag.add(new Pair<SerialEpisode,Integer>(alpha,j_new));
				} else{
					waits.get(alpha.get(j_new)).add(new Pair<SerialEpisode,Integer>(alpha,j_new));
				}
				if(alpha.isLastIndex(j)){
					if(frequencyList.hasValue(alpha)){
						frequencyList.getValue(alpha).incFreq();
					} else{
						frequencyList.setValue(alpha, new FrequencyListElement(1, curStartBucketNum-1));
					}
				}
			}
			waits.put(e_i,new ArrayList<>(bag));
		}
		return candidates.stream().filter(e -> isFrequent(e)).collect(Collectors.toList());
	}

	private void assertWaitsCondidtion(Map<EventType, List<Pair<SerialEpisode, Integer>>> waits, int size) {
		int totalSize = waits.values().stream().mapToInt( e -> e.size()).reduce( (a,b) -> a+b).getAsInt();
		assert(size==totalSize);
	}

	protected boolean isFrequent(SerialEpisode e) {
		FrequencyListElement elem = frequencyList.getValue(e);
		boolean isFrequent = elem.getFreq()+elem.getDelta() > curEndBucketNum;
		if(isFrequent){
			System.out.println("Episode " + e + " is frequent with at least "+elem.getFreq() + " total occurances");
		}
		return isFrequent;
	}

}
