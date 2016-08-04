package reallife_data.finance.yahoo.stock.stream;

import java.io.IOException;
import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;

public interface AnnotatedEventStream {

	public FixedStreamWindow getBackwardsWindow(int d);

	public boolean hasNext();

	public AnnotatedEvent next() throws IOException;

	public AnnotatedEvent peek();

	public List<AnnotatedEvent> getAllEventsOfCurrentTimestamp() throws IOException;
}
