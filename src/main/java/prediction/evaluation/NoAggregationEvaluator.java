package prediction.evaluation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.Change;
import data.stream.PredictorPerformance;
import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class NoAggregationEvaluator extends Evaluator{

	private File lowLevelStreamDir;
	private int d;
	private Map<String, List<Pair<LocalDateTime, BigDecimal>>> companyMovements;
	
	public NoAggregationEvaluator(int d,File lowLevelStreamDir) throws IOException{
		this.d = d;
		if(lowLevelStreamDir!=null){
			this.lowLevelStreamDir = lowLevelStreamDir;
			companyMovements = initCompanyMovements();
			assertTimeSeriesSorted();
		} else{
			//just for unit testing methods:
			
		}
		
	}

	private void assertTimeSeriesSorted() {
		for(String id:companyMovements.keySet()){
			List<Pair<LocalDateTime, BigDecimal>> timeSeries = companyMovements.get(id);
			for(int i=1;i<timeSeries.size();i++){
				assert(timeSeries.get(0).getFirst().compareTo(timeSeries.get(i).getFirst())<=0);
			}
		}
	}

	private Map<String, List<Pair<LocalDateTime, BigDecimal>>> initCompanyMovements() throws IOException {
		List<File> allFiles = Arrays.asList(lowLevelStreamDir.listFiles());
		Map<String, List<Pair<LocalDateTime, BigDecimal>>> companyMovements = new HashMap<>();
		for(File file : allFiles){
			if(file.isFile() && file.getName().endsWith(".csv")){
				companyMovements.put(file.getName().split("\\.")[0], IOService.readTimeSeriesData(file));
			}
		}
		return companyMovements;
	}

	public void eval(List<EvaluationFiles> companies) throws IOException {
		Map<String,Map<LocalDate, List<Pair<LocalDateTime, Change>>>> predictionsByCompanyByDay = getOrganizedPredictions(companies);
		Map<String,EvaluationResult> results = new HashMap<>();
		Map<String,InvestmentTracker> trackers = new HashMap<>();
		predictionsByCompanyByDay.keySet().stream().forEach(id -> {
			results.put(id, new EvaluationResult());
			trackers.put(id, new InvestmentTracker(getStartingPrice(id)));
		});
		List<LocalDate> daysSorted = getAllDates().stream().sorted().collect(Collectors.toList());
		for (LocalDate day : daysSorted) {
			System.out.println("Results for day " + day.format(StandardDateTimeFormatter.getStandardDateFormatter()));
			System.out.println("--------------------------------------------------------------------");
			for(String companyID : predictionsByCompanyByDay.keySet().stream().sorted().collect(Collectors.toList())){
				System.out.println("--------------------------------------------------------------------");
				System.out.println("Results for company " + companyID);
				EvaluationResult result = results.get(companyID);
				InvestmentTracker tracker = trackers.get(companyID);
				Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictionsByCompanyByDay.get(companyID);
				List<Pair<LocalDateTime, BigDecimal>> targetMovement = getTargetPriceMovementForDay(companyID,day);
				if(byDay.get(day)!=null && !targetMovement.isEmpty()){
					PredictorPerformance thisDayPerformance = new PredictorPerformance();
					PredictorPerformance thisDayPerformanceImprovedMetric = new PredictorPerformance();
					evalMetricsForDay(byDay.get(day),targetMovement,thisDayPerformance);
					evalImprovedMetricForDay(byDay.get(day),targetMovement,thisDayPerformanceImprovedMetric);
					evalRateOfReturnForDay(byDay.get(day),targetMovement,tracker);
					System.out.println("--------------------------------------------------------------------");
					print(thisDayPerformance);
					System.out.println("Return of investment: " + tracker.rateOfReturn());
					result.putReturnOfInvestment(day,tracker.rateOfReturn());
					result.putMetricPerformance(day,thisDayPerformance);
					result.putImprovedMetricPerformance(day,thisDayPerformanceImprovedMetric);
					System.out.println("--------------------------------------------------------------------");
					trackers.put(companyID,new InvestmentTracker(tracker.getPrice(), tracker.netWorth()));
				} else{
					result.addWarning("Skipped day " + day.format(StandardDateTimeFormatter.getStandardDateFormatter()));
					System.out.println("Skipping Company because there were no predictions this day");
				}
				System.out.println("--------------------------------------------------------------------");
			}
			System.out.println("--------------------------------------------------------------------");
		}
		for(String id : results.keySet()){
			List<EvaluationFiles> company = companies.stream().filter(e -> e.getCompanyID().equals(id)).collect(Collectors.toList());
			assert(company.size()==1);
			results.get(id).serialize(company.get(0).getEvaluationResultFile());
		}
	}

	private Set<LocalDate> getAllDates() {
		return companyMovements.values().stream().flatMap(l -> l.stream().map(p -> p.getFirst())).map(dt -> LocalDate.from(dt)).collect(Collectors.toSet());
	}

	private void evalImprovedMetricForDay(List<Pair<LocalDateTime, Change>> predictions,List<Pair<LocalDateTime, BigDecimal>> targetMovement,PredictorPerformance perf) {
		List<Pair<LocalDateTime,Change>> diff = getDiffPoints(targetMovement);
		int searchStartIndex = 0;
		for(int i = 0;i<diff.size();i++){
			Change curMovement = diff.get(i).getSecond();
			LocalDateTime curMovementTimestamp = diff.get(i).getFirst();
			int predictionIndex = getIndexOfLastBefore(predictions,searchStartIndex,curMovementTimestamp);
			if(predictionIndex>=0){
				Pair<LocalDateTime,Change> predicted = predictions.get(predictionIndex);
				assert(predicted.getFirst().compareTo(curMovementTimestamp)<0);
				if(Math.abs(ChronoUnit.SECONDS.between(predicted.getFirst(), curMovementTimestamp))<=d){
					perf.addTestExample(predicted.getSecond(), curMovement);
					searchStartIndex = predictionIndex+1;
				} else {
					System.out.println("Weird - gap between prediction and movement");
				}
			}
		}
	}

	//only public so we can unit-test
	public int getIndexOfLastBefore(List<Pair<LocalDateTime, Change>> predictions, int searchStartIndex,LocalDateTime time) {
		for(int i=searchStartIndex;i<predictions.size();i++){
			if(predictions.get(i).getFirst().compareTo(time) >=0){
				return i-1;
			}
		}
		return -1;
	}

	//only public so we can unit-test
	public List<Pair<LocalDateTime, Change>> getDiffPoints(List<Pair<LocalDateTime, BigDecimal>> targetMovement) {
		BigDecimal prev = targetMovement.get(0).getSecond();
		List<Pair<LocalDateTime, Change>> diff = new ArrayList<>();
		for(int i=1;i<targetMovement.size();i++){
			Pair<LocalDateTime, BigDecimal> current = targetMovement.get(i);
			if(current.getSecond().compareTo(prev)<0){
				diff.add(new Pair<>(current.getFirst(),Change.DOWN));
			} else if(current.getSecond().compareTo(prev)>0){
				diff.add(new Pair<>(current.getFirst(),Change.UP));
			}
			prev = current.getSecond();
		}
		return diff;
	}

	private BigDecimal getStartingPrice(String id){
		List<Pair<LocalDateTime, BigDecimal>> timeSeries = companyMovements.get(id);
		//assert its actually start:
		return timeSeries.get(0).getSecond();
	}

	private List<Pair<LocalDateTime, BigDecimal>> getTargetPriceMovementForDay(String companyID,LocalDate day) {
		return companyMovements.get(companyID).stream().filter(p -> LocalDate.from(p.getFirst()).equals(day)).sorted((a,b)->a.getFirst().compareTo(b.getFirst())).collect(Collectors.toList());
	}

	private Map<String, Map<LocalDate, List<Pair<LocalDateTime, Change>>>> getOrganizedPredictions(List<EvaluationFiles> companies) throws IOException {
		Map<String, Map<LocalDate, List<Pair<LocalDateTime, Change>>>> organizedPredictions = new HashMap<>();
		for(EvaluationFiles company : companies){
			List<Pair<LocalDateTime, Change>> predictions = deserializePairList(company.getPredictionsFile());
			predictions = predictions.stream().filter(p -> isInTimeBounds(p.getFirst())).collect(Collectors.toList());
			Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictions.stream().collect(Collectors.groupingBy(p -> LocalDate.from(p.getFirst())));
			organizedPredictions.put(company.getCompanyID(), byDay);
		}
		return organizedPredictions;
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

	private void evalMetricsForDay(List<Pair<LocalDateTime, Change>> predictions, List<Pair<LocalDateTime, BigDecimal>> targetMovement, PredictorPerformance perf) {
		for (int i = 0; i < predictions.size(); i++) {
			Pair<LocalDateTime, Change> curPrediction = predictions.get(i);
			Change actualValue = getActualValue(curPrediction.getFirst(),targetMovement);
			perf.addTestExample(curPrediction.getSecond(), actualValue);
		}
	}

	/***
	 * only public so we can unit-test!
	 * @param predictionTime
	 * @param targetMovement
	 * @return
	 */
	public Change getActualValue(LocalDateTime predictionTime, List<Pair<LocalDateTime, BigDecimal>> targetMovement) {
		int i=0;
		Pair<LocalDateTime, BigDecimal> curElem = targetMovement.get(i);
		while(curElem.getFirst().compareTo(predictionTime)<=0 && i+1<targetMovement.size()){
			i++;
			curElem = targetMovement.get(i);
		}
		BigDecimal initial;
		if(curElem.getFirst().compareTo(predictionTime)<=0){
			assert(i+1==targetMovement.size());
			initial = targetMovement.get(i).getSecond();
		} else if(i==0){
			initial = targetMovement.get(i).getSecond(); //hotfix - this should be a rare problem?
		} else{
			initial = targetMovement.get(i-1).getSecond();
		}
		BigDecimal end = targetMovement.get(i).getSecond();
		while(curElem.getFirst().compareTo(predictionTime.plus(d,ChronoUnit.SECONDS))  <= 0 && i+1<targetMovement.size()){
			i++;
			curElem = targetMovement.get(i);
			end = curElem.getSecond();
		}
		if(curElem.getFirst().compareTo(predictionTime.plus(d,ChronoUnit.SECONDS)) <=0){
			assert(i+1==targetMovement.size());
		} else if(i==0){
			end = targetMovement.get(i).getSecond(); //hotfix - this should be a rare problem?
		} else{
			end = targetMovement.get(i-1).getSecond();
		}
		if(end.compareTo(initial) > 0){
			return Change.UP;
		} else if(initial.compareTo(end)>0){
			return Change.DOWN;
		} else{
			return Change.EQUAL;
		}
	}

	/***
	 * only public so we can unit-test
	 * @param pred
	 * @param targetMovement
	 * @param tracker
	 */
	public void evalRateOfReturnForDay(List<Pair<LocalDateTime, Change>> pred,List<Pair<LocalDateTime, BigDecimal>> targetMovement, InvestmentTracker tracker) {
		Collections.sort(pred, (a,b) -> a.getFirst().compareTo(b.getFirst()));
		Collections.sort(targetMovement, (a,b) -> a.getFirst().compareTo(b.getFirst()));
		int predIndex = 0;
		int targetMovementIndex = 0;
		while(true){
			if(predIndex==pred.size() && targetMovementIndex == targetMovement.size()){
				break;
			} else if(predIndex==pred.size()){
				Pair<LocalDateTime, BigDecimal> targetMovementEventPair = targetMovement.get(targetMovementIndex);
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
					Pair<LocalDateTime, BigDecimal> targetMovementEventPair = targetMovement.get(targetMovementIndex);
					processTargetMovement(tracker, targetMovementEventPair);
					targetMovementIndex++;
				}
			}
		}
		tracker.sellIfPossible();
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

	private void processTargetMovement(InvestmentTracker tracker,Pair<LocalDateTime, BigDecimal> targetMovement) {
		tracker.setPrice(targetMovement.getSecond());
	}

	private boolean isInTimeBounds(LocalDateTime first) {
		LocalTime border = LocalTime.of(15, 0);
		return LocalTime.from(first).compareTo(border) > 0;
	}

}
