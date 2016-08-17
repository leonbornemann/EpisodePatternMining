package prediction.mining;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import episode.finance.mining.ParallelEpisodePatternMiner;
import episode.finance.mining.SerialEpisodePatternMiner;
import prediction.data.AnnotatedEventType;
import prediction.data.stream.FixedStreamWindow;

public class PredictiveMiner {

	private int n;
	private int s;
	private Set<AnnotatedEventType> eventAlphabet;
	private WindowMiner miner;

	public PredictiveMiner(WindowMiner miner,Set<AnnotatedEventType> eventAlphabet, int s,int n) throws IOException {
		this.n = n;
		this.s = s;
		this.eventAlphabet =eventAlphabet;
		this.miner = miner;
	}
		
	public Map<EpisodePattern,Integer> getInitialPreditiveEpisodes() throws IOException{
		return mineEpisodes(miner.getPredictiveWindows(),miner.getInversePredictiveWindows(),miner.getNeutralWindows());
	}

	private Map<EpisodePattern, Integer> mineEpisodes(List<FixedStreamWindow> predictiveWindows,List<FixedStreamWindow> inversePredictiveWindows,List<FixedStreamWindow> nothingWindows) {
		SerialEpisodePatternMiner serialEpisodeMiner = new SerialEpisodePatternMiner(predictiveWindows, eventAlphabet);
		ParallelEpisodePatternMiner parallelEpisodeMiner = new ParallelEpisodePatternMiner(predictiveWindows, eventAlphabet);
		Map<EpisodePattern,Integer> initialPredictiveEpisodes = new HashMap<>();
		initialPredictiveEpisodes.putAll(serialEpisodeMiner.mineBestEpisodePatterns(s, n, inversePredictiveWindows,nothingWindows));
		initialPredictiveEpisodes.putAll(parallelEpisodeMiner.mineBestEpisodePatterns(s, n, inversePredictiveWindows,nothingWindows));
		return initialPredictiveEpisodes;
	}

	public Map<EpisodePattern, Integer> getInitialInversePreditiveEpisodes() {
		return mineEpisodes(miner.getInversePredictiveWindows(),miner.getPredictiveWindows(),miner.getNeutralWindows());
	}

}
