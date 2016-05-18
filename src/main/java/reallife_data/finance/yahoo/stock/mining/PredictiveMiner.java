package reallife_data.finance.yahoo.stock.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import episode.finance.EpisodePatternMiner;
import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class PredictiveMiner {

	private AnnotatedEventStream stream;
	private AnnotatedEventType toPredict;
	private int n;
	private int m;
	private int s;
	private int d;
	private Set<AnnotatedEventType> eventAlphabet;

	public PredictiveMiner(AnnotatedEventStream stream, AnnotatedEventType toPredict, Set<AnnotatedEventType> eventAlphabet,int m, int s,int n,int d) {
		this.stream = stream;
		this.toPredict = toPredict;
		this.n = n;
		this.m = m;
		this.s = s;
		this.d = d;
		this.eventAlphabet =eventAlphabet;
	}
	
	public Map<SerialEpisodePattern,Integer> getInitialPreditiveEpisodes(){
		List<AnnotatedEvent> events = stream.getEvents();
		List<StreamWindow> predictiveWindows = new ArrayList<>();
		List<StreamWindow> inversePredictiveWindows = new ArrayList<>();
		for(int i=0;i<events.size();i++){
			if(predictiveWindows.size()!=m &&events.get(i).getEventType().equals(toPredict)){
				System.out.println("found new apple up");
				predictiveWindows.add(stream.getBackwardsWindow(d, i));
			} else if(inversePredictiveWindows.size()!=m && events.get(i).getEventType().equals(toPredict.getInverseEvent())){
				System.out.println("found new apple down");
				inversePredictiveWindows.add(stream.getBackwardsWindow(d, i));
			}
			if(predictiveWindows.size()==m && inversePredictiveWindows.size() == m){
				break;
			}
		}
		EpisodePatternMiner patternMiner = new EpisodePatternMiner(predictiveWindows, inversePredictiveWindows, eventAlphabet);
		return patternMiner.mineSerialEpisodes(s, n);
	}

}
