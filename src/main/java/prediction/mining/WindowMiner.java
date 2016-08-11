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

	/***
	 * Initializes a Window Miner, which will progress through the stream, until either the stream is exhausted or the target number of backwards windows is found
	 * @param stream
	 * @param toPredict the event type that must follow the predictive windows and whose inverse type must follow the inversePredictiveWindows
	 * @param numWindows the number of windows of all three categories that must be achieved
	 * @param windowDuration the window duration in seconds
	 * @throws IOException
	 */
	public WindowMiner(AnnotatedEventStream stream, AnnotatedEventType toPredict, int numWindows, int windowDuration) throws IOException{
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
			if(predictiveWindows.size()!=numWindows && current.getEventType().equals(toPredict)){
				predictiveWindows.add(stream.getBackwardsWindow(windowDuration));
				lastUsedTime = current.getTimestamp();
			} else if(inversePredictiveWindows.size()!=numWindows && current.getEventType().equals(toPredict.getInverseEvent())){
				inversePredictiveWindows.add(stream.getBackwardsWindow(windowDuration));
				lastUsedTime = current.getTimestamp();
			} else if(nothingWindows.size()!=numWindows && ChronoUnit.SECONDS.between(lastUsedTime, currentTime) >=windowDuration*2){ //TODO: fix this, should be 2*d and window should start in the middle
				lastUsedTime = current.getTimestamp();
				FixedStreamWindow largeWindow = stream.getBackwardsWindow(windowDuration*2);
				nothingWindows.add(largeWindow.getSubWindow(0,windowDuration));
			}
			//TODO: get a window of size m where neither happens!
			if(predictiveWindows.size()==numWindows && inversePredictiveWindows.size() == numWindows && nothingWindows.size()==numWindows){
				break;
			}
		}
	}

	public List<FixedStreamWindow> getNeutralWindows() {
		return nothingWindows;
	}

	public List<FixedStreamWindow> getInversePredictiveWindows() {
		return inversePredictiveWindows;
	}

	public List<FixedStreamWindow> getPredictiveWindows() {
		return predictiveWindows;
	}
}
