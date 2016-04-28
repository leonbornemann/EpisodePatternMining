package episode;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.tuple.Tuple2;

public class EpisodePatternMiner {

	private double s;
	private List<Tuple2<EventType, Integer>> source;
	private Set<EventType> eventAlphabet;
	List<Tuple2<SerialEpisode,Integer>> frequent;

	public EpisodePatternMiner(List<Tuple2<EventType, Integer>> dataSet, double support, Set<EventType> eventAlphabet) {
		this.s = support;
		this.source = dataSet;
		this.eventAlphabet = eventAlphabet;
		frequent = new ArrayList<>();
	}
	
	public void execute(){
		List<SerialEpisode> candidates = eventAlphabet.stream().map(e -> new SerialEpisode(e)).collect(Collectors.toList());
		while(true){
			List<Tuple2<SerialEpisode,Integer>> newFrequent = countFrequency(candidates);
			if(newFrequent.isEmpty()){
				break;
			} else{
				frequent.addAll(newFrequent);
				candidates = generateCandidates(newFrequent);
			}
		}
		outputFrequent();
	}

	private List<SerialEpisode> generateCandidates(List<Tuple2<SerialEpisode, Integer>> oldFrequent) {
		//split to blocks
		//TODO: case for size 1
		int episodeLength = ((SerialEpisode) oldFrequent.get(0).getField(0)).getLength();
		System.out.println("found " + oldFrequent.size() +" candidates for length " +episodeLength);
		System.out.println("generating candidates for length " +(episodeLength+1));
		if(episodeLength==1){
			return generateCandidates(new ArrayList<>(),oldFrequent.stream().map(e -> ((SerialEpisode) e.getField(0)).get(0)).collect(Collectors.toList()));
		}
		Map<List<EventType>,List<EventType>> blocks = new HashMap<>();
		for(Tuple2<SerialEpisode, Integer> a : oldFrequent){
			SerialEpisode curEpisode = a.getField(0);
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

	private List<Tuple2<SerialEpisode, Integer>> countFrequency(List<SerialEpisode> candidates) {
		Map<EventType,List<Tuple2<SerialEpisode,Integer>>> waits = new HashMap<>();
		for(EventType A : eventAlphabet){
			waits.put(A, new ArrayList<>());
		}
		Map<SerialEpisode,Integer> freq = new HashMap<>();
		for(SerialEpisode alpha : candidates){
			waits.get(alpha.get(0)).add(new Tuple2<SerialEpisode, Integer>(alpha,0));
			freq.put(alpha, 0);
		}
		for(int i=0;i<source.size();i++){
			assertWaitsCondidtion(waits,candidates.size());
			Set<Tuple2<SerialEpisode,Integer>> bag = new HashSet<>();
			EventType e_i= source.get(i).getField(0);
			for(Tuple2<SerialEpisode,Integer> cur : waits.get(e_i)){
				SerialEpisode alpha = cur.getField(0);
				Integer j = cur.getField(1);
				int j_new = j+1;
				if(j_new == alpha.getLength()){
					j_new = 0;
				}
				if(alpha.get(j_new).equals(e_i)){
					bag.add(new Tuple2<SerialEpisode,Integer>(alpha,j_new));
				} else{
					waits.get(alpha.get(j_new)).add(new Tuple2<SerialEpisode,Integer>(alpha,j_new));
				}
				if(alpha.isLastIndex(j)){
					freq.put(alpha, freq.get(alpha)+1);
				}
			}
			waits.put(e_i,new ArrayList<>(bag));
		}
		return candidates.stream().filter(e -> freq.get(e)>= source.size()*s).map( e -> new Tuple2<SerialEpisode,Integer>(e,freq.get(e))).collect(Collectors.toList());
	}

	private void assertWaitsCondidtion(Map<EventType, List<Tuple2<SerialEpisode, Integer>>> waits, int size) {
		int totalSize = waits.values().stream().mapToInt( e -> e.size()).reduce( (a,b) -> a+b).getAsInt();
		assert(size==totalSize);
	}

	private void outputFrequent() {
		frequent.forEach( t -> System.out.println("Serial Episode " + t.getField(0) + " is frequent with support "+getRelativeSupport(t.getField(1))));
	}

	private double getRelativeSupport(int absoluteSupport) {
		return absoluteSupport / (double)source.size();
	}
}
