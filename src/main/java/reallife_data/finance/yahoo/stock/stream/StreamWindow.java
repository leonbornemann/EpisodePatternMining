package reallife_data.finance.yahoo.stock.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class StreamWindow {

	List<AnnotatedEvent> window;

	public StreamWindow(List<AnnotatedEvent> window) {
		this.window = window;
	}

	public List<AnnotatedEvent> getEvents() {
		return window;
	}

	public Map<LocalDateTime, List<AnnotatedEvent>> getEventTypesByTimestamp() {
		return window.stream().collect(Collectors.groupingBy(AnnotatedEvent::getTimestamp));
		
	}
	
}
