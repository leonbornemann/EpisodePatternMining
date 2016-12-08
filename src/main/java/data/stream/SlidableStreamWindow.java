package data.stream;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.LinkedList;

import data.events.CategoricalEvent;
import util.Pair;

/***
 * Implemements a Window that is slidable (content of the window can change by appending events at the end and removing events at the start)
 * @author Leon Bornemann
 *
 */
public class SlidableStreamWindow extends AbstractStreamWindow {

	public SlidableStreamWindow(){
		window = new LinkedList<>();
	}

	/***
	 * Appends a new event to the end of the window
	 * @param e
	 */
	public void append(CategoricalEvent e){
		if(window.size()!=0){
			assert(window.get(window.size()-1).getTimestamp().compareTo(e.getTimestamp())<=0);
		}
		window.add(e);
	}
	
	/***
	 * Removes the element at the start of the window
	 * @return 
	 */
	public CategoricalEvent removeStart(){
		return window.remove(0);
	}
	
	public CategoricalEvent first(){
		return window.get(0);
	}
	
	public CategoricalEvent last(){
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
