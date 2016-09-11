package episode.finance;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import episode.finance.recognition.ContinousEpisodeRecognitionDFA;
import episode.finance.recognition.SimpleEpisodeRecognitionDFA;
import prediction.data.AnnotatedEventType;

public interface EpisodePattern extends Serializable{

	public int length();
		
	public SimpleEpisodeRecognitionDFA getSimpleRecognitionDFA();

	public ContinousEpisodeRecognitionDFA getContinousDFA();

	public Set<AnnotatedEventType> getAllContainedTypes();

	public boolean containsType(AnnotatedEventType e);

	public List<AnnotatedEventType> getCanonicalListRepresentation();
	
}
