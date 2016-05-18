package reallife_data.finance.yahoo.stock.stream;

import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;

public class StreamWindow {

	List<AnnotatedEvent> window;

	public StreamWindow(List<AnnotatedEvent> window) {
		this.window = window;
	}

	public List<AnnotatedEvent> getEvents() {
		return window;
	}
	
}
