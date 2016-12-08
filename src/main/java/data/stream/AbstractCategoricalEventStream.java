package data.stream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import data.events.CategoricalEvent;

public abstract class AbstractCategoricalEventStream implements CategoricalEventStream {

	@Override
	public List<CategoricalEvent> getAllEventsOfCurrentTimestamp() throws IOException {
		if(hasNext()){
			LocalDateTime curTs = peek().getTimestamp();
			List<CategoricalEvent> thisTs = new ArrayList<>();
			while(hasNext() && peek().getTimestamp().equals(curTs)){
				thisTs.add(next());
			}
			return thisTs;
		} else{
			throw new NoSuchElementException();
		}
	}

}
