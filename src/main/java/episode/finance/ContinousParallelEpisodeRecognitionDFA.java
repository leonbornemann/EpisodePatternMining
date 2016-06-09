package episode.finance;

import java.time.LocalDateTime;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public class ContinousParallelEpisodeRecognitionDFA implements ContinousEpisodeRecognitionDFA {

	private ParallelEpisodePattern pattern;

	public ContinousParallelEpisodeRecognitionDFA(ParallelEpisodePattern pattern) {
		this.pattern = pattern;
	}

	@Override
	public Pair<LocalDateTime, LocalDateTime> processEvent(AnnotatedEvent e) {
		//TODO: what do we do here? How do we do this?
	}

	@Override
	public EpisodePattern getEpsiodePattern() {
		return pattern;
	}

}
