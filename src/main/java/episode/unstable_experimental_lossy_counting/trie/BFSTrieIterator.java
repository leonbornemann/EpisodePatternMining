package episode.unstable_experimental_lossy_counting.trie;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import episode.unstable_experimental_lossy_counting.EventType;
import episode.unstable_experimental_lossy_counting.SerialEpisode;

import java.util.NoSuchElementException;
import java.util.Queue;

public class BFSTrieIterator<T> implements Iterator<java.util.Map.Entry<episode.unstable_experimental_lossy_counting.SerialEpisode, T>> {

	private SerialEpisodeTrieNode<T> curNode;
	private List<Entry<EventType,T>> curEntries;
	private int entryIndex;
	private Queue<SerialEpisodeTrieNode<T>> queue = new ArrayDeque<SerialEpisodeTrieNode<T>>();
	private boolean done = false;

	public BFSTrieIterator(SerialEpisodeTrieNode<T> root) {
		queue.add(root);
		incToNextNode();
	}

	private void incToNext() {
		if(entryIndex<curEntries.size()-1){
			entryIndex++;
		} else if(!queue.isEmpty()){
			incToNextNode();
		} else{
			done = true;
		}
	}

	private void incToNextNode() {
		assert(!queue.isEmpty());
		curNode = queue.poll();
		curEntries = curNode.getEntries();
		entryIndex = 0;
		if(curNode.hasChildren()){
			queue.addAll(curNode.getChildren());
		}
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public Entry<SerialEpisode, T> next() {
		if(hasNext()){
			Entry<EventType, T> entry = curEntries.get(entryIndex);
			List<EventType> events = curNode.getPathFromRoot();
			events.add(entry.getKey());
			SerialEpisode episode = new SerialEpisode(events);
			Entry<SerialEpisode,T> toReturn = new MyEntry<>(episode,entry.getValue());
			if(hasNext()){
				incToNext();
			}
			return toReturn;
		} else{
			throw new NoSuchElementException();
		}
	}

}
