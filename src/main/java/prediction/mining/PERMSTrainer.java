package prediction.mining;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.AnnotatedEventType;
import data.stream.FixedStreamWindow;
import episode.finance.EpisodePattern;
import episode.finance.mining.ParallelEpisodePatternMiner;
import episode.finance.mining.SerialEpisodePatternMiner;

public class PERMSTrainer {

	private int n;
	private Set<AnnotatedEventType> eventAlphabet;
	private WindowMiner miner;
	private double sParallel;
	private double sSerial;

	public PERMSTrainer(WindowMiner miner,Set<AnnotatedEventType> eventAlphabet, double sSerial,double sParallel,int n) throws IOException {
		this.n = n;
		this.sSerial = sSerial;
		this.sParallel = sParallel;
		this.eventAlphabet =eventAlphabet;
		this.miner = miner;
	}
		
	public Map<EpisodePattern,Double> getInitialPreditiveEpisodes() throws IOException{
		return mineEpisodes(miner.getPredictiveWindows(),miner.getInversePredictiveWindows());
	}

	private Map<EpisodePattern, Double> mineEpisodes(List<FixedStreamWindow> predictiveWindows,List<FixedStreamWindow> inversePredictiveWindows) {
		SerialEpisodePatternMiner serialEpisodeMiner = new SerialEpisodePatternMiner(predictiveWindows, eventAlphabet);
		ParallelEpisodePatternMiner parallelEpisodeMiner = new ParallelEpisodePatternMiner(predictiveWindows, eventAlphabet);
		Map<EpisodePattern,Double> initialPredictiveEpisodes = new HashMap<>();
		initialPredictiveEpisodes.putAll(serialEpisodeMiner.mineBestEpisodePatterns(sSerial, n, inversePredictiveWindows));
		initialPredictiveEpisodes.putAll(parallelEpisodeMiner.mineBestEpisodePatterns(sParallel, n, inversePredictiveWindows));
		return initialPredictiveEpisodes;
	}

	public Map<EpisodePattern, Double> getInitialInversePreditiveEpisodes() {
		return mineEpisodes(miner.getInversePredictiveWindows(),miner.getPredictiveWindows());
	}

}
