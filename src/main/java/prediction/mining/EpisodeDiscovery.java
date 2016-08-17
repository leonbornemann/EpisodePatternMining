package prediction.mining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import episode.finance.mining.ParallelEpisodePatternMiner;
import episode.finance.mining.SerialEpisodePatternMiner;
import prediction.data.AnnotatedEventType;
import prediction.data.stream.FixedStreamWindow;

public class EpisodeDiscovery {
	
	public Map<EpisodePattern,List<Boolean>>  mineFrequentEpisodes(List<FixedStreamWindow> windows, Set<AnnotatedEventType> eventAlphabet,int s){
		Map<EpisodePattern,List<Boolean>> frequentPatterns = new HashMap<>();
		SerialEpisodePatternMiner serialMiner = new SerialEpisodePatternMiner(windows, eventAlphabet);
		frequentPatterns.putAll(serialMiner.mineFrequentEpisodePatterns(s));
		ParallelEpisodePatternMiner parallelMiner = new ParallelEpisodePatternMiner(windows, eventAlphabet);
		frequentPatterns.putAll(parallelMiner.mineFrequentEpisodePatterns(s));
		return frequentPatterns;
	}	
	
}
