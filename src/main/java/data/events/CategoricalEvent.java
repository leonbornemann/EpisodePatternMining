package data.events;

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

import util.StandardDateTimeFormatter;

/***
 * Main data class for categorical events
 * @author Leon Bornemann
 *
 */
public class CategoricalEvent {
	
	private LocalDateTime timestamp;
	private CategoricalEventType annotatedEventType;
	
	public CategoricalEvent(String companyId, Change change, LocalDateTime timestamp) {
		this.annotatedEventType = new CategoricalEventType(companyId, change); //TODO: we could have a database of singletons where we just lookup the event types.
		this.timestamp = timestamp;
	}

	public CategoricalEvent(CategoricalEventType type, LocalDateTime timestamp) {
		this.annotatedEventType = type;
		this.timestamp = timestamp;
	}

	public static void serialize(List<CategoricalEvent> allAnnotated, File outFile) throws IOException {
		Collections.sort(allAnnotated, (a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));
		PrintWriter pr = new PrintWriter(new FileWriter(outFile));
		pr.println("company,change,timestamp");
		for(int i=0;i<allAnnotated.size();i++){
			CategoricalEvent e = allAnnotated.get(i);
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

	public CategoricalEventType getEventType() {
		return annotatedEventType;
	}

	@Override
	public String toString(){
		return annotatedEventType.getCompanyID() + "," + annotatedEventType.getChange() + "," + timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter());
	}

	public static List<CategoricalEvent> readAll(File file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		reader.readLine();
		String line = reader.readLine();
		List<CategoricalEvent> events = new ArrayList<>();
		while(line!=null){
			CategoricalEvent event = parseEvent(line);
			events.add(event);
			line = reader.readLine();
		}
		reader.close();
		return events;
	}
	
	private static CategoricalEvent parseEvent(String line) {
		String[] tokens = line.split(",");
		LocalDateTime timestamp = LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter());
		CategoricalEvent event = new CategoricalEvent(tokens[0].replaceAll("\"", ""), Change.valueOf(tokens[1]), timestamp );
		return event;
	}
	
}
