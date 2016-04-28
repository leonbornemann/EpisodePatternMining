package trie;

import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

import episode.EventType;
import episode.SerialEpisode;

public class SerialEpisodeTrieNode<T> {

	Map<EventType,T> values = new HashMap<>();
	Map<EventType,SerialEpisodeTrieNode<T>> children = new HashMap<>();
	
	public void setValue(SerialEpisode e, T v) {
		setValue(e,0,v);
	}

	private void setValue(SerialEpisode e, int eventIndex, T v) {
		EventType curEventType = e.get(eventIndex);
		if(e.isLastIndex(eventIndex)){
			values.put(curEventType, v);
		} else if(children.containsKey(curEventType)){
			children.get(curEventType).setValue(e, eventIndex+1, v);
		} else{
			//create new child
			SerialEpisodeTrieNode<T> newChild = new SerialEpisodeTrieNode<T>();
			children.put(curEventType, newChild);
			newChild.setValue(e, eventIndex+1, v);
		}
	}

	public T getValue(SerialEpisode e) {
		return getValue(e,0);
	}

	private T getValue(SerialEpisode e, int eventIndex) {
		EventType curEventType = e.get(eventIndex);
		if(e.isLastIndex(eventIndex)){
			return values.get(curEventType);
		} else if(children.containsKey(curEventType)){
			return children.get(curEventType).getValue(e, eventIndex+1);
		} else{
			return null;
		}
	}

}
