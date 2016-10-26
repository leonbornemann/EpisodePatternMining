package data.stream;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import data.AnnotatedEvent;

public class InMemoryMultiTimeSeriesAnnotatedEventStream extends AbstractAnnotatedEventStream{
	
	private List<AnnotatedEvent> stream;
	private int curIndex;

	public InMemoryMultiTimeSeriesAnnotatedEventStream(File streamDir) throws IOException {
		List<File> allFiles = Arrays.asList(streamDir.listFiles()).stream().filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList());
		List<AnnotatedEvent> allEvents = new ArrayList<>();
		for(File file : allFiles){
			allEvents.addAll(AnnotatedEvent.readAll(file));
		}
		Collections.sort(allEvents,(a,b) -> a.getTimestamp().compareTo(b.getTimestamp()));
		this.stream = allEvents;
		curIndex = 0;
	}

	@Override
	public FixedStreamWindow getBackwardsWindow(int d) {
		if(curIndex==0){
			return new FixedStreamWindow(new ArrayList<>());
		}
		LocalDateTime endTimestamp = stream.get(curIndex).getTimestamp();
		int index = curIndex-1;
		List<AnnotatedEvent> window = new ArrayList<>();
		while(index >=0){
			long diffInSeconds = ChronoUnit.SECONDS.between(stream.get(index).getTimestamp(), endTimestamp);
			if(diffInSeconds >d){
				break;
			} else if(diffInSeconds > 0){ //we need the if, since events may have happened at the same time and occur before in our list
				//add this element to the front of our window
				window.add(0, stream.get(index));
			}
			index--;
		}
		return new FixedStreamWindow(window);
	}

	@Override
	public boolean hasNext() {
		return curIndex <stream.size();
	}

	@Override
	public AnnotatedEvent next() throws IOException {
		return stream.get(curIndex++);
	}

	@Override
	public AnnotatedEvent peek() {
		return stream.get(curIndex);
	}

}

