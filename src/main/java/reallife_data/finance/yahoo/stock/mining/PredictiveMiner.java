package reallife_data.finance.yahoo.stock.mining;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePatternMiner;
import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
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

	public PredictiveMiner(MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, Set<AnnotatedEventType> eventAlphabet,int m, int s,int n,int d) {
		this.stream = stream;
		this.toPredict = toPredict;
		this.n = n;
		this.m = m;
		this.s = s;
		this.d = d;
		this.eventAlphabet =eventAlphabet;
	}
	
	public Map<SerialEpisodePattern,Integer> getInitialPreditiveEpisodes() throws IOException{
		List<StreamWindow> predictiveWindows = new ArrayList<>();
		List<StreamWindow> inversePredictiveWindows = new ArrayList<>();
		while(stream.hasNext()){
			AnnotatedEvent current = stream.next();
			if(predictiveWindows.size()!=m &&current.getEventType().equals(toPredict)){
				System.out.println("found new apple up");
				predictiveWindows.add(stream.getBackwardsWindow(d));
			} else if(inversePredictiveWindows.size()!=m && current.getEventType().equals(toPredict.getInverseEvent())){
				System.out.println("found new apple down");
				inversePredictiveWindows.add(stream.getBackwardsWindow(d));
			}
			if(predictiveWindows.size()==m && inversePredictiveWindows.size() == m){
				break;
			}
		}
		EpisodePatternMiner patternMiner = new EpisodePatternMiner(predictiveWindows, inversePredictiveWindows, eventAlphabet);
		return patternMiner.mineSerialEpisodes(s, n);
	}

}
