package episode.finance.storage;

import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Queue;

import prediction.data.AnnotatedEventType;

public class BFSTrieIterator<T> implements Iterator<EpisodeIdentifier<T>> {

	private EpisodeTrieNode<T> curNode;
	private List<Entry<AnnotatedEventType,T>> curEntries;
	private int entryIndex;
	private Queue<EpisodeTrieNode<T>> queue = new ArrayDeque<EpisodeTrieNode<T>>();
	private boolean done = false;

	public BFSTrieIterator(EpisodeTrieNode<T> root) {
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
		if(entryIndex==curEntries.size()){
			incToNext();
		}
	}

	@Override
	public boolean hasNext() {
		return !done;
	}

	@Override
	public EpisodeIdentifier<T> next() {
		if(hasNext()){
			EpisodeIdentifier<T> toReturn = new EpisodeIdentifier<>(curNode,  curEntries.get(entryIndex).getKey());
			if(hasNext()){
				incToNext();
			}
			return toReturn;
		} else{
			throw new NoSuchElementException();
		}
	}

}
