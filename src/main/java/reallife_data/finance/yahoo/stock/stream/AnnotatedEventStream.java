package reallife_data.finance.yahoo.stock.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;

public class AnnotatedEventStream {

	public static AnnotatedEventStream read(File streamFile) throws IOException{
		List<AnnotatedEvent> events = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(streamFile));
		br.readLine();
		String line = br.readLine();
		while(line!=null && !line.equals("")){
			String[] tokens = line.split(",");
			AnnotatedEvent event = new AnnotatedEvent(tokens[0].replaceAll("\"", ""), Change.valueOf(tokens[1]), LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter()) );
			events.add(event);
			line=br.readLine();
		}
		br.close();
		return new AnnotatedEventStream(events);
	}

	private final List<AnnotatedEvent> events;
	
	public AnnotatedEventStream(List<AnnotatedEvent> events){
		this.events = Collections.unmodifiableList(events);
	}
	
	/***
	 * Returns a window of the stream in the following: Let t be the timestamp of the event at position pos, the window will then contain all events that have timestamps in the interval [t-d,t),
	 * this means events[pos] is NOT contained in the interval. 
	 * @param d duration (in seconds)
	 * @param pos the index of the event from which we want a backwards window
	 * @return
	 */
	public StreamWindow getBackwardsWindow(int d,int pos){
		if(pos==0){
			return new StreamWindow(new ArrayList<>());
		}
		LocalDateTime endTimestamp = events.get(pos).getTimestamp();
		int index = pos-1;
		List<AnnotatedEvent> window = new ArrayList<>();
		while(index >=0){
			long diffInSeconds = ChronoUnit.SECONDS.between(events.get(index).getTimestamp(), endTimestamp);
			if(diffInSeconds >d){
				break;
			} else if(diffInSeconds > 0){ //we need the if, since events may have happened at the same time and occur before in our list
				//add this element to the front of our window
				window.add(0, events.get(index));
			}
			index--;
		}
		return new StreamWindow(window);
	}

	public List<AnnotatedEvent> getEvents() {
		return events;
	}
}
