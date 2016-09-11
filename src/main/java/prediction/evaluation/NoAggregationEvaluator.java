package prediction.evaluation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import prediction.data.Change;
import prediction.data.LowLevelEvent;
import prediction.data.stream.PredictorPerformance;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class NoAggregationEvaluator extends Evaluator{

	private File lowLevelStreamDirDesktop;
	private String targetCompanyID;
	private int d;

	public void eval(int d, File predictionsTargetFile, File lowLevelStreamDirDesktop, String targetCompanyID) throws IOException {
		this.d= d;
		this.targetCompanyID = "\""+targetCompanyID+"\"";
		this.lowLevelStreamDirDesktop = lowLevelStreamDirDesktop;
		List<Pair<LocalDateTime, Change>> predictions = deserializePairList(predictionsTargetFile);
		predictions = predictions.stream().filter(p -> isInTimBounds(p.getFirst())).collect(Collectors.toList());
		Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictions.stream().collect(Collectors.groupingBy(p -> LocalDate.from(p.getFirst())));
		Map<LocalDate,String> filenames = byDay.keySet().stream().collect(Collectors.toMap(k -> k, k -> "NASDAQ_" + k.format(StandardDateTimeFormatter.getStandardDateFormatter()) + ".csv"));
		for (String filename : filenames.values()) {
			File file = buildFile(filename,lowLevelStreamDirDesktop);
			assert(file.exists());
		}
		evalMetrics(byDay,filenames);
		evalRateOfReturn(byDay,filenames);
	}
	
	private void evalMetrics(Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay, Map<LocalDate, String> filenames){
		PredictorPerformance perf = new PredictorPerformance();
		byDay.keySet().stream().sorted().forEachOrdered(day -> evalMetricsForDay(byDay.get(day),filenames.get(day),perf));

		
		System.out.println("Values for Equal:");
		System.out.println("Precision: "+ perf.getPrecision(Change.EQUAL));
		System.out.println("Recall: "+ perf.getRecall(Change.EQUAL));
		//System.out.println("Accuracy: "+ perf.getAccuracy(Change.EQUAL));
		System.out.println("Values for DOWN:");
		System.out.println("Precision: "+ perf.getPrecision(Change.DOWN));
		System.out.println("Recall: "+ perf.getRecall(Change.DOWN));
		//System.out.println("Accuracy: "+ perf.getAccuracy(Change.DOWN));
		System.out.println("Values for UP:");
		System.out.println("Precision: "+ perf.getPrecision(Change.UP));
		System.out.println("Recall: "+ perf.getRecall(Change.UP));
		perf.printConfusionMatrix();
	}
	

	private void evalMetricsForDay(List<Pair<LocalDateTime, Change>> predictions, String filename, PredictorPerformance perf) {
		try{
			List<Pair<LocalDateTime, Double>> targetMovement = getTargetPriceMovement(filename);
			for (int i = 0; i < predictions.size(); i++) {
				Pair<LocalDateTime, Change> curPrediction = predictions.get(i);
				Change actualValue = getActualValue(curPrediction.getFirst(),targetMovement);
				perf.addTestExample(curPrediction.getSecond(), actualValue);
			}
		} catch(IOException e){
			throw new AssertionError();
		}
	}

	private Change getActualValue(LocalDateTime predictionTime, List<Pair<LocalDateTime, Double>> targetMovement) {
		int i=0;
		Pair<LocalDateTime, Double> curElem = targetMovement.get(i);
		while(curElem.getFirst().compareTo(predictionTime)<=0 && i<targetMovement.size()){
			curElem = targetMovement.get(i);
			i++;
		}
		if(i==targetMovement.size()){
			return Change.EQUAL;
		} else{
			double initial = targetMovement.get(i-1).getSecond();
			double end = targetMovement.get(i).getSecond();
			while(curElem.getFirst().compareTo(predictionTime.plus(d,ChronoUnit.SECONDS))  <= 0 && i<targetMovement.size()){
				curElem = targetMovement.get(i);
				end = curElem.getSecond();
				i++;
			}
			if(end>initial){
				return Change.UP;
			} else if(initial>end){
				return Change.DOWN;
			} else{
				return Change.EQUAL;
			}
		}
	}
	
	private Change getActualValue(LocalDateTime predictionTime, List<Pair<LocalDateTime, Change>> targetMovement,int d) {
		int i=0;
		Pair<LocalDateTime, Change> curElem = targetMovement.get(i);
		while(curElem.getFirst().compareTo(predictionTime)<=0 && i<targetMovement.size()){
			curElem = targetMovement.get(i);
			i++;
		}
		if(i==targetMovement.size()){
			return Change.EQUAL;
		} else{
			int change = 0;
			while(curElem.getFirst().compareTo(predictionTime.plus(d,ChronoUnit.SECONDS))  <= 0 && i<targetMovement.size()){
				curElem = targetMovement.get(i);
				if(curElem.getSecond()==Change.UP){
					change++;
				} else if(curElem.getSecond()==Change.DOWN){
					change--;
				}
				i++;
			}
			if(change>0){
				return Change.UP;
			} else if(change<0){
				return Change.DOWN;
			} else{
				return Change.EQUAL;
			}
		}
	}

	private List<Pair<LocalDateTime, Double>> getTargetPriceMovement(String filename) throws IOException {
		File file = buildFile(filename, lowLevelStreamDirDesktop);
		List<LowLevelEvent> events= LowLevelEvent.readAll(file);
		return events.stream()
			.filter(e -> e.getCompanyId().equals(targetCompanyID))
			.sorted(LowLevelEvent::temporalOrder)
			.map(e -> new Pair<>(e.getTimestamp(),e.getValue()))
			.collect(Collectors.toList());
	}

	private void evalRateOfReturn(Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay,Map<LocalDate, String> filenames) throws IOException {
		InverstmentTracker tracker = new InverstmentTracker(93.24);
		System.out.println("starting price: " + tracker.netWorth());
		byDay.keySet().stream().sorted().forEachOrdered(day -> evalRateOfReturnForDay(byDay.get(day),filenames.get(day),tracker));
		System.out.println("Ending price: " + tracker.netWorth());
		System.out.println("rate of return: " + tracker.rateOfReturn());
	}

	private void evalRateOfReturnForDay(List<Pair<LocalDateTime, Change>> pred,String filenameForThisDay, InverstmentTracker tracker) {
		try {
			Collections.sort(pred, (a,b) -> a.getFirst().compareTo(b.getFirst()));
			List<Pair<LocalDateTime, Double>> targetMovement;
			targetMovement = getTargetPriceMovement(filenameForThisDay);
			Collections.sort(targetMovement, (a,b) -> a.getFirst().compareTo(b.getFirst()));
			int predIndex = 0;
			int targetMovementIndex = 0;
			while(true){
				if(predIndex==pred.size() && targetMovementIndex == targetMovement.size()){
					break;
				} else if(predIndex==pred.size()){
					Pair<LocalDateTime, Double> targetMovementEventPair = targetMovement.get(targetMovementIndex);
					processTargetMovement(tracker, targetMovementEventPair);
					targetMovementIndex++;
				} else if(targetMovementIndex == targetMovement.size()){
					Pair<LocalDateTime, Change> predEventPair = pred.get(predIndex);
					processPredictionEvent(tracker, predEventPair);
					predIndex++;
				} else{
					LocalDateTime predElement = pred.get(predIndex).getFirst();
					LocalDateTime targetElement = targetMovement.get(targetMovementIndex).getFirst();
					if(predElement.compareTo(targetElement)<0){
						//process predElement
						Pair<LocalDateTime, Change> predEventPair = pred.get(predIndex);
						processPredictionEvent(tracker, predEventPair);
						predIndex++;
					} else{
						//process targetElement
						Pair<LocalDateTime, Double> targetMovementEventPair = targetMovement.get(targetMovementIndex);
						processTargetMovement(tracker, targetMovementEventPair);
						targetMovementIndex++;
					}
				}
			}
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		//System.out.println("Ending price: " + tracker.netWorth());
		//System.out.println("rate of return: " + tracker.rateOfReturn());
	}

	private double getInitialPrice(Map<LocalDate, List<Pair<LocalDateTime, Double>>> targetPriceMovement) {
		LocalDate firstDate = targetPriceMovement.keySet().stream().sorted().iterator().next();
		Collections.sort(targetPriceMovement.get(firstDate), (a,b) -> a.getFirst().compareTo(b.getFirst()));
		return targetPriceMovement.get(firstDate).stream().iterator().next().getSecond();
	}

	private void processPredictionEvent(InverstmentTracker tracker, Pair<LocalDateTime, Change> predEventPair) {
		if(predEventPair.getSecond()==Change.UP){
			tracker.buyIfPossible();
		} else if(predEventPair.getSecond()==Change.DOWN){
			tracker.sellIfPossible();
		} else{
			//hold
		}
	}

	private void processTargetMovement(InverstmentTracker tracker,Pair<LocalDateTime, Double> targetMovement) {
		tracker.setPrice(targetMovement.getSecond());
	}

	private boolean isInTimBounds(LocalDateTime first) {
		LocalTime border = LocalTime.of(15, 0);
		return LocalTime.from(first).compareTo(border) > 0;
	}

	private File buildFile(String filename, File lowLevelStreamDirDesktop) {
		return new File(lowLevelStreamDirDesktop.getAbsolutePath() + File.separator + filename); 
	}

}
