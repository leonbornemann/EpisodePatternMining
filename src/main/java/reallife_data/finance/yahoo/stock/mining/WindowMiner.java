package reallife_data.finance.yahoo.stock.mining;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.stream.AnnotatedEventStream;
import reallife_data.finance.yahoo.stock.stream.StreamWindow;

public class WindowMiner {


	private ArrayList<StreamWindow> predictiveWindows;
	private ArrayList<StreamWindow> inversePredictiveWindows;
	private ArrayList<StreamWindow> nothingWindows;

	public WindowMiner(AnnotatedEventStream stream, AnnotatedEventType toPredict, int m, int d) throws IOException{
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
				predictiveWindows.add(stream.getBackwardsWindow(d));
				lastUsedTime = current.getTimestamp();
			} else if(inversePredictiveWindows.size()!=m && current.getEventType().equals(toPredict.getInverseEvent())){
				inversePredictiveWindows.add(stream.getBackwardsWindow(d));
				lastUsedTime = current.getTimestamp();
			} else if(nothingWindows.size()!=m &&ChronoUnit.SECONDS.between(lastUsedTime, currentTime) >=d){ //TODO: fix this, should be 2*d and window should start in the middle
				lastUsedTime = current.getTimestamp();
				nothingWindows.add(stream.getBackwardsWindow(d));
			}
			//TODO: get a window of size m where neither happens!
			if(predictiveWindows.size()==m && inversePredictiveWindows.size() == m && nothingWindows.size()==m){
				break;
			}
		}
	}

	public List<StreamWindow> getNothingWindows() {
		return nothingWindows;
	}

	public List<StreamWindow> getInversePredictiveWindows() {
		return inversePredictiveWindows;
	}

	public List<StreamWindow> getPredictiveWindows() {
		return predictiveWindows;
	}
}
