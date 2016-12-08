package episode.pattern.storage;

import java.util.List;

import data.events.CategoricalEventType;

/***
 * Identifier class that is used to unambigously identify episodes in a trie. Essentially implemented via a pointer to their node and their entry index.
 * @author Leon Bornemann
 *
 * @param <T>
 */
public class EpisodeIdentifier<T> {

	private EpisodeTrieNode<T> node;
	private CategoricalEventType entryIndex;

	public EpisodeIdentifier(EpisodeTrieNode<T> node,CategoricalEventType entryIndex){
		this.node = node;
		this.entryIndex = entryIndex;
	}
	
	public List<CategoricalEventType> getCanonicalEpisodeRepresentation(){
		List<CategoricalEventType> events = node.getPathFromRoot();
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
