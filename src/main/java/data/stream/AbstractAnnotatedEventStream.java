package data.stream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import data.AnnotatedEvent;

public abstract class AbstractAnnotatedEventStream implements AnnotatedEventStream {

	@Override
	public List<AnnotatedEvent> getAllEventsOfCurrentTimestamp() throws IOException {
		if(hasNext()){
			LocalDateTime curTs = peek().getTimestamp();
			List<AnnotatedEvent> thisTs = new ArrayList<>();
			while(hasNext() && peek().getTimestamp().equals(curTs)){
				thisTs.add(next());
			}
			return thisTs;
		} else{
			throw new NoSuchElementException();
		}
	}

}
