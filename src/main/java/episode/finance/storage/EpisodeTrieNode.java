package episode.finance.storage;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.Set;
import java.util.TreeMap;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEventType;

public class EpisodeTrieNode<T> {

	Map<AnnotatedEventType,T> values = new TreeMap<>();
	Map<AnnotatedEventType,EpisodeTrieNode<T>> children = new TreeMap<>();
	private AnnotatedEventType myType;
	private EpisodeTrieNode<T> parent;
	
	public EpisodeTrieNode(AnnotatedEventType myType, EpisodeTrieNode<T> parent) {
		this.myType = myType;
		this.parent = parent;
	}

	public EpisodeTrieNode() {
		this.parent = null;
		this.myType = null;
	}

	public void setValue(List<AnnotatedEventType> e, T v) {
		setValue(e,0,v);
	}

	private void setValue(List<AnnotatedEventType> e, int eventIndex, T v) {
		AnnotatedEventType curEventType = e.get(eventIndex);
		if(e.size()-1 == eventIndex){
			values.put(curEventType, v);
		} else if(children.containsKey(curEventType)){
			children.get(curEventType).setValue(e, eventIndex+1, v);
		} else{
			//create new child
			EpisodeTrieNode<T> newChild = new EpisodeTrieNode<T>(curEventType,this);
			children.put(curEventType, newChild);
			newChild.setValue(e, eventIndex+1, v);
		}
	}

	public T getValue(EpisodePattern e) {
		return getValue(e.getCanonicalListRepresentation(),0);
	}

	private T getValue(List<AnnotatedEventType> e, int eventIndex) {
		AnnotatedEventType curEventType = e.get(eventIndex);
		if(e.size()-1 == eventIndex){
			return values.get(curEventType);
		} else if(children.containsKey(curEventType)){
			return children.get(curEventType).getValue(e, eventIndex+1);
		} else{
			return null;
		}
	}

	public List<Entry<AnnotatedEventType, T>> getEntries() {
		return new ArrayList<>(values.entrySet());
	}

	public Collection<? extends EpisodeTrieNode<T>> getChildren() {
		return children.values();
	}

	public boolean hasChildren() {
		return !children.isEmpty();
	}

	public List<AnnotatedEventType> getPathFromRoot() {
		if(parent==null){
			assert(myType==null);
			return new ArrayList<>();
		} else{
			List<AnnotatedEventType> events = parent.getPathFromRoot();
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

	public T getStoredValue(AnnotatedEventType entryIndex) {
		return values.get(entryIndex);
	}

	public void putValue(AnnotatedEventType entryIndex, T val) {
		values.put(entryIndex, val);
	}

	public void deleteValueFromTree(AnnotatedEventType entryIndex) {
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

	private void deleteChild(AnnotatedEventType childIdentifier) {
		children.remove(childIdentifier);
		cleanup();
	}

}
