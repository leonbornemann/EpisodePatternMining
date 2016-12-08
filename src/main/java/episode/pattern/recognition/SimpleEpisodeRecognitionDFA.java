package episode.pattern.recognition;

import data.events.CategoricalEventType;
import episode.pattern.storage.EpisodeIdentifier;

/***
 * Basic interface for recognition algorithms
 * @author Leon Bornemann
 *
 * @param <T>
 */
public interface SimpleEpisodeRecognitionDFA<T> {

	public void reset();
	
	public void processEvent(CategoricalEventType e);
	
	public boolean isDone();

	public boolean waitsFor(CategoricalEventType e);

	public EpisodeIdentifier<T> getEpisodePattern();
}
