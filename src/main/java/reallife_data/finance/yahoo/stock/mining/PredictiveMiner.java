package reallife_data.finance.yahoo.stock.mining;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePattern;
import episode.finance.ParallelEpisodePatternMiner;
import episode.finance.SerialEpisodePattern;
import episode.finance.SerialEpisodePatternMiner;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.MultiFileAnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class PredictiveMiner {

	private MultiFileAnnotatedEventStream stream;
	private AnnotatedEventType toPredict;
	private int n;
	private int m;
	private int s;
	private int d;
	private Set<AnnotatedEventType> eventAlphabet;
	private List<StreamWindow> predictiveWindows;
	private List<StreamWindow> inversePredictiveWindows;
	private List<StreamWindow> nothingWindows;

	public PredictiveMiner(MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, Set<AnnotatedEventType> eventAlphabet,int m, int s,int n,int d) throws IOException {
		this.stream = stream;
		this.toPredict = toPredict;
		this.n = n;
		this.m = m;
		this.s = s;
		this.d = d;
		this.eventAlphabet =eventAlphabet;
		predictiveWindows = new ArrayList<>();
		inversePredictiveWindows = new ArrayList<>();
		nothingWindows = new ArrayList<>();
		LocalDateTime currentTime;
		LocalDateTime lastUsedTime =null;;
		while(stream.hasNext()){
			AnnotatedEvent current = stream.next();
			currentTime = current.getTimestamp();
			if(lastUsedTime==null){
				lastUsedTime = currentTime;
			}
			if(predictiveWindows.size()!=m &&current.getEventType().equals(toPredict)){
				System.out.println("found new apple up");
				predictiveWindows.add(stream.getBackwardsWindow(d));
				lastUsedTime = current.getTimestamp();
			} else if(inversePredictiveWindows.size()!=m && current.getEventType().equals(toPredict.getInverseEvent())){
				System.out.println("found new apple down");
				inversePredictiveWindows.add(stream.getBackwardsWindow(d));
				lastUsedTime = current.getTimestamp();
			} else if(nothingWindows.size()!=m &&ChronoUnit.SECONDS.between(lastUsedTime, currentTime) >=d){ //TODO: fix this, should be 2*d and window should start in the middle
				System.out.println("found empty Window");
				lastUsedTime = current.getTimestamp();
				nothingWindows.add(stream.getBackwardsWindow(d));
			}
			//TODO: get a window of size m where neither happens!
			if(predictiveWindows.size()==m && inversePredictiveWindows.size() == m && nothingWindows.size()==m){
				break;
			}
		}
		predictiveWindows.forEach(e -> System.out.println(e.getWindowBorders()));
	}
	
	public Map<EpisodePattern,Integer> getInitialPreditiveEpisodes() throws IOException{
		return mineEpisodes(predictiveWindows,inversePredictiveWindows,nothingWindows);
	}

	private Map<EpisodePattern, Integer> mineEpisodes(List<StreamWindow> predictiveWindows,List<StreamWindow> inversePredictiveWindows,List<StreamWindow> nothingWindows) {
		SerialEpisodePatternMiner serialEpisodeMiner = new SerialEpisodePatternMiner(predictiveWindows, inversePredictiveWindows,nothingWindows, eventAlphabet);
		ParallelEpisodePatternMiner parallelEpisodeMiner = new ParallelEpisodePatternMiner(predictiveWindows, inversePredictiveWindows,nothingWindows, eventAlphabet);
		Map<EpisodePattern,Integer> initialPredictiveEpisodes = new HashMap<>();
		initialPredictiveEpisodes.putAll(serialEpisodeMiner.mineEpisodePatterns(s, n));
		initialPredictiveEpisodes.putAll(parallelEpisodeMiner.mineEpisodePatterns(s, n));
		return initialPredictiveEpisodes;
	}

	public Map<EpisodePattern, Integer> getInitialInversePreditiveEpisodes() {
		return mineEpisodes(inversePredictiveWindows,predictiveWindows,nothingWindows);
	}

}
