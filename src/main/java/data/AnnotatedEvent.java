package data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import prediction.util.StandardDateTimeFormatter;

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
		for(int i=0;i<allAnnotated.size();i++){
			AnnotatedEvent e = allAnnotated.get(i);
			String toPrint = e.annotatedEventType.getCompanyID()+"," + e.annotatedEventType.getChange() + ","+e.timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter());
			if(i==allAnnotated.size()-1){
				pr.print(toPrint);
			} else{
				pr.println(toPrint);
			}
		}
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

	public static List<AnnotatedEvent> readAll(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.readLine();
		String line = reader.readLine();
		List<AnnotatedEvent> events = new ArrayList<>();
		while(line!=null){
			AnnotatedEvent event = parseEvent(line);
			events.add(event);
			line = reader.readLine();
		}
		reader.close();
		return events;
	}
	
	private static AnnotatedEvent parseEvent(String line) {
		String[] tokens = line.split(",");
		LocalDateTime timestamp = LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter());
		AnnotatedEvent event = new AnnotatedEvent(tokens[0].replaceAll("\"", ""), Change.valueOf(tokens[1]), timestamp );
		return event;
	}
	
}
