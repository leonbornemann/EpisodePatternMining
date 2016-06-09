package episode.finance;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public interface SimpleEpisodeRecognitionDFA {

	public void reset();
	
	public void processEvent(AnnotatedEventType e);
	
	public boolean isDone();

	public boolean waitsFor(AnnotatedEventType e);

	public EpisodePattern getEpisodePattern();
}
