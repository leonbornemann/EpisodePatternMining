package episode.finance.recognition;

import data.AnnotatedEventType;
import episode.finance.storage.EpisodeIdentifier;

public interface SimpleEpisodeRecognitionDFA<T> {

	public void reset();
	
	public void processEvent(AnnotatedEventType e);
	
	public boolean isDone();

	public boolean waitsFor(AnnotatedEventType e);

	public EpisodeIdentifier<T> getEpisodePattern();
}
