package episode.finance.storage;

import java.util.List;

import data.AnnotatedEventType;

public class EpisodeIdentifier<T> {

	private EpisodeTrieNode<T> node;
	private AnnotatedEventType entryIndex;

	public EpisodeIdentifier(EpisodeTrieNode<T> node,AnnotatedEventType entryIndex){
		this.node = node;
		this.entryIndex = entryIndex;
	}
	
	public List<AnnotatedEventType> getCanonicalEpisodeRepresentation(){
		List<AnnotatedEventType> events = node.getPathFromRoot();
		events.add(entryIndex);
		return events;
	}
	
	public T getAssociatedValue(){
		return node.getStoredValue(entryIndex);
	}
	
	@Override
	public int hashCode(){
		return node.hashCode()*entryIndex.hashCode()*7;
	}
	
	@Override
	public boolean equals(Object o){
		if(o instanceof EpisodeIdentifier<?>){
			EpisodeIdentifier<?> other = (EpisodeIdentifier<?>) o;
			return node.equals(other.node) && entryIndex.equals(other.entryIndex);
		} else{
			return false;
		}
	}

	public void setAssociatedValue(T val) {
		node.putValue(entryIndex,val);
	}

	public void deleteElement() {
		node.deleteValueFromTree(entryIndex);
	}
}
