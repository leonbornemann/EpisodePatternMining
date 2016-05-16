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
	
	private String companyId;
	private Change change;
	private LocalDateTime timestamp;
	
	public AnnotatedEvent(String companyId, Change change, LocalDateTime timestamp) {
		super();
		this.companyId = companyId;
		this.change = change;
		this.timestamp = timestamp;
	}

	public static void serialize(List<AnnotatedEvent> allAnnotated, File outFile) throws IOException {
		Collections.sort(allAnnotated, (a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));
		PrintWriter pr = new PrintWriter(new FileWriter(outFile));
		pr.println("company,change,timestamp");
		allAnnotated.forEach(e -> pr.println(e.companyId+"," + e.change + ","+e.timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter())));
		pr.close();
	}

	private LocalDateTime getTimestamp() {
		return timestamp;
	}

	
}
