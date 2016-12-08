package data.stream;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.events.CategoricalEvent;
import util.Pair;

/***
 * A fixed window of a categorical event stream that contains exactly one, unchanging window of the stream
 * @author Leon Bornemann
 *
 */
public class FixedStreamWindow extends AbstractStreamWindow {

	public FixedStreamWindow(List<CategoricalEvent> window) {
		this.window = window;
	}
	
	@Override
	public Pair<LocalDateTime,LocalDateTime> getWindowBorders() {
		return new Pair<>(window.get(0).getTimestamp(),window.get(window.size()-1).getTimestamp());
	}

	/***
	 * returns a subwindow starting at the startIndex with the given duration
	 * @param startIndex
	 * @param windowDuration
	 */
	public FixedStreamWindow getSubWindow(int startIndex, int windowDuration) {
		if(window.size()==0){
			return this;
		}
		if(startIndex<0 || startIndex >=window.size()){
			throw new IndexOutOfBoundsException();
		}
		int curIndex = startIndex;
		LocalDateTime startTs = window.get(startIndex).getTimestamp();
		ArrayList<CategoricalEvent> newWindow = new ArrayList<CategoricalEvent>();
		while(curIndex < window.size()){
			CategoricalEvent curEvent = window.get(curIndex);
			if(ChronoUnit.SECONDS.between(startTs,curEvent.getTimestamp())<=windowDuration){
				newWindow.add(curEvent);
			} else{
				break;
			}
			curIndex++;
		}
		return new FixedStreamWindow(newWindow);
	}

	public boolean isEmpty() {
		return window.isEmpty();
	}
	
}
