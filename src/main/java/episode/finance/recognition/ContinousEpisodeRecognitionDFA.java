package episode.finance.recognition;

import java.time.LocalDateTime;

import episode.finance.EpisodePattern;
import prediction.data.AnnotatedEvent;
import util.Pair;

public interface ContinousEpisodeRecognitionDFA {

	public Pair<LocalDateTime,LocalDateTime> processEvent(AnnotatedEvent e);
	
	public EpisodePattern getEpsiodePattern();
	
	public int getOccuranceCount();
}
