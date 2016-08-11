package prediction.data.stream;

import java.io.IOException;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import prediction.data.AnnotatedEvent;

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
		assert(ChronoUnit.SECONDS.between(slidingWindow.first().getTimestamp(),stream.peek().getTimestamp()) > windowSize);
		List<AnnotatedEvent> toAdd = stream.getAllEventsOfCurrentTimestamp();
		toAdd.forEach(e -> slidingWindow.append(e));
		List<AnnotatedEvent> droppedOut = new ArrayList<>();
		while(slidingWindow.windowDuration() >= windowSize){
			droppedOut.add(slidingWindow.removeStart());
		}	
		return droppedOut;
	}

	private void initWindow() throws IOException {
		slidingWindow = new SlidableStreamWindow();
		AnnotatedEvent begin = null;
		while(stream.hasNext()){
			if(begin==null){
				begin = stream.next();
			} else if (ChronoUnit.SECONDS.between(begin.getTimestamp(),stream.peek().getTimestamp())<=windowSize){
				slidingWindow.append(stream.next());
			} else{
				//we would go over window size
				break;
			}
		}
	}

}
