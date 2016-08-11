package episode.lossy_counting;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerialEpisode {

	List<EventType> events;
	
	public SerialEpisode(EventType... events){
		this.events = new ArrayList<EventType>(Arrays.asList(events));
	}
	
	public SerialEpisode(List<EventType> events) {
		this.events = events;
	}

	public EventType get(int index){
		return events.get(index);
	}

	public int getLength() {
		return events.size();
	}

	public boolean isLastIndex(Integer j) {
		return j == events.size()-1;
	}

	public List<EventType> subList(int i, int j) {
		return events.subList(i, j);
	}

	public void addEventType(EventType eventType) {
		events.add(eventType);
	}
	
	@Override
	public String toString(){
		return events.toString();
	}
}
