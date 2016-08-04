package reallife_data.finance.yahoo.stock.stream;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import episode.finance.EpisodePattern;
import episode.finance.SimpleEpisodeRecognitionDFA;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import util.Pair;

public class FixedStreamWindow extends AbstractStreamWindow {

	public FixedStreamWindow(List<AnnotatedEvent> window) {
		this.window = window;
	}
	
	@Override
	public Pair<LocalDateTime,LocalDateTime> getWindowBorders() {
		return new Pair<>(window.get(0).getTimestamp(),window.get(window.size()-1).getTimestamp());
	}
	
}
