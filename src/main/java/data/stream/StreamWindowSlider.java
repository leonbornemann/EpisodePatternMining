package data.stream;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.events.CategoricalEvent;

/***
 * Class used in the evaluation of models, that processes the stream by sliding a window over the stream.
 * @author Leon Bornemann
 *
 */
public class StreamWindowSlider {

	private CategoricalEventStream stream;
	private int windowSize;
	private SlidableStreamWindow slidingWindow;

	public StreamWindowSlider(CategoricalEventStream stream, int windowSize) throws IOException {
		this.stream = stream;
		this.windowSize = windowSize;
		initWindow();
	}
	
	public StreamWindow getCurrentWindow(){
		return slidingWindow;
	}
	
	public boolean canSlide(){
		return stream.hasNext();
	}
	
	/***
	 * Slides the window forward as many time units as necessaryto reach at least one new event that will be included in the current window. Returns the events that were dropped out by sliding the stream one time-unit forward.
	 * @return
	 * @throws IOException
	 */
	public List<CategoricalEvent> slideForward() throws IOException{
		assertWindowFilled();
		List<CategoricalEvent> toAdd = stream.getAllEventsOfCurrentTimestamp();
		toAdd.forEach(e -> slidingWindow.append(e));
		List<CategoricalEvent> droppedOut = new ArrayList<>();
		if(slidingWindow.windowDuration() > windowSize){
			//we need to drop out events to return back to the set window size
			while(slidingWindow.windowDuration() > windowSize){
				droppedOut.add(slidingWindow.removeStart());
			}
		}
		//we may be able to include more events:
		fillWindow();
		assertWindowFilled();
		return droppedOut;
	}

	private void assertWindowFilled() {
		if(stream.hasNext()){
			long timeDiff = ChronoUnit.SECONDS.between(slidingWindow.first().getTimestamp(),stream.peek().getTimestamp());
			assert(timeDiff > windowSize);
		}
	}

	private void fillWindow() throws IOException {
		List<CategoricalEvent> toAdd;
		while(stream.hasNext() && ChronoUnit.SECONDS.between(slidingWindow.first().getTimestamp(),stream.peek().getTimestamp())<=windowSize){
			toAdd = stream.getAllEventsOfCurrentTimestamp();
			toAdd.forEach(e -> slidingWindow.append(e));
		}
	}

	private void initWindow() throws IOException {
		slidingWindow = new SlidableStreamWindow();
		CategoricalEvent begin = null;
		while(stream.hasNext()){
			if(begin==null){
				begin = stream.next();
				slidingWindow.append(begin);
			} else if (ChronoUnit.SECONDS.between(begin.getTimestamp(),stream.peek().getTimestamp())<=windowSize){
				slidingWindow.append(stream.next());
			} else{
				//we would go over window size
				break;
			}
		}
	}

}
