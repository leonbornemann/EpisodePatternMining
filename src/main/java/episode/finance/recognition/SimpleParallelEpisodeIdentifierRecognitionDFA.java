package episode.finance.recognition;

import java.util.HashMap;
import java.util.Set;

import episode.finance.ParallelEpisodePattern;
import episode.finance.storage.EpisodeIdentifier;
import prediction.data.AnnotatedEventType;

public class SimpleParallelEpisodeIdentifierRecognitionDFA<T> implements SimpleEpisodeRecognitionDFA<T> {

	private HashMap<AnnotatedEventType,Integer> remaining;
	private EpisodeIdentifier<T> identifier;

	
	public SimpleParallelEpisodeIdentifierRecognitionDFA(EpisodeIdentifier<T> identifier) {
		this.identifier = identifier;
		reset();
	}

	@Override
	public void reset() {
		remaining = new HashMap<>(new ParallelEpisodePattern(identifier.getCanonicalEpisodeRepresentation()).getEvents());
	}

	@Override
	public void processEvent(AnnotatedEventType e) {
		if(remaining.containsKey(e)){
			Integer current = remaining.get(e);
			assert(current>0);
			if(current==1){
				remaining.remove(e);
			} else{
				remaining.put(e,current-1);
			}
		}
	}

	@Override
	public boolean isDone() {
		return remaining.isEmpty();
	}

	@Override
	public boolean waitsFor(AnnotatedEventType e) {
		return remaining.containsKey(e);
	}
	
	public Set<AnnotatedEventType> getRemainingRequiredEventTypes(){
		return remaining.keySet();
	}

	@Override
	public EpisodeIdentifier<T> getEpisodePattern() {
		return identifier;
	}

}
