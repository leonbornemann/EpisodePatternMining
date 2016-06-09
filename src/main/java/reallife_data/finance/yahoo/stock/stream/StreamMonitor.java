package reallife_data.finance.yahoo.stock.stream;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.stream.Collectors;

import episode.finance.ContinousSerialEpisodeRecognitionDFA;
import episode.finance.EpisodePattern;
import episode.finance.ContinousEpisodeRecognitionDFA;
import episode.finance.SerialEpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import util.Pair;

public class StreamMonitor {
	
	private Map<ContinousEpisodeRecognitionDFA,Integer> trustScores = new HashMap<>();
	private PriorityQueue<PredictorOccuranceState> predictorOccuranceExpiryTimes = new PriorityQueue<>((a,b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
	private AnnotatedEventStream stream;
	private AnnotatedEventType toPredict;
	private int episodeDuration;
	
	public StreamMonitor(Map<EpisodePattern, Integer> predictors, AnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration) {
		for(EpisodePattern pattern : predictors.keySet()){
			trustScores.put(pattern.getContinousDFA(), predictors.get(pattern));
		}
		this.toPredict = toPredict;
		this.episodeDuration = episodeDuration;
		this.stream = stream;
	}
	
	public void monitor() throws IOException{
		while(stream.hasNext()){
			AnnotatedEvent curEvent = stream.next();
			while(predictorOccuranceExpiryTimes.peek() != null && isSmaller(predictorOccuranceExpiryTimes.peek().getExpiryTime(),curEvent.getTimestamp())){
				PredictorOccuranceState expired = predictorOccuranceExpiryTimes.poll();
				if(!expired.hadOccurance()){
					penalize(expired.getDFA());
				}
			}
			if(curEvent.getEventType().equals(toPredict)){
				predictorOccuranceExpiryTimes.forEach(e -> {
					reward(e.getDFA());
					e.setOccurance(true);
				}); //Reward
			} else if(curEvent.getEventType().equals(toPredict.getInverseEvent())){
				predictorOccuranceExpiryTimes.forEach(e -> {
					penalize(e.getDFA());
					e.setOccurance(true);
				}); //Reward
			}
			//advance automata
			for(ContinousEpisodeRecognitionDFA automaton : trustScores.keySet()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					addPredictorOccurance(automaton,occurance.getFirst());
				}
			}
		}
	}

	private Integer penalize(ContinousEpisodeRecognitionDFA dfa) {
		return trustScores.put(dfa, trustScores.get(dfa)-1);
	}

	private Integer reward(ContinousEpisodeRecognitionDFA dfa) {
		return trustScores.put(dfa, trustScores.get(dfa)+1);
	}

	private boolean isSmaller(LocalDateTime first, LocalDateTime second) {
		return first.compareTo(second) < 0;
	}

	private void addPredictorOccurance(ContinousEpisodeRecognitionDFA automaton, LocalDateTime start) {
		predictorOccuranceExpiryTimes.add(new PredictorOccuranceState(automaton,getExpiryTime(start)));
	}

	private LocalDateTime getExpiryTime(LocalDateTime start) {
		return start.plus(episodeDuration, ChronoUnit.SECONDS);
	}

	public Map<EpisodePattern, Integer> getCurrentTrustScores() {
		return trustScores.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getEpsiodePattern(), e -> e.getValue()));
	}
	
	
}
