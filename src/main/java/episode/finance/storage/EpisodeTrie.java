package episode.finance.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import data.AnnotatedEventType;
import episode.finance.EpisodePattern;

public class EpisodeTrie<T> implements Iterable<EpisodeIdentifier<T>>{

	private EpisodeTrieNode<T> root;
	private int size =0;
	
	public EpisodeTrie(){
		this.root = new EpisodeTrieNode<T>();
	}
	
	public void setValue(EpisodePattern e,T v){
		setValue(e.getCanonicalListRepresentation(),v);
		size++;
	}
	
	private void setValue(List<AnnotatedEventType> canonicalListRepresentation, T v) {
		root.setValue(canonicalListRepresentation,v);
	}

	public T getValue(EpisodePattern e){
		return root.getValue(e.getCanonicalListRepresentation());
	}
	
	public boolean hasValue(EpisodePattern e){
		return hasValue(e.getCanonicalListRepresentation());
	}

	private boolean hasValue(List<AnnotatedEventType> canonicalListRepresentation) {
		return root.getValue(canonicalListRepresentation)!=null;
	}

	public Iterator<EpisodeIdentifier<T>> bfsIterator() {
		return new BFSTrieIterator<T>(root);
	}

	public Set<EpisodeIdentifier<T>> getAllOfSize(int episodeLength) {
		Set<EpisodeIdentifier<T>> allOfSize = root.getAllOfRemainingLength(episodeLength);
		return allOfSize;
	}

	public Stream<EpisodeIdentifier<T>> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(bfsIterator(), Spliterator.ORDERED),false);
	}

	public void addAllNew(EpisodeTrie<T> other) {
		for (EpisodeIdentifier<T> episodeIdentifier : other) {
			List<AnnotatedEventType> canonicalEpisodeRepresentation = episodeIdentifier.getCanonicalEpisodeRepresentation();
			if(!hasValue(canonicalEpisodeRepresentation)){
				setValue(canonicalEpisodeRepresentation, episodeIdentifier.getAssociatedValue());
			}
		}
	}

	@Override
	public Iterator<EpisodeIdentifier<T>> iterator() {
		return bfsIterator();
	}
}
