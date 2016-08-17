package prediction.data.stream;

import java.io.IOException;
import java.util.List;

import prediction.data.AnnotatedEvent;

public interface AnnotatedEventStream {

	public FixedStreamWindow getBackwardsWindow(int d);

	public boolean hasNext();

	public AnnotatedEvent next() throws IOException;

	public AnnotatedEvent peek();

	/***
	 * Moves the stream forward and returns all events that have the same timestamp as the one shown by peek()
	 * @return
	 * @throws IOException
	 */
	public List<AnnotatedEvent> getAllEventsOfCurrentTimestamp() throws IOException;
}
