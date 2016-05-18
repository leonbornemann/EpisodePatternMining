package episode.finance;

import java.util.Collection;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public interface EpisodePattern {

	public int length();
	
	public Collection<AnnotatedEventType> getAll();
	
	
}
