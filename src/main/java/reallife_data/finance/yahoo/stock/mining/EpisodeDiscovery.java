package reallife_data.finance.yahoo.stock.mining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import episode.finance.ParallelEpisodePatternMiner;
import episode.finance.SerialEpisodePatternMiner;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class EpisodeDiscovery {
	
	public Map<EpisodePattern,List<Boolean>>  mineFrequentEpisodes(List<StreamWindow> windows, Set<AnnotatedEventType> eventAlphabet,int s){
		Map<EpisodePattern,List<Boolean>> frequentPatterns = new HashMap<>();
		SerialEpisodePatternMiner serialMiner = new SerialEpisodePatternMiner(windows, eventAlphabet);
		frequentPatterns.putAll(serialMiner.mineFrequentEpisodePatterns(s));
		ParallelEpisodePatternMiner parallelMiner = new ParallelEpisodePatternMiner(windows, eventAlphabet);
		frequentPatterns.putAll(parallelMiner.mineFrequentEpisodePatterns(s));
		return frequentPatterns;
	}	
	
}
