package prediction.evaluation;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import prediction.data.Change;
import prediction.data.LowLevelEvent;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class NoAggregationEvaluator extends Evaluator{

	private File lowLevelStreamDirDesktop;
	private String targetCompanyID;

	public void eval(int d, File predictionsTargetFile, File lowLevelStreamDirDesktop, String targetCompanyID) throws IOException {
		this.targetCompanyID = "\""+targetCompanyID+"\"";
		this.lowLevelStreamDirDesktop = lowLevelStreamDirDesktop;
		List<Pair<LocalDateTime, Change>> predictions = deserializePairList(predictionsTargetFile);
		predictions = predictions.stream().filter(p -> isInTimBounds(p.getFirst())).collect(Collectors.toList());
		Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictions.stream().collect(Collectors.groupingBy(p -> LocalDate.from(p.getFirst())));
		Map<LocalDate,String> filenames = byDay.keySet().stream().collect(Collectors.toMap(k -> k, k -> "NASDAQ_" + k.format(StandardDateTimeFormatter.getStandardDateFormatter()) + ".csv"));
		for (String filename : filenames.values()) {
			File file = buildFile(filename,lowLevelStreamDirDesktop);
			System.out.println(file.getAbsolutePath());
			System.out.println(file.exists());
			assert(file.exists());
		}
		evalRateOfReturn(byDay,filenames);
	}
	

	private List<Pair<LocalDateTime, Double>> getTargetPriceMovement(String filename) throws IOException {
		File file = buildFile(filename, lowLevelStreamDirDesktop);
		List<LowLevelEvent> a = LowLevelEvent.readAll(file);
		Set<String> allCompanies = a.stream().map(e -> e.getCompanyId()).collect(Collectors.toSet());
		return a.stream()
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
