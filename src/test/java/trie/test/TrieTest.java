package trie.test;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.junit.Test;

import episode.lossy_counting.EventType;
import episode.lossy_counting.SerialEpisode;
import trie.SerialEpisodeTrie;

public class TrieTest {

	@Test
	public void test() {
		List<SerialEpisode> episodesSize1And2 = Arrays.asList(
				new SerialEpisode(new EventType('A')),
				new SerialEpisode(new EventType('B')),
				new SerialEpisode(new EventType('C')),
				new SerialEpisode(new EventType('A'),new EventType('A')),
				new SerialEpisode(new EventType('C'),new EventType('A')),
				new SerialEpisode(new EventType('C'),new EventType('B'))
		);
		SerialEpisodeTrie<Integer> trie = new SerialEpisodeTrie<>(getEventAlphabet());
		for(int i=0;i<episodesSize1And2.size();i++){
			trie.setValue(episodesSize1And2.get(i), i);
		}
		for(int i=0;i<episodesSize1And2.size();i++){
			assertTrue(trie.hasValue(episodesSize1And2.get(i)));
			assertEquals(new Integer(i),trie.getValue(episodesSize1And2.get(i)));
		}
		assertTrue(!trie.hasValue(new SerialEpisode(new EventType('A'),new EventType('C'))));
		assertTrue(!trie.hasValue(new SerialEpisode(new EventType('A'),new EventType('C'),new EventType('C'),new EventType('A'))));
		Iterator<Entry<SerialEpisode, Integer>> it = trie.bfsIterator();
		int i=0;
		while(it.hasNext()){
			System.out.println(i);
			Entry<SerialEpisode, Integer> entry = it.next();
			SerialEpisode episode = entry.getKey();
			assertEquals(new Integer(i),entry.getValue());
			assertEquals(episodesSize1And2.get(i).getLength(),episode.getLength());
			for(int j=0;j<episode.getLength();j++){
				assertEquals(episodesSize1And2.get(i).get(j),episode.get(j));
			}
			i++;
		}
		assertEquals(6,i);
	}

	private Set<EventType> getEventAlphabet() {
		HashSet<EventType> a = new HashSet<EventType>();
		a.addAll(Arrays.asList(new EventType('A'),new EventType('B'),new EventType('C')));
		return a;
	}

}
