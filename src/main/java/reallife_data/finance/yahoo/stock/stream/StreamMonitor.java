package reallife_data.finance.yahoo.stock.stream;

import java.io.File;
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
import reallife_data.finance.yahoo.stock.util.IOService;
import util.Pair;

public class StreamMonitor {
	
	private Map<ContinousEpisodeRecognitionDFA,Integer> trustScores = new HashMap<>();
	private PriorityQueue<PredictorOccuranceState> predictorOccuranceExpiryTimes = new PriorityQueue<>((a,b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
	private PriorityQueue<PredictorOccuranceState> inversePredictorOccuranceExpiryTimes = new PriorityQueue<>((a,b) -> a.getExpiryTime().compareTo(b.getExpiryTime()));
	private AnnotatedEventStream stream;
	private AnnotatedEventType toPredict;
	private int episodeDuration;
	private int toPredictCount = 0;
	private int inverseToPredictCount = 0;
	private int ensembleScore = 0;
	private File performanceLog;
	private InvestmentTracker investmentTracker;
	private Map<ContinousEpisodeRecognitionDFA, Integer> inverseTrustScores = new HashMap<>();
	
	public StreamMonitor(Map<EpisodePattern, Integer> predictors,Map<EpisodePattern, Integer> inversePredictors, AnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration){
		this(predictors,inversePredictors,stream,toPredict,episodeDuration,null);
	}
	
	public StreamMonitor(Map<EpisodePattern, Integer> predictors,Map<EpisodePattern, Integer> inversePredictors, AnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration, File performanceLog) {
		for(EpisodePattern pattern : predictors.keySet()){
			trustScores.put(pattern.getContinousDFA(), predictors.get(pattern));
		}
		for(EpisodePattern pattern : inversePredictors.keySet()){
			inverseTrustScores.put(pattern.getContinousDFA(), predictors.get(pattern));
		}
		this.toPredict = toPredict;
		this.episodeDuration = episodeDuration;
		this.stream = stream;
		this.performanceLog = performanceLog;
		investmentTracker = new InvestmentTracker(0.001);
	}
	
	public void monitor() throws IOException{
		while(stream.hasNext()){
			AnnotatedEvent curEvent = stream.next();
			handleExpiration(curEvent, predictorOccuranceExpiryTimes);
			handleExpiration(curEvent, inversePredictorOccuranceExpiryTimes);
			//TODO: also process the inverse predictors here!
			if(curEvent.getEventType().equals(toPredict)){
				investmentTracker.up();
				toPredictCount++;
				if(!predictorOccuranceExpiryTimes.isEmpty()){
					rewardEnsemble();
					predictorOccuranceExpiryTimes.forEach(e -> {
						reward(e.getDFA());
						e.setOccurance(true);
					}); //Reward individual predictors
				} else{
					penalizeEnsemble();
				}
				logStatus();
			} else if(curEvent.getEventType().equals(toPredict.getInverseEvent())){
				investmentTracker.down();
				inverseToPredictCount++;
				if(!predictorOccuranceExpiryTimes.isEmpty()){
					penalizeEnsemble();
					predictorOccuranceExpiryTimes.forEach(e -> {
						penalize(e.getDFA());
						e.setOccurance(true);
					}); //Reward
				} else{
					//rewardEnsemble();
				}
				logStatus();
			}
			//advance automata
			for(ContinousEpisodeRecognitionDFA automaton : trustScores.keySet()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					investmentTracker.buyIfPossible();
					addPredictorOccurance(automaton,occurance.getFirst(), predictorOccuranceExpiryTimes);
				}
			}
			//other automata:
			for(ContinousEpisodeRecognitionDFA automaton : inverseTrustScores.keySet()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					investmentTracker.sellIfPossible();
					addPredictorOccurance(automaton,occurance.getFirst(), inversePredictorOccuranceExpiryTimes);
				}
			}
		}
	}

	private void handleExpiration(AnnotatedEvent curEvent, PriorityQueue<PredictorOccuranceState> occuranceExpiryTimes) {
		while(occuranceExpiryTimes.peek() != null && isSmaller(occuranceExpiryTimes.peek().getExpiryTime(),curEvent.getTimestamp())){
			PredictorOccuranceState expired = occuranceExpiryTimes.poll();
			if(!expired.hadOccurance()){
				//penalize(expired.getDFA());
				//penalizeEnsemble();
				//logStatus();
			}
		}
	}

	private void logStatus() {
		if(performanceLog!=null){
			IOService.writeLogEntry(performanceLog.getAbsolutePath(),"("+ toPredictCount +","+ inverseToPredictCount +","+ ensembleScore +")");
		}
	}

	private void penalizeEnsemble() {
		ensembleScore--;
	}

	private void rewardEnsemble() {
		ensembleScore++;
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

	private void addPredictorOccurance(ContinousEpisodeRecognitionDFA automaton, LocalDateTime start, PriorityQueue<PredictorOccuranceState> expiryTimes) {
		expiryTimes.add(new PredictorOccuranceState(automaton,getExpiryTime(start)));
	}

	private LocalDateTime getExpiryTime(LocalDateTime start) {
		return start.plus(episodeDuration, ChronoUnit.SECONDS);
	}

	public Map<EpisodePattern, Integer> getCurrentTrustScores() {
		return trustScores.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().getEpsiodePattern(), e -> e.getValue()));
	}

	public InvestmentTracker getInvestmentTracker() {
		return investmentTracker;
	}
	
	
}
