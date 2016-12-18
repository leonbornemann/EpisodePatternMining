package prediction.training;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.stream.CategoricalEventStream;
import data.stream.FixedStreamWindow;

/***
 * Class that mines training examples (windows) from a categorical event stream.
 * @author Leon Bornemann
 *
 */
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
	public WindowMiner(CategoricalEventStream stream, CategoricalEventType toPredict, int numWindows, int windowDuration) throws IOException{
		predictiveWindows = new ArrayList<>();
		inversePredictiveWindows = new ArrayList<>();
		nothingWindows = new ArrayList<>();
		LocalDateTime currentTime;
		LocalDateTime lastUsedTimeOfEvent =null;;
		while(stream.hasNext()){
			CategoricalEvent current = stream.next();
			currentTime = current.getTimestamp();
			if(lastUsedTimeOfEvent==null){
				lastUsedTimeOfEvent = currentTime;
			}
			if(predictiveWindows.size()!=numWindows && current.getEventType().equals(toPredict)){
				FixedStreamWindow backwardsWindow = stream.getBackwardsWindow(windowDuration);
				if(!backwardsWindow.isEmpty()){
					predictiveWindows.add(backwardsWindow);
					lastUsedTimeOfEvent = current.getTimestamp();
				}
			} else if(inversePredictiveWindows.size()!=numWindows && current.getEventType().equals(toPredict.getInverseEvent())){
				FixedStreamWindow backwardsWindow = stream.getBackwardsWindow(windowDuration);
				if(!backwardsWindow.isEmpty()){
					inversePredictiveWindows.add(backwardsWindow);
					lastUsedTimeOfEvent = current.getTimestamp();
				}
			} else if(nothingWindows.size()!=numWindows && ChronoUnit.SECONDS.between(lastUsedTimeOfEvent, currentTime) >=windowDuration*2){ 
				lastUsedTimeOfEvent = current.getTimestamp();
				FixedStreamWindow largeWindow = stream.getBackwardsWindow(windowDuration*2);
				FixedStreamWindow backwardsWindow = largeWindow.getSubWindow(0,windowDuration);
				if(!backwardsWindow.isEmpty()){
					nothingWindows.add(backwardsWindow);
				}
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
