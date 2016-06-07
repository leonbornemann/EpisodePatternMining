package reallife_data.finance.yahoo.stock.stream;

import java.io.IOException;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;

public interface AnnotatedEventStream {

	public StreamWindow getBackwardsWindow(int d);

	public boolean hasNext();

	public AnnotatedEvent next() throws IOException;
}
