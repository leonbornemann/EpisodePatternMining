package episode.pattern;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import data.events.CategoricalEventType;
import episode.pattern.recognition.SimpleEpisodeRecognitionDFA;

/***
 * Main interface for episode patterns.
 * @author Leon Bornemann
 *
 */
public interface EpisodePattern extends Serializable{

	public int length();
		
	public SimpleEpisodeRecognitionDFA getSimpleRecognitionDFA();

	public Set<CategoricalEventType> getAllContainedTypes();

	public boolean containsType(CategoricalEventType e);

	public List<CategoricalEventType> getCanonicalListRepresentation();
	
}
