package episode.finance;

import java.util.Collection;
import java.util.List;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class ParrallelEpisodePattern implements EpisodePattern{

private List<AnnotatedEventType> events;
	
	public ParrallelEpisodePattern(List<AnnotatedEventType> events) {
		this.events =events;
	}
	
	@Override
	public int length() {
		return events.size();
	}

	@Override
	public Collection<AnnotatedEventType> getAll() {
		return events;
	}

}
