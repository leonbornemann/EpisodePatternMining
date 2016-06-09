package episode.finance;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import util.Pair;

public class ContinousParallelEpisodeRecognitionDFA implements ContinousEpisodeRecognitionDFA {

	private ParallelEpisodePattern pattern;
	private Map<AnnotatedEventType,Queue<LocalDateTime>> eventOccurances;
	private Collection<AnnotatedEventType> relevantTypes;
	private boolean done;
	private HashMap<AnnotatedEventType,Integer> remaining;

	public ContinousParallelEpisodeRecognitionDFA(ParallelEpisodePattern pattern) {
		this.pattern = pattern;
		eventOccurances = new HashMap<>();
		relevantTypes = pattern.getEvents().keySet();
		remaining = new HashMap<>(pattern.getEvents());
		done = false;
	}

	@Override
	public Pair<LocalDateTime, LocalDateTime> processEvent(AnnotatedEvent e) {
		if(relevantTypes.contains(e.getEventType())){
			addOccurance(e.getEventType(), e.getTimestamp());
			if(completed()){
				LocalDateTime endTime = e.getTimestamp();
				LocalDateTime startTime = getCurrentStartTime();
				return new Pair<>(startTime,endTime);
			} else{
				return null;
			}
		} else{
			return null;
		}
	}

	private LocalDateTime getCurrentStartTime() {
		return eventOccurances.values().stream().map(e -> e.peek()).min(LocalDateTime::compareTo).get();
	}

	private boolean completed() {
		return remaining.isEmpty();
	}

	private void addOccurance(AnnotatedEventType eventType, LocalDateTime timestamp) {
		if(eventOccurances.containsKey(eventType)){
			eventOccurances.get(eventType).add(timestamp);
			if(remaining.get(eventType)==0){
				//delete the earliest occurance in favor of the one we just added
				eventOccurances.get(eventType).poll();
			} else{
				assert(!done);
				decrementRemaining(eventType);
			}
		} else{
			assert(!done);
			Queue<LocalDateTime> queue = new LinkedList<>();
			queue.add(timestamp);
			eventOccurances.put(eventType, queue);
			decrementRemaining(eventType);
		}
	}

	private void decrementRemaining(AnnotatedEventType eventType) {
		if(remaining.get(eventType)>1){
			remaining.put(eventType,remaining.get(eventType)-1);
		} else{
			remaining.remove(eventType);
		}
	}

	@Override
	public EpisodePattern getEpsiodePattern() {
		return pattern;
	}

}
