package data.stream;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.AnnotatedEvent;

public class StreamWindowSlider {

	private AnnotatedEventStream stream;
	private int windowSize;
	private SlidableStreamWindow slidingWindow;

	public StreamWindowSlider(AnnotatedEventStream stream, int windowSize) throws IOException {
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
	
	public List<AnnotatedEvent> slideForward() throws IOException{
		assertWindowFilled();
		List<AnnotatedEvent> toAdd = stream.getAllEventsOfCurrentTimestamp();
		toAdd.forEach(e -> slidingWindow.append(e));
		List<AnnotatedEvent> droppedOut = new ArrayList<>();
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
		List<AnnotatedEvent> toAdd;
		while(stream.hasNext() && ChronoUnit.SECONDS.between(slidingWindow.first().getTimestamp(),stream.peek().getTimestamp())<=windowSize){
			toAdd = stream.getAllEventsOfCurrentTimestamp();
			toAdd.forEach(e -> slidingWindow.append(e));
		}
	}

	private void initWindow() throws IOException {
		slidingWindow = new SlidableStreamWindow();
		AnnotatedEvent begin = null;
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
