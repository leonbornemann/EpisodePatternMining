package episode.finance.recognition;

import java.time.LocalDateTime;

import data.AnnotatedEvent;
import episode.finance.EpisodePattern;
import util.Pair;

public interface ContinousEpisodeRecognitionDFA {

	public Pair<LocalDateTime,LocalDateTime> processEvent(AnnotatedEvent e);
	
	public EpisodePattern getEpsiodePattern();
	
	public int getOccuranceCount();
}
