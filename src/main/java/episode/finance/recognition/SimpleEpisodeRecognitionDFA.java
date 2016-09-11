package episode.finance.recognition;

import episode.finance.EpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import prediction.data.AnnotatedEventType;

public interface SimpleEpisodeRecognitionDFA<T> {

	public void reset();
	
	public void processEvent(AnnotatedEventType e);
	
	public boolean isDone();

	public boolean waitsFor(AnnotatedEventType e);

	public EpisodeIdentifier<T> getEpisodePattern();
}
