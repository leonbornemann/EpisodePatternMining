package episode.unstable_experimental_lossy_counting;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import util.Pair;


/***
 * Experimental classes in this package to experiment with the lossy counting algorithm.
 * @author Leon Bornemann
 *
 */
public class DatabaseEpisodePatternMiner{

	private double s;
	private List<Pair<SerialEpisode, Integer>> frequent;
	private List<Pair<EventType, Integer>> source;
	private Set<EventType> eventAlphabet;
	private SerialEpisodePatternGenerator candidateGen;

	public DatabaseEpisodePatternMiner(List<Pair<EventType, Integer>> dataSet, Set<EventType> eventAlphabet, double support) {
		this.source = dataSet;
		this.eventAlphabet = eventAlphabet;
		this.s = support;
		this.frequent = new ArrayList<>();
		this.candidateGen = new SerialEpisodePatternGenerator(eventAlphabet);
	}
	
	public void execute(){
		List<SerialEpisode> candidates = eventAlphabet.stream().map(e -> new SerialEpisode(e)).collect(Collectors.toList());
		while(true){
			List<Pair<SerialEpisode,Integer>> newFrequent = countFrequency(candidates);
			if(newFrequent.isEmpty()){
				break;
			} else{
				addToFrequent(newFrequent);
				candidates = candidateGen.generateNewCandidates(newFrequent.stream().map(e -> getEpisode(e) ).collect(Collectors.toList()));
			}
		}
		outputFrequent();
	}
	
	private SerialEpisode getEpisode(Pair<SerialEpisode, Integer> e) {
		return e.getFirst();
	}

	protected List<Pair<SerialEpisode, Integer>> countFrequency(List<SerialEpisode> candidates) {
		Map<EventType,List<Pair<SerialEpisode,Integer>>> waits = new HashMap<>();
		for(EventType A : eventAlphabet){
			waits.put(A, new ArrayList<>());
		}
		Map<SerialEpisode,Integer> freq = new HashMap<>();
		for(SerialEpisode alpha : candidates){
			waits.get(alpha.get(0)).add(new Pair<SerialEpisode, Integer>(alpha,0));
			freq.put(alpha, 0);
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
					freq.put(alpha, freq.get(alpha)+1);
				}
			}
			waits.put(e_i,new ArrayList<>(bag));
		}
		return candidates.stream().filter(e -> isFrequent(freq.get(e))).map( e -> new Pair<SerialEpisode,Integer>(e,freq.get(e))).collect(Collectors.toList());
	}

	private void assertWaitsCondidtion(Map<EventType, List<Pair<SerialEpisode, Integer>>> waits, int size) {
		int totalSize = waits.values().stream().mapToInt( e -> e.size()).reduce( (a,b) -> a+b).getAsInt();
		assert(size==totalSize);
	}
	
	protected void addToFrequent(List<Pair<SerialEpisode, Integer>> newFrequent) {
		frequent.addAll(newFrequent);
	}

	protected boolean isFrequent(Integer absFrequency) {
		return absFrequency >= source.size()*s;
	}

	protected void outputFrequent() {
		frequent.forEach( t -> System.out.println("Serial Episode " + t.getFirst() + " is frequent with support "+getRelativeSupport(t.getSecond())));
	}

	private double getRelativeSupport(int absoluteSupport) {
		return absoluteSupport / (double)source.size();
	}
}
