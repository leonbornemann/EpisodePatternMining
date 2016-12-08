package data.stream;

import java.io.IOException;
import java.util.List;

import data.events.CategoricalEvent;

/***
 * Basic Interface for categorical event streams
 * @author Leon Bornemann
 *
 */
public interface CategoricalEventStream {

	public FixedStreamWindow getBackwardsWindow(int d);

	public boolean hasNext();

	public CategoricalEvent next() throws IOException;

	public CategoricalEvent peek();

	/***
	 * Moves the stream forward and returns all events that have the same timestamp as the one shown by peek()
	 * @return
	 * @throws IOException
	 */
	public List<CategoricalEvent> getAllEventsOfCurrentTimestamp() throws IOException;
}
