package prediction.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import data.Change;
import data.stream.PredictorPerformance;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;


public class AggregatedEvaluator extends Evaluator{

	public void eval(int d, File predictionsTargetFile, File targetMovementTargetFile) throws IOException {
		List<Pair<LocalDateTime, Change>> predictions = deserializePairList(predictionsTargetFile);
		List<Pair<LocalDateTime, Change>> targetMovement = deserializePairList(targetMovementTargetFile);
		evalRateOfReturn(predictions, targetMovement);
		evalMetrics(predictions,targetMovement,d);
	}

	private void evalMetrics(List<Pair<LocalDateTime, Change>> predictions,List<Pair<LocalDateTime, Change>> targetMovement, int d) {
		PredictorPerformance perf = new PredictorPerformance();
		for (int i = 0; i < predictions.size(); i++) {
			Pair<LocalDateTime, Change> curPrediction = predictions.get(i);
			Change actualValue = getActualValue(curPrediction.getFirst(),targetMovement,d);
			perf.addTestExample(curPrediction.getSecond(), actualValue);
		}
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
		//System.out.println("Accuracy: "+ perf.getAccuracy(Change.UP));

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

	private void evalRateOfReturn(List<Pair<LocalDateTime, Change>> predictions,
			List<Pair<LocalDateTime, Change>> targetMovement) {
		PercentageBasedInvestmentTracker tracker = new PercentageBasedInvestmentTracker(0.001);
		int predIndex = 0;
		int targetMovementIndex = 0;
		System.out.println("starting price: " + tracker.netWorth());
		while(true){
			if(predIndex==predictions.size() && targetMovementIndex == targetMovement.size()){
				break;
			} else if(predIndex==predictions.size()){
				Pair<LocalDateTime, Change> targetMovementEventPair = targetMovement.get(targetMovementIndex);
				processTargetMovement(tracker, targetMovementEventPair);
				targetMovementIndex++;
			} else if(targetMovementIndex == targetMovement.size()){
				Pair<LocalDateTime, Change> predEventPair = predictions.get(predIndex);
				processPredictionEvent(tracker, predEventPair);
				predIndex++;
			} else{
				LocalDateTime predElement = predictions.get(predIndex).getFirst();
				LocalDateTime targetElement = targetMovement.get(targetMovementIndex).getFirst();
				if(predElement.compareTo(targetElement)<0){
					//process predElement
					Pair<LocalDateTime, Change> predEventPair = predictions.get(predIndex);
					processPredictionEvent(tracker, predEventPair);
					predIndex++;
				} else{
					//process targetElement
					Pair<LocalDateTime, Change> targetMovementEventPair = targetMovement.get(targetMovementIndex);
					processTargetMovement(tracker, targetMovementEventPair);
					targetMovementIndex++;
				}
			}
		}
		long upCount = targetMovement.stream().map(e -> e.getSecond()).filter(e -> e==Change.UP).count();
		long downCount = targetMovement.stream().map(e -> e.getSecond()).filter(e -> e==Change.DOWN).count();
		System.out.println("Num upwards movements: " + upCount);
		System.out.println("Num downwards movements: " + downCount);
		System.out.println("Ending price: " + tracker.netWorth());
		System.out.println("rate of return: " + tracker.rateOfReturn());
	}

	private void processPredictionEvent(PercentageBasedInvestmentTracker tracker, Pair<LocalDateTime, Change> predEventPair) {
		if(predEventPair.getSecond()==Change.UP){
			tracker.buyIfPossible();
		} else if(predEventPair.getSecond()==Change.DOWN){
			tracker.sellIfPossible();
		} else{
			//hold
		}
	}

	private void processTargetMovement(PercentageBasedInvestmentTracker tracker,
			Pair<LocalDateTime, Change> targetMovementEventPair) {
		if(targetMovementEventPair.getSecond()==Change.UP){
			tracker.up();
		} else{
			assert(targetMovementEventPair.getSecond()==Change.DOWN);
			tracker.down();
		}
	}

}
