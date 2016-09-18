package prediction.mining;

import java.util.List;
import java.util.Set;

import episode.finance.mining.ParallelEpisodePatternMiner;
import episode.finance.mining.SerialEpisodePatternMiner;
import episode.finance.storage.EpisodeTrie;
import prediction.data.AnnotatedEventType;
import prediction.data.stream.FixedStreamWindow;
import util.Pair;

public class EpisodeDiscovery {
	
	/***
	 * Returns the episodes that are frequent in the windows. Pair[0] - the frequent serial episodes. Pair[1] - the frequent parallel episodes 
	 * @param windows
	 * @param eventAlphabet
	 * @param s
	 * @return
	 */
	public Pair<EpisodeTrie<List<Boolean>>,EpisodeTrie<List<Boolean>>>  mineFrequentEpisodes(List<FixedStreamWindow> windows, Set<AnnotatedEventType> eventAlphabet,int s){
		SerialEpisodePatternMiner serialMiner = new SerialEpisodePatternMiner(windows, eventAlphabet);
		EpisodeTrie<List<Boolean>> serialPatterns = serialMiner.mineFrequentEpisodePatterns(s);
		ParallelEpisodePatternMiner parallelMiner = new ParallelEpisodePatternMiner(windows, eventAlphabet);
		EpisodeTrie<List<Boolean>> parallelPatterns = parallelMiner.mineFrequentEpisodePatterns(s);
		return new Pair<>(serialPatterns,parallelPatterns);
	}	
	
}
