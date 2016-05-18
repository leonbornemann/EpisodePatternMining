package trie;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;

import org.apache.commons.collections.iterators.EntrySetMapIterator;

import episode.basic.EventType;
import episode.basic.SerialEpisode;

public class BFSTrieIterator<T> implements Iterator<java.util.Map.Entry<episode.basic.SerialEpisode, T>> {

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
