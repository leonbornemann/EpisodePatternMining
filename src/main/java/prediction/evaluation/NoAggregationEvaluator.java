package prediction.evaluation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import data.Change;
import data.LowLevelEvent;
import data.stream.PredictorPerformance;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class NoAggregationEvaluator extends Evaluator{

	private File lowLevelStreamDir;
	private int d;
	
	public NoAggregationEvaluator(int d,File lowLevelStreamDir){
		this.d = d;
		this.lowLevelStreamDir = lowLevelStreamDir;
	}

	public void eval(List<EvaluationFiles> companies) throws IOException {
		Map<LocalDate,File> filenames = initLowLevelDatabase();
		Map<String,Map<LocalDate, List<Pair<LocalDateTime, Change>>>> predictionsByCompanyByDay = getOrganizedPredictions(companies);
		Map<String,EvaluationResult> results = new HashMap<>();
		Map<String,InvestmentTracker> trackers = new HashMap<>();
		predictionsByCompanyByDay.keySet().stream().forEach(id -> {
			results.put(id, new EvaluationResult());
			trackers.put(id, new InvestmentTracker(getStartingPrice(id)));
		});
		for (LocalDate day : filenames.keySet().stream().sorted().collect(Collectors.toList())) {
			System.out.println("Results for day " + day.format(StandardDateTimeFormatter.getStandardDateFormatter()));
			System.out.println("--------------------------------------------------------------------");
			List<LowLevelEvent> lowLevelEvents = getLowLevelEvents(filenames.get(day));
			for(String companyID : predictionsByCompanyByDay.keySet().stream().sorted().collect(Collectors.toList())){
				System.out.println("--------------------------------------------------------------------");
				System.out.println("Results for company " + companyID);
				EvaluationResult result = results.get(companyID);
				InvestmentTracker tracker = trackers.get(companyID);
				Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictionsByCompanyByDay.get(companyID);
				List<Pair<LocalDateTime, BigDecimal>> targetMovement = getTargetPriceMovement(lowLevelEvents,companyID);
				if(byDay.get(day)!=null && !targetMovement.isEmpty()){
					PredictorPerformance thisDayPerformance = new PredictorPerformance();
					evalMetricsForDay(byDay.get(day),targetMovement,thisDayPerformance);
					evalRateOfReturnForDay(byDay.get(day),targetMovement,tracker);
					//TODO: save return of investment for this day
					System.out.println("--------------------------------------------------------------------");
					print(thisDayPerformance);
					System.out.println("Return of investment: " + tracker.rateOfReturn());
					result.putReturnOfInvestment(day,tracker.rateOfReturn());
					result.putMetricPerformance(day,thisDayPerformance);
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

	private BigDecimal getStartingPrice(String id){
		try{
			 File earliestFile = Arrays.asList(lowLevelStreamDir.listFiles()).stream().
					 sorted((f1,f2) -> getLocalDateFromFile(f1).compareTo(getLocalDateFromFile(f2)))
					 .iterator().next();
			return LowLevelEvent.getStartingPrice(earliestFile,getLowLevelStyleId(id));
		} catch(Exception e){
			throw new AssertionError(e);
		}
	}

	private String getLowLevelStyleId(String id) {
		return "\""+id + "\"";
	}

	private List<LowLevelEvent> getLowLevelEvents(File file) throws IOException {
		return LowLevelEvent.readAll(file).stream().
			filter(e -> isInTimeBounds(e.getTimestamp())).
			collect(Collectors.toList());
	}

	private List<Pair<LocalDateTime, BigDecimal>> getTargetPriceMovement(List<LowLevelEvent> lowLevelEvents,
			String companyID) {
		return lowLevelEvents.stream().
				filter(e -> e.getCompanyId().equals(getLowLevelStyleId(companyID))).
				sorted(LowLevelEvent::temporalOrder).
				map(e -> new Pair<>(e.getTimestamp(),e.getValue())).
				collect(Collectors.toList());
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

	private Map<LocalDate, File> initLowLevelDatabase() {
		List<File> files = Arrays.asList(lowLevelStreamDir.listFiles()).stream().filter(f -> f.exists()).collect(Collectors.toList());
		return files.stream().collect(Collectors.toMap(f -> getLocalDateFromFile(f), f->f));
	}

	private LocalDate getLocalDateFromFile(File f) {
		String[] a = f.getName().split("_");
		assert(a.length==2 && a[0].equals("NASDAQ"));
		String[] b = a[1].split("\\.");
		assert(b.length==2 && b[1].equals("csv"));
		return LocalDate.parse(b[0], StandardDateTimeFormatter.getStandardDateFormatter());
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
