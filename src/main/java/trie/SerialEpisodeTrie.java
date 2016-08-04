package trie;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;

import episode.basic.EventType;
import episode.basic.SerialEpisode;

public class SerialEpisodeTrie<T> {

	private SerialEpisodeTrieNode<T> root;

	public SerialEpisodeTrie(Set<EventType> eventAlphabet){
		this.root = new SerialEpisodeTrieNode<T>();
	}
	
	public void setValue(SerialEpisode e,T v){
		root.setValue(e,v);
	}
	
	public T getValue(SerialEpisode e){
		return root.getValue(e);
	}
	
	public boolean hasValue(SerialEpisode e){
		return root.getValue(e)!=null;
	}

	public Iterator<Entry<SerialEpisode, T>> bfsIterator() {
		return new BFSTrieIterator<T>(root);
	}
}
