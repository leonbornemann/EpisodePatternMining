package episode.finance;

import java.util.HashMap;
import java.util.Set;

import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;

public class SimpleParallelEpisodeRecognitionDFA implements SimpleEpisodeRecognitionDFA {

	private HashMap<AnnotatedEventType,Integer> remaining;
	private ParallelEpisodePattern parallelEpisodePattern;

	public SimpleParallelEpisodeRecognitionDFA(ParallelEpisodePattern parallelEpisodePattern) {
		this.parallelEpisodePattern = parallelEpisodePattern;
		reset();
	}

	@Override
	public void reset() {
		remaining = new HashMap<>(parallelEpisodePattern.getEvents());
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
	public ParallelEpisodePattern getEpisodePattern() {
		return parallelEpisodePattern;
	}

}
