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

	public void eval(int d, File predictionsTargetFile, File lowLevelStreamDirDesktop, String targetCompanyID, File evaluationResultFile) throws IOException {
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
		evalMetricsAndRateOfReturn(byDay,filenames,evaluationResultFile);
	}
	
	private void evalMetricsAndRateOfReturn(Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay,
			Map<LocalDate, String> filenames, File evaluationResultFile) throws IOException {
		EvaluationResult result = new EvaluationResult();
		PredictorPerformance totalPerformance = new PredictorPerformance();
		double thisDayStartingPrice = 93.57; //TODO: get the starting price from somewhere
		InvestmentTracker tracker = new InvestmentTracker(thisDayStartingPrice);
		double startingInvestment = tracker.netWorth();
		for(LocalDate day : byDay.keySet().stream().sorted().collect(Collectors.toList())){
			PredictorPerformance thisDayPerformance = new PredictorPerformance();
			List<Pair<LocalDateTime, Double>> targetMovement = getTargetPriceMovement(filenames.get(day));
			evalMetricsForDay(byDay.get(day),targetMovement,thisDayPerformance);
			totalPerformance.addAllExamples(thisDayPerformance);
			evalRateOfReturnForDay(byDay.get(day),targetMovement,tracker);
			//TODO: save return of investment for this day
			System.out.println("--------------------------------------------------------------------");
			System.out.println("Results for day " + day.format(StandardDateTimeFormatter.getStandardDateFormatter()));
			print(thisDayPerformance);
			System.out.println("Return of investment: " + tracker.rateOfReturn());
			result.putReturnOfInvestment(day,tracker.rateOfReturn());
			result.putMetricPerformance(day,thisDayPerformance);
			System.out.println("--------------------------------------------------------------------");
			tracker = new InvestmentTracker(tracker.getPrice(), tracker.netWorth());
		}
		result.setTotalReturnOfInvestment(tracker.rateOfReturn(startingInvestment));
		print(totalPerformance);
		System.out.println("total return of investment: " + tracker.rateOfReturn(startingInvestment));
		result.serialize(evaluationResultFile);
	}

	private void print(PredictorPerformance perf) {
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

	private void evalMetricsForDay(List<Pair<LocalDateTime, Change>> predictions, List<Pair<LocalDateTime, Double>> targetMovement, PredictorPerformance perf) {
		for (int i = 0; i < predictions.size(); i++) {
			Pair<LocalDateTime, Change> curPrediction = predictions.get(i);
			Change actualValue = getActualValue(curPrediction.getFirst(),targetMovement);
			perf.addTestExample(curPrediction.getSecond(), actualValue);
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

	private List<Pair<LocalDateTime, Double>> getTargetPriceMovement(String filename) throws IOException {
		File file = buildFile(filename, lowLevelStreamDirDesktop);
		List<LowLevelEvent> events= LowLevelEvent.readAll(file);
		return events.stream()
			.filter(e -> e.getCompanyId().equals(targetCompanyID))
			.sorted(LowLevelEvent::temporalOrder)
			.map(e -> new Pair<>(e.getTimestamp(),e.getValue()))
			.collect(Collectors.toList());
	}

	private void evalRateOfReturnForDay(List<Pair<LocalDateTime, Change>> pred,List<Pair<LocalDateTime, Double>> targetMovement, InvestmentTracker tracker) {
		Collections.sort(pred, (a,b) -> a.getFirst().compareTo(b.getFirst()));
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
		tracker.sellIfPossible();
	}

	private double getInitialPrice(Map<LocalDate, List<Pair<LocalDateTime, Double>>> targetPriceMovement) {
		LocalDate firstDate = targetPriceMovement.keySet().stream().sorted().iterator().next();
		Collections.sort(targetPriceMovement.get(firstDate), (a,b) -> a.getFirst().compareTo(b.getFirst()));
		return targetPriceMovement.get(firstDate).stream().iterator().next().getSecond();
	}

	private void processPredictionEvent(InvestmentTracker tracker, Pair<LocalDateTime, Change> predEventPair) {
		if(predEventPair.getSecond()==Change.UP){
			tracker.buyIfPossible();
		} else if(predEventPair.getSecond()==Change.DOWN){
			tracker.sellIfPossible();
		} else{
			//hold
		}
	}

	private void processTargetMovement(InvestmentTracker tracker,Pair<LocalDateTime, Double> targetMovement) {
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
