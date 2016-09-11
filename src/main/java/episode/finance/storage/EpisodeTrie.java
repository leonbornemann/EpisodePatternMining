package episode.finance.storage;

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import episode.finance.EpisodePattern;
import prediction.data.stream.FixedStreamWindow;

public class EpisodeTrie<T> {

	private EpisodeTrieNode<T> root;
	private int size =0;
	
	public EpisodeTrie(){
		this.root = new EpisodeTrieNode<T>();
	}
	
	public void setValue(EpisodePattern e,T v){
		root.setValue(e.getCanonicalListRepresentation(),v);
		size++;
	}
	
	public T getValue(EpisodePattern e){
		return root.getValue(e);
	}
	
	public boolean hasValue(EpisodePattern e){
		return root.getValue(e)!=null;
	}

	public Iterator<EpisodeIdentifier<T>> bfsIterator() {
		return new BFSTrieIterator<T>(root);
	}

	public Iterator<EpisodeIdentifier<T>> filteredIterator() {
		return new BFSTrieIterator<T>(root);
	}

	public Set<EpisodeIdentifier<T>> getAllOfSize(int episodeLength) {
		Set<EpisodeIdentifier<T>> allOfSize = root.getAllOfRemainingLength(episodeLength);
		return allOfSize;
	}

	public Stream<EpisodeIdentifier<T>> stream() {
		return StreamSupport.stream(Spliterators.spliteratorUnknownSize(bfsIterator(), Spliterator.ORDERED),false);
	}
}
