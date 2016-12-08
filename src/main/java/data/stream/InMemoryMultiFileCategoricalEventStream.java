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

import data.events.CategoricalEvent;

/***
 * Implementation that merges multiple individual streams to one big stream. Keeps the entire stream in memory.
 * @author Leon Bornemann
 *
 */
public class InMemoryMultiFileCategoricalEventStream extends AbstractCategoricalEventStream{
	
	private List<CategoricalEvent> stream;
	private int curIndex;

	/***
	 * The Stream is assembled from all .csv files in all directories passed to this constructor
	 * @param streamDirs
	 * @throws IOException
	 */
	public InMemoryMultiFileCategoricalEventStream(List<File> streamDirs) throws IOException {
		List<CategoricalEvent> allEvents = new ArrayList<>();
		for(File streamDir : streamDirs){
			List<File> allFiles = Arrays.asList(streamDir.listFiles()).stream().filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList());
			for(File file : allFiles){
				allEvents.addAll(CategoricalEvent.readAll(file));
			}
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
		List<CategoricalEvent> window = new ArrayList<>();
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
	public CategoricalEvent next() throws IOException {
		return stream.get(curIndex++);
	}

	@Override
	public CategoricalEvent peek() {
		return stream.get(curIndex);
	}

}

