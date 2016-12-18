package episode.pattern.recognition;

import java.util.HashMap;
import java.util.Set;

import data.events.CategoricalEventType;
import episode.pattern.ParallelEpisodePattern;
import episode.pattern.storage.EpisodeIdentifier;

public class ParallelEpisodeRecognitionDFA<T> implements EpisodeRecognitionDFA<T> {

	private HashMap<CategoricalEventType,Integer> remaining;
	private EpisodeIdentifier<T> identifier;

	
	public ParallelEpisodeRecognitionDFA(EpisodeIdentifier<T> identifier) {
		this.identifier = identifier;
		reset();
	}

	@Override
	public void reset() {
		remaining = new HashMap<>(new ParallelEpisodePattern(identifier.getCanonicalEpisodeRepresentation()).getEvents());
	}

	@Override
	public void processEvent(CategoricalEventType e) {
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
	public boolean waitsFor(CategoricalEventType e) {
		return remaining.containsKey(e);
	}
	
	public Set<CategoricalEventType> getRemainingRequiredEventTypes(){
		return remaining.keySet();
	}

	@Override
	public EpisodeIdentifier<T> getEpisodePattern() {
		return identifier;
	}

}
