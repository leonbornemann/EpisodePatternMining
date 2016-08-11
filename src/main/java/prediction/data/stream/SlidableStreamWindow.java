package prediction.data.stream;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import prediction.data.AnnotatedEvent;
import util.Pair;

public class SlidableStreamWindow extends AbstractStreamWindow {

	public SlidableStreamWindow(){
		window = new LinkedList<>();
	}

	/***
	 * Appends a new event to the end of the window
	 * @param e
	 */
	public void append(AnnotatedEvent e){
		assert(window.get(window.size()-1).getTimestamp().compareTo(e.getTimestamp())<=0);
		window.add(e);
	}
	
	/***
	 * Removes the element at the start of the window
	 * @return 
	 */
	public AnnotatedEvent removeStart(){
		return window.remove(0);
	}
	
	public AnnotatedEvent first(){
		return window.get(0);
	}
	
	public AnnotatedEvent last(){
		return window.get(window.size()-1);
	}
	
	@Override
	public Pair<LocalDateTime, LocalDateTime> getWindowBorders() {
		return new Pair<>(first().getTimestamp(),last().getTimestamp());
	}

	/***
	 * 
	 * @return the size of the window (in seconds)
	 */
	public long windowDuration() {
		return ChronoUnit.SECONDS.between(first().getTimestamp(),last().getTimestamp());
	}

}
