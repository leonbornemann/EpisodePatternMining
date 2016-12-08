package data.stream;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import data.events.CategoricalEvent;
import data.events.Change;
import util.StandardDateTimeFormatter;

/***
 * Implementation that keeps the entire stream in memory.
 * @author Leon Bornemann
 *
 */
public class InMemoryCategoricalEventStream extends AbstractCategoricalEventStream{
	
	public static InMemoryCategoricalEventStream read(File streamFile) throws IOException{
		List<CategoricalEvent> events = new ArrayList<>();
		BufferedReader br = new BufferedReader(new FileReader(streamFile));
		br.readLine();
		String line = br.readLine();
		while(line!=null && !line.equals("")){
			String[] tokens = line.split(",");
			CategoricalEvent event = new CategoricalEvent(tokens[0].replaceAll("\"", ""), Change.valueOf(tokens[1]), LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter()) );
			events.add(event);
			line=br.readLine();
		}
		br.close();
		return new InMemoryCategoricalEventStream(events);
	}

	private final List<CategoricalEvent> events;
	private int pos;
	
	public InMemoryCategoricalEventStream(List<CategoricalEvent> events){
		this.events = Collections.unmodifiableList(events);
		this.pos = 0;
	}
	
	/***
	 * Returns a window of the stream in the following: Let t be the timestamp of the event at position pos, the window will then contain all events that have timestamps in the interval [t-d,t),
	 * this means events[pos] is NOT contained in the interval. 
	 * @param d duration (in seconds)
	 * @param pos the index of the event from which we want a backwards window
	 * @return
	 */
	public FixedStreamWindow getBackwardsWindow(int d,int pos){
		if(pos==0){
			return new FixedStreamWindow(new ArrayList<>());
		}
		LocalDateTime endTimestamp = events.get(pos).getTimestamp();
		int index = pos-1;
		List<CategoricalEvent> window = new ArrayList<>();
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
		return new FixedStreamWindow(window);
	}

	public List<CategoricalEvent> getEvents() {
		return events;
	}

	public InMemoryCategoricalEventStream filter(Predicate<? super CategoricalEvent> predicate) {
		return new InMemoryCategoricalEventStream(events.stream().filter(predicate).collect(Collectors.toList()));
	}

	@Override
	public FixedStreamWindow getBackwardsWindow(int d) {
		return getBackwardsWindow(d,pos);
	}

	@Override
	public boolean hasNext() {
		return pos<events.size();
	}

	@Override
	public CategoricalEvent next() throws IOException {
		return events.get(pos++);
	}

	@Override
	public CategoricalEvent peek() {
		return events.get(pos);
	}
}
