package prediction.mining;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.stream.AnnotatedEventStream;
import prediction.data.stream.FixedStreamWindow;

public class WindowMiner {


	private ArrayList<FixedStreamWindow> predictiveWindows;
	private ArrayList<FixedStreamWindow> inversePredictiveWindows;
	private ArrayList<FixedStreamWindow> nothingWindows;

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

	public List<FixedStreamWindow> getNothingWindows() {
		return nothingWindows;
	}

	public List<FixedStreamWindow> getInversePredictiveWindows() {
		return inversePredictiveWindows;
	}

	public List<FixedStreamWindow> getPredictiveWindows() {
		return predictiveWindows;
	}
}
