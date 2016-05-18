package trie;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import episode.basic.EventType;
import episode.basic.SerialEpisode;

import java.util.TreeMap;

public class SerialEpisodeTrieNode<T> {

	Map<EventType,T> values = new TreeMap<>();
	Map<EventType,SerialEpisodeTrieNode<T>> children = new TreeMap<>();
	private EventType myType;
	private SerialEpisodeTrieNode<T> parent;
	
	public SerialEpisodeTrieNode(EventType myType, SerialEpisodeTrieNode<T> parent) {
		this.myType = myType;
		this.parent = parent;
	}

	public SerialEpisodeTrieNode() {
		this.parent = null;
		this.myType = null;
	}

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
			SerialEpisodeTrieNode<T> newChild = new SerialEpisodeTrieNode<T>(curEventType,this);
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

	public List<Entry<EventType, T>> getEntries() {
		return new ArrayList<>(values.entrySet());
	}

	public Collection<? extends SerialEpisodeTrieNode<T>> getChildren() {
		return children.values();
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public List<EventType> getPathFromRoot() {
		if(parent==null){
			assert(myType==null);
			return new ArrayList<>();
		} else{
			List<EventType> events = parent.getPathFromRoot();
			events.add(myType);
			return events;
		}
	}

}
