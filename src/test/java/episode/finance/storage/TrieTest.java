package episode.finance.storage;

import static org.junit.Assert.*;

import java.lang.instrument.Instrumentation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import data.events.CategoricalEventType;
import data.events.Change;
import episode.pattern.ParallelEpisodePattern;
import episode.pattern.storage.EpisodeIdentifier;
import episode.pattern.storage.EpisodeTrie;
import episode.unstable_experimental_lossy_counting.EventType;
import episode.unstable_experimental_lossy_counting.SerialEpisode;
import episode.unstable_experimental_lossy_counting.SerialEpisodeTrie;

public class TrieTest {

	private static CategoricalEventType A = new CategoricalEventType("foo", Change.UP);
	private static CategoricalEventType B = new CategoricalEventType("bar", Change.EQUAL);
	private static CategoricalEventType C = new CategoricalEventType("mystic", Change.DOWN);
	private static CategoricalEventType D = new CategoricalEventType("mystic2", Change.DOWN);
	private static CategoricalEventType E = new CategoricalEventType("mystic3", Change.DOWN);
	private static CategoricalEventType F = new CategoricalEventType("mystic4", Change.DOWN);
	private static CategoricalEventType G = new CategoricalEventType("mystic5", Change.DOWN);
	private static CategoricalEventType H = new CategoricalEventType("mystic6", Change.DOWN);
	private static CategoricalEventType I = new CategoricalEventType("mystic7", Change.DOWN);
	private static CategoricalEventType J = new CategoricalEventType("mystic8", Change.DOWN);
	private static CategoricalEventType K = new CategoricalEventType("mystic9", Change.DOWN);
	
	@Test
	public void addAllNewTest(){
		List<ParallelEpisodePattern> trie1Episodes = Arrays.asList(
				new ParallelEpisodePattern(A),
				new ParallelEpisodePattern(B),
				new ParallelEpisodePattern(A,A)
		);
		List<ParallelEpisodePattern> trie2Episodes = Arrays.asList(
				new ParallelEpisodePattern(A,B,C),
				new ParallelEpisodePattern(B,C),
				new ParallelEpisodePattern(A)
		);
		EpisodeTrie<Integer> trie1 = assignValues(trie1Episodes);
		EpisodeTrie<Integer> trie2 = assignValues(trie2Episodes);
		trie1.addAllNew(trie2);
		int size=0;
		for (EpisodeIdentifier<Integer> episodeIdentifier : trie1) {
			size++;
		}
		assertEquals(5, size);
		assertEquals(0, trie1.getValue(new ParallelEpisodePattern(A)).intValue());
	}
	
	private HashMap<ParallelEpisodePattern, Integer> initPowerset(List<CategoricalEventType> events) {
		HashMap<ParallelEpisodePattern,Integer> allPatterns = new HashMap<>();
		int ansSize = (int)Math.pow(2, events.size());
		for(int i= 0;i< ansSize;++i){
			String bin= Integer.toBinaryString(i); //convert to binary
			while(bin.length() < events.size()){
				bin = "0" + bin; //pad with 0's
			}
			List<CategoricalEventType> thisComb = new ArrayList<>(); //place to put one combination
			for(int j= 0;j< events.size();++j){
				if(bin.charAt(j) == '1'){
					thisComb.add(events.get(j));
				}
			}
			if(!thisComb.isEmpty()){
				allPatterns.put(new ParallelEpisodePattern(thisComb), i);
			}
		}
		return allPatterns;
	}

	private HashMap<ParallelEpisodePattern, Integer> initPowerset(List<CategoricalEventType> eventTypes,List<Integer> sizes) {
		HashMap<ParallelEpisodePattern,Integer> allPatterns = new HashMap<>();
		for(int size:sizes){
			allPatterns.putAll(initEpisodes(eventTypes,size));
		}
		return allPatterns;
	}

	private Map<ParallelEpisodePattern,Integer> initEpisodes(List<CategoricalEventType> eventTypes,int size) {
		// TODO Auto-generated method stub
		return null;
	}

	@Test
	public void test() {
		List<ParallelEpisodePattern> episodesSize1And2 = Arrays.asList(
				new ParallelEpisodePattern(A),
				new ParallelEpisodePattern(B),
				new ParallelEpisodePattern(C),
				new ParallelEpisodePattern(A,A),
				new ParallelEpisodePattern(C,A),
				new ParallelEpisodePattern(C,B),
				new ParallelEpisodePattern(D,D)
		);
		EpisodeTrie<Integer> trie = assignValues(episodesSize1And2);
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,C,C,A)));
		int i = testIterator(episodesSize1And2, trie);
		assertEquals(7,i);
	}
	
	@Test
	public void testNoSize3() {
		List<ParallelEpisodePattern> episodesSize1And2 = Arrays.asList(
				new ParallelEpisodePattern(A),
				new ParallelEpisodePattern(B),
				new ParallelEpisodePattern(C),
				new ParallelEpisodePattern(A,A),
				new ParallelEpisodePattern(C,A),
				new ParallelEpisodePattern(C,B),
				new ParallelEpisodePattern(D,D),
				new ParallelEpisodePattern(D,D,D,D)
		);
		EpisodeTrie<Integer> trie = assignValues(episodesSize1And2);
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,C,C,A)));
		int i = testIterator(episodesSize1And2, trie);
	}
	
	@Test
	public void testOnlySize2() {
		List<ParallelEpisodePattern> episodesSize2 = Arrays.asList(
				new ParallelEpisodePattern(A,A),
				new ParallelEpisodePattern(C,A),
				new ParallelEpisodePattern(C,B),
				new ParallelEpisodePattern(D,D)
		);
		EpisodeTrie<Integer> trie = assignValues(episodesSize2);
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,D)));
		assertTrue(!trie.hasValue(new ParallelEpisodePattern(A,C,C,A)));
		int i = testIterator(episodesSize2, trie);
	}

	private int testIterator(List<ParallelEpisodePattern> episodesSize2, EpisodeTrie<Integer> trie) {
		Iterator<EpisodeIdentifier<Integer>> it = trie.bfsIterator();
		int i=0;
		while(it.hasNext()){
			System.out.println(i);
			EpisodeIdentifier<Integer> entry = it.next();
			ParallelEpisodePattern episode = new ParallelEpisodePattern(entry.getCanonicalEpisodeRepresentation());
			assertEquals(new Integer(i),entry.getAssociatedValue());
			List<CategoricalEventType> canonicalOriginal = episodesSize2.get(i).getCanonicalListRepresentation();
			List<CategoricalEventType> canonicalFromTrie = episode.getCanonicalListRepresentation();
			assertEquals(canonicalOriginal.size(),canonicalFromTrie.size());
			for(int j=0;j<canonicalOriginal.size();j++){
				assertEquals(canonicalOriginal.get(j),canonicalFromTrie.get(j));
			}
			i++;
		}
		return i;
	}

	private EpisodeTrie<Integer> assignValues(List<ParallelEpisodePattern> episodesSize2) {
		EpisodeTrie<Integer> trie = new EpisodeTrie<>();
		for(int i=0;i<episodesSize2.size();i++){
			trie.setValue(episodesSize2.get(i), i);
		}
		for(int i=0;i<episodesSize2.size();i++){
			assertTrue(trie.hasValue(episodesSize2.get(i)));
			assertEquals(new Integer(i),trie.getValue(episodesSize2.get(i)));
		}
		return trie;
	}

}
