package episode.unstable_experimental_lossy_counting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.flink.api.java.tuple.Tuple2;

/***
 * Experimental classes in this package to experiment with the lossy counting algorithm.
 * @author Leon Bornemann
 *
 */
public class DatabaseEpisodePatternMiner{

	private double s;
	private List<Tuple2<SerialEpisode, Integer>> frequent;
	private List<Tuple2<EventType, Integer>> source;
	private Set<EventType> eventAlphabet;
	private SerialEpisodePatternGenerator candidateGen;

	public DatabaseEpisodePatternMiner(List<Tuple2<EventType, Integer>> dataSet, Set<EventType> eventAlphabet, double support) {
		this.source = dataSet;
		this.eventAlphabet = eventAlphabet;
		this.s = support;
		this.frequent = new ArrayList<>();
		this.candidateGen = new SerialEpisodePatternGenerator(eventAlphabet);
	}
	
	public void execute(){
		List<SerialEpisode> candidates = eventAlphabet.stream().map(e -> new SerialEpisode(e)).collect(Collectors.toList());
		while(true){
			List<Tuple2<SerialEpisode,Integer>> newFrequent = countFrequency(candidates);
			if(newFrequent.isEmpty()){
				break;
			} else{
				addToFrequent(newFrequent);
				candidates = candidateGen.generateNewCandidates(newFrequent.stream().map(e -> getEpisode(e) ).collect(Collectors.toList()));
			}
		}
		outputFrequent();
	}
	
	private SerialEpisode getEpisode(Tuple2<SerialEpisode, Integer> e) {
		return e.getField(0);
	}

	protected List<Tuple2<SerialEpisode, Integer>> countFrequency(List<SerialEpisode> candidates) {
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
		return candidates.stream().filter(e -> isFrequent(freq.get(e))).map( e -> new Tuple2<SerialEpisode,Integer>(e,freq.get(e))).collect(Collectors.toList());
	}

	private void assertWaitsCondidtion(Map<EventType, List<Tuple2<SerialEpisode, Integer>>> waits, int size) {
		int totalSize = waits.values().stream().mapToInt( e -> e.size()).reduce( (a,b) -> a+b).getAsInt();
		assert(size==totalSize);
	}
	
	protected void addToFrequent(List<Tuple2<SerialEpisode, Integer>> newFrequent) {
		frequent.addAll(newFrequent);
	}

	protected boolean isFrequent(Integer absFrequency) {
		return absFrequency >= source.size()*s;
	}

	protected void outputFrequent() {
		frequent.forEach( t -> System.out.println("Serial Episode " + t.getField(0) + " is frequent with support "+getRelativeSupport(t.getField(1))));
	}

	private double getRelativeSupport(int absoluteSupport) {
		return absoluteSupport / (double)source.size();
	}
}
