package trie;

import java.util.Set;

import episode.EventType;
import episode.SerialEpisode;

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
}
