package episode.pattern.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.events.CategoricalEventType;

public class EpisodeTrieNode<T> {

	Map<CategoricalEventType,T> values = new TreeMap<>();
	Map<CategoricalEventType,EpisodeTrieNode<T>> children = new TreeMap<>();
	private CategoricalEventType myType;
	private EpisodeTrieNode<T> parent;
	
	public EpisodeTrieNode(CategoricalEventType myType, EpisodeTrieNode<T> parent) {
		this.myType = myType;
		this.parent = parent;
	}

	public EpisodeTrieNode() {
		this.parent = null;
		this.myType = null;
	}

	public void setValue(List<CategoricalEventType> e, T v) {
		setValue(e,0,v);
	}

	private void setValue(List<CategoricalEventType> canonicalRepresentation, int eventIndex, T v) {
		CategoricalEventType curEventType = canonicalRepresentation.get(eventIndex);
		if(canonicalRepresentation.size()-1 == eventIndex){
			values.put(curEventType, v);
		} else if(children.containsKey(curEventType)){
			children.get(curEventType).setValue(canonicalRepresentation, eventIndex+1, v);
		} else{
			//create new child
			EpisodeTrieNode<T> newChild = new EpisodeTrieNode<T>(curEventType,this);
			children.put(curEventType, newChild);
			newChild.setValue(canonicalRepresentation, eventIndex+1, v);
		}
	}

	public T getValue(List<CategoricalEventType> canonicalRepresentation) {
		return getValue(canonicalRepresentation,0);
	}

	private T getValue(List<CategoricalEventType> e, int eventIndex) {
		CategoricalEventType curEventType = e.get(eventIndex);
		if(e.size()-1 == eventIndex){
			return values.get(curEventType);
		} else if(children.containsKey(curEventType)){
			return children.get(curEventType).getValue(e, eventIndex+1);
		} else{
			return null;
		}
	}

	public List<Entry<CategoricalEventType, T>> getEntries() {
		return new ArrayList<>(values.entrySet());
	}

	public Collection<? extends EpisodeTrieNode<T>> getChildren() {
		return children.values();
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public List<CategoricalEventType> getPathFromRoot() {
		if(parent==null){
			assert(myType==null);
			return new ArrayList<>();
		} else{
			List<CategoricalEventType> events = parent.getPathFromRoot();
			events.add(myType);
			return events;
		}
	}

	public Set<EpisodeIdentifier<T>> getAllOfRemainingLength(int episodeLength) {
		if(episodeLength==1){
			return values.keySet().stream().map( e -> new EpisodeIdentifier<>(this, e)).collect(Collectors.toSet());
		} else{
			Set<EpisodeIdentifier<T>> recursive = new HashSet<>();
			for(EpisodeTrieNode<T> child : children.values()){
				recursive.addAll(child.getAllOfRemainingLength(episodeLength-1));
			}
			return recursive;
			//return children.values().stream().flatMap(n -> n.getAllOfRemainingLength(episodeLength-1).stream()).collect(Collectors.toSet());
		}
	}

	public T getStoredValue(CategoricalEventType entryIndex) {
		return values.get(entryIndex);
	}

	public void putValue(CategoricalEventType entryIndex, T val) {
		values.put(entryIndex, val);
	}

	public void deleteValueFromTree(CategoricalEventType entryIndex) {
		assert(values.containsKey(entryIndex));
		values.remove(entryIndex);
		cleanup();
	}

	private void cleanup() {
		if(children.isEmpty() && values.isEmpty() && parent !=null){
			//delete ourselves in parent:
			parent.deleteChild(myType);
			parent = null;
		}
	}

	private void deleteChild(CategoricalEventType childIdentifier) {
		children.remove(childIdentifier);
		cleanup();
	}

}
