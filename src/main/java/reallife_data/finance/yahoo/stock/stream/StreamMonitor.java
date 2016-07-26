package reallife_data.finance.yahoo.stock.stream;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.stream.Collectors;

import episode.finance.ContinousEpisodeRecognitionDFA;
import episode.finance.EpisodePattern;
import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.AnnotatedEventType;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.util.IOService;
import util.Pair;

public class StreamMonitor {
	
	private Map<EpisodePattern,PredictorPerformance> trustScores = new HashMap<>();
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
	private Map<EpisodePattern, PredictorPerformance> inverseTrustScores = new HashMap<>();
	private Map<EpisodePattern, ContinousEpisodeRecognitionDFA> automata = new HashMap<>();
	private Map<EpisodePattern, ContinousEpisodeRecognitionDFA> inverseAutomata = new HashMap<>();
	
	public StreamMonitor(Map<EpisodePattern, Integer> predictors,Map<EpisodePattern, Integer> inversePredictors, AnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration){
		this(predictors,inversePredictors,stream,toPredict,episodeDuration,null);
	}
	
	public StreamMonitor(Map<EpisodePattern, Integer> predictors,Map<EpisodePattern, Integer> inversePredictors, AnnotatedEventStream stream, AnnotatedEventType toPredict, int episodeDuration, File performanceLog) {
		for(EpisodePattern pattern : predictors.keySet()){
			trustScores.put(pattern, new PredictorPerformance()); //TODO: are predictors now passed with PredictorPreformances?
			automata.put(pattern,pattern.getContinousDFA());
		}
		for(EpisodePattern pattern : inversePredictors.keySet()){
			inverseTrustScores.put(pattern, new PredictorPerformance());
			inverseAutomata .put(pattern,pattern.getContinousDFA());
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
				handleTargetEventOccurrence();
			} else if(curEvent.getEventType().equals(toPredict.getInverseEvent())){
				handleInverseTargeEventOccurrence();
			}
			//advance automata
			for(ContinousEpisodeRecognitionDFA automaton : automata.values()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					investmentTracker.buyIfPossible();
					addPredictorOccurance(automaton,occurance.getFirst(), predictorOccuranceExpiryTimes);
				}
			}
			//other automata:
			for(ContinousEpisodeRecognitionDFA automaton : inverseAutomata.values()){
				Pair<LocalDateTime, LocalDateTime> occurance = automaton.processEvent(curEvent);
				if(occurance!=null){
					investmentTracker.sellIfPossible();
					addPredictorOccurance(automaton,occurance.getFirst(), inversePredictorOccuranceExpiryTimes);
				}
			}
		}
	}

	private void handleTargetEventOccurrence() {
		changePrice();
		toPredictCount++;
		Set<EpisodePattern> occurredPredictors = getOccurredPatterns(predictorOccuranceExpiryTimes);
		Set<EpisodePattern> occurredInversePredictors = getOccurredPatterns(inversePredictorOccuranceExpiryTimes);
		occurredPredictors.forEach(e -> trustScores.get(e).incTruePositives()); //Reward individual predictors
		occurredInversePredictors.forEach(e -> inverseTrustScores.get(e).incFalsePositives()); //TODO: is that actually false positives?
		//false negatives
		HashSet<EpisodePattern> nonOccurringPredictors = new HashSet<EpisodePattern>(trustScores.keySet());
		nonOccurringPredictors.removeAll(occurredPredictors);
		nonOccurringPredictors.forEach(e -> trustScores.get(e).incFalseNegatives());
		//true negatives:
		HashSet<EpisodePattern> nonOccurringInversePredictors = new HashSet<EpisodePattern>(inverseTrustScores.keySet());
		nonOccurringInversePredictors.removeAll(occurredInversePredictors);
		nonOccurringInversePredictors.forEach(e -> inverseTrustScores.get(e).incTrueNegatives());
		logStatus();
	}

	
	private void handleInverseTargeEventOccurrence() {
		changePrice();
		toPredictCount++;
		Set<EpisodePattern> occurredPredictors = getOccurredPatterns(predictorOccuranceExpiryTimes);
		Set<EpisodePattern> occurredInversePredictors = getOccurredPatterns(inversePredictorOccuranceExpiryTimes);
		occurredPredictors.forEach(e -> trustScores.get(e).incFalsePositives()); //Reward individual predictors
		occurredInversePredictors.forEach(e -> inverseTrustScores.get(e).incTruePositives()); //TODO: is that actually false positives?
		//false negatives
		HashSet<EpisodePattern> nonOccurringPredictors = new HashSet<EpisodePattern>(trustScores.keySet());
		nonOccurringPredictors.removeAll(occurredPredictors);
		nonOccurringPredictors.forEach(e -> trustScores.get(e).incTrueNegatives());
		//true negatives:
		HashSet<EpisodePattern> nonOccurringInversePredictors = new HashSet<EpisodePattern>(inverseTrustScores.keySet());
		nonOccurringInversePredictors.removeAll(occurredInversePredictors);
		nonOccurringInversePredictors.forEach(e -> inverseTrustScores.get(e).incFalseNegatives());
		logStatus();
	}

	private Set<EpisodePattern> getOccurredPatterns(PriorityQueue<PredictorOccuranceState> priorityQueue) {
		return priorityQueue.stream().map(e -> e.getDFA().getEpsiodePattern()).collect(Collectors.toSet());
	}

	private void changePrice() {
		if(toPredict.getChange()==Change.UP){
			investmentTracker.up();
		} else if(toPredict.getChange()==Change.DOWN){
			investmentTracker.down();
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

	private boolean isSmaller(LocalDateTime first, LocalDateTime second) {
		return first.compareTo(second) < 0;
	}

	private void addPredictorOccurance(ContinousEpisodeRecognitionDFA automaton, LocalDateTime start, PriorityQueue<PredictorOccuranceState> expiryTimes) {
		expiryTimes.add(new PredictorOccuranceState(automaton,getExpiryTime(start)));
	}

	private LocalDateTime getExpiryTime(LocalDateTime start) {
		return start.plus(episodeDuration, ChronoUnit.SECONDS);
	}

	public Map<EpisodePattern, PredictorPerformance> getCurrentTrustScores() {
		return trustScores;
	}

	public InvestmentTracker getInvestmentTracker() {
		return investmentTracker;
	}

	public Map<EpisodePattern, PredictorPerformance> getCurrentInverseTrustScores() {
		return inverseTrustScores;
	}
	
	
}
