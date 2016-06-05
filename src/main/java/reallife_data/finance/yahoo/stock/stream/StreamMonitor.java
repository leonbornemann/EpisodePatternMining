package reallife_data.finance.yahoo.stock.stream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.ContinousEpisodeRecognitionDFA;
import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import util.Pair;

public class StreamMonitor {
	
	private Map<ContinousEpisodeRecognitionDFA,Integer> trustScores = new HashMap<>();
	private PriorityQueue<Pair<ContinousEpisodeRecognitionDFA,LocalDateTime>> predictorOccuranceExpiryTimes = new PriorityQueue<>((a,b) -> a.getSecond().compareTo(b.getSecond()));
	private MultiFileAnnotatedEventStream stream;
	private AnnotatedEventType toPredict;
	private int episodeDuration;
	
	public StreamMonitor(Map<SerialEpisodePattern, Integer> predictors, MultiFileAnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration) {
		for(SerialEpisodePattern pattern : predictors.keySet()){
			trustScores.put(pattern.getContinousDFA(), predictors.get(pattern));
		}
		this.toPredict = toPredict;
		this.episodeDuration = episodeDuration;
		this.stream = stream;
	}
	
	public void monitor() throws IOException{
		while(stream.hasNext()){
			AnnotatedEvent curEvent = stream.next();
			while(predictorOccuranceExpiryTimes.peek() != null && isSmaller(predictorOccuranceExpiryTimes.peek().getSecond(),curEvent.getTimestamp())){
				predictorOccuranceExpiryTimes.poll();
			}
			if(curEvent.getEventType().equals(toPredict)){
				predictorOccuranceExpiryTimes.forEach(e -> trustScores.put(e.getFirst(), trustScores.get(e.getFirst())+1)); //Reward
			} else if(curEvent.getEventType().equals(toPredict)){
				predictorOccuranceExpiryTimes.forEach(e -> trustScores.put(e.getFirst(), trustScores.get(e.getFirst())-1)); //Penalize
			} //TODO: look at the case that we never find anything!
			
			//advance automata
			for(ContinousEpisodeRecognitionDFA automaton : trustScores.keySet()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					addPredictorOccurance(automaton,occurance.getFirst());
				}
			}
		}
	}

	private boolean isSmaller(LocalDateTime first, LocalDateTime second) {
		return first.compareTo(second) < 0;
	}

	private void addPredictorOccurance(ContinousEpisodeRecognitionDFA automaton, LocalDateTime start) {
		predictorOccuranceExpiryTimes.add(new Pair<>(automaton,getExpiryTime(start)));
	}

	private LocalDateTime getExpiryTime(LocalDateTime start) {
		return start.plus(episodeDuration, ChronoUnit.SECONDS);
	}

	public Map<SerialEpisodePattern, Integer> getCurrentTrustScores() {
		return trustScores.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getEpsiodePattern(), e -> e.getValue()));
	}
	
	
}
