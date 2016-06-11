package episode.finance;

import java.time.LocalDateTime;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public interface ContinousEpisodeRecognitionDFA {

	public Pair<LocalDateTime,LocalDateTime> processEvent(AnnotatedEvent e);
	
	public EpisodePattern getEpsiodePattern();
	
	public int getOccuranceCount();
}
