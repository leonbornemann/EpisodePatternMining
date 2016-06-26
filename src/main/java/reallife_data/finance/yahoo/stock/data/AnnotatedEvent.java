package reallife_data.finance.yahoo.stock.data;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;

public class AnnotatedEvent {
	
	private LocalDateTime timestamp;
	private AnnotatedEventType annotatedEventType;
	
	public AnnotatedEvent(String companyId, Change change, LocalDateTime timestamp) {
		this.annotatedEventType = new AnnotatedEventType(companyId, change); //TODO: we could have a database of singletons where we just lookup the event types.
		this.timestamp = timestamp;
	}

	public AnnotatedEvent(AnnotatedEventType type, LocalDateTime timestamp) {
		this.annotatedEventType = type;
		this.timestamp = timestamp;
	}

	public static void serialize(List<AnnotatedEvent> allAnnotated, File outFile) throws IOException {
		Collections.sort(allAnnotated, (a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));
		PrintWriter pr = new PrintWriter(new FileWriter(outFile));
		pr.println("company,change,timestamp");
		allAnnotated.forEach(e -> pr.println(e.annotatedEventType.getCompanyID()+"," + e.annotatedEventType.getChange() + ","+e.timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter())));
		pr.close();
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	public AnnotatedEventType getEventType() {
		return annotatedEventType;
	}

	@Override
	public String toString(){
		return annotatedEventType.getCompanyID() + "," + annotatedEventType.getChange() + "," + timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter());
	}
	
}
