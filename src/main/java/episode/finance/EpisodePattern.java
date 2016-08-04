package episode.finance;

import java.util.Set;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public interface EpisodePattern {

	public int length();
		
	public SimpleEpisodeRecognitionDFA getSimpleRecognitionDFA();

	public ContinousEpisodeRecognitionDFA getContinousDFA();

	public Set<AnnotatedEventType> getAllContainedTypes();

	public boolean containsType(AnnotatedEventType e);
	
}
