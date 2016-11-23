package prediction.evaluation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.Change;
import data.stream.PredictorPerformance;
import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;
import util.NumericUtil;
import util.Pair;

public class NoAggregationEvaluator extends Evaluator{

	private int d;
	private Map<String, TreeMap<LocalDateTime,BigDecimal>> companyMovements;
	private Map<String, TreeMap<LocalDateTime,BigDecimal>> smoothedCompanyMovements;
	
	public NoAggregationEvaluator(int d,List<File> lowLevelStreamDirs, List<File> smoothedStreamDirs) throws IOException{
		this.d = d;
		if(lowLevelStreamDirs!=null){
			companyMovements = initCompanyMovements(lowLevelStreamDirs);
		} 
		if(smoothedStreamDirs!=null){
			smoothedCompanyMovements = initCompanyMovements(smoothedStreamDirs);
		}
	}

	private Map<String, TreeMap<LocalDateTime,BigDecimal>> initCompanyMovements(List<File> dirs) throws IOException {
		List<File> allFiles = dirs.stream().flatMap(dir -> Arrays.stream(dir.listFiles())).collect(Collectors.toList());
		Map<String, TreeMap<LocalDateTime,BigDecimal>> companyMovements = new HashMap<>();
		for(File file : allFiles){
			if(file.isFile() && file.getName().endsWith(".csv")){
				companyMovements.put(file.getName().split("\\.")[0], IOService.readTimeSeries(file));
			}
		}
		return companyMovements;
	}

	public void eval(List<EvaluationFiles> companies,Map<String,Pair<Long,Long>> timeValues) throws IOException {
		Map<String,Map<LocalDate, List<Pair<LocalDateTime, Change>>>> predictionsByCompanyByDay = getOrganizedPredictions(companies);
		Map<String,EvaluationResult> results = new HashMap<>();
		predictionsByCompanyByDay.keySet().stream().forEach(id -> {
			results.put(id, new EvaluationResult(timeValues.get(id).getFirst(),timeValues.get(id).getSecond()));
		});
		List<LocalDate> daysSorted = getAllDates().stream().sorted().collect(Collectors.toList());
		for (LocalDate day : daysSorted) {
			System.out.println("Results for day " + day.format(StandardDateTimeFormatter.getStandardDateFormatter()));
			System.out.println("--------------------------------------------------------------------");
			for(String companyID : predictionsByCompanyByDay.keySet().stream().sorted().collect(Collectors.toList())){
				System.out.println("--------------------------------------------------------------------");
				System.out.println("Results for company " + companyID);
				EvaluationResult result = results.get(companyID);
				Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictionsByCompanyByDay.get(companyID);
				TreeMap<LocalDateTime,BigDecimal> targetMovement = getTargetPriceMovementForDay(companyID,day, companyMovements);
				TreeMap<LocalDateTime,BigDecimal> smoothedTargetMovement = getTargetPriceMovementForDay(companyID,day,smoothedCompanyMovements);
				if(byDay.get(day)!=null && !targetMovement.isEmpty()){
					evalThisNowBetter(byDay.get(day),day,targetMovement,smoothedTargetMovement,result);					
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

	private void evalThisNowBetter(List<Pair<LocalDateTime,Change>> predictions, LocalDate day, TreeMap<LocalDateTime, BigDecimal> timeSeries, TreeMap<LocalDateTime,BigDecimal> smoothedTargetMovement, EvaluationResult evalResult) {
		List<BigDecimal> longResult = new ArrayList<>();
		List<BigDecimal> shortResult = new ArrayList<>();
		List<BigDecimal> smoothedLongResult = new ArrayList<>();
		List<BigDecimal> smoothedShortResult = new ArrayList<>();
		PredictorPerformance result = evalMetrics(predictions, timeSeries, longResult, shortResult);
		evalMetrics(predictions, smoothedTargetMovement, smoothedLongResult, smoothedShortResult);
		BigDecimal totalReturn = NumericUtil.sum(longResult).add(NumericUtil.sum(shortResult));
		BigDecimal relativeReturn = totalReturn.divide(timeSeries.values().iterator().next(), 100, RoundingMode.FLOOR);
		BigDecimal totalSmoothedReturn = NumericUtil.sum(smoothedLongResult).add(NumericUtil.sum(smoothedShortResult));
		BigDecimal relativeSmoothedReturn = totalSmoothedReturn.divide(timeSeries.values().iterator().next(), 100, RoundingMode.FLOOR);
		evalResult.putReturnOfInvestment(day, relativeReturn);
		evalResult.putSmoothedReturnOfInvestment(day, relativeSmoothedReturn);
		evalResult.putAbsoluteReturn(day,totalReturn);
		evalResult.putAbsoluteSmoothedReturn(day,totalSmoothedReturn);
		evalResult.putPredictorPerformance(day, result);
	}

	private PredictorPerformance evalMetrics(List<Pair<LocalDateTime, Change>> predictions,
			TreeMap<LocalDateTime, BigDecimal> timeSeries, List<BigDecimal> longResult, List<BigDecimal> shortResult) {
		PredictorPerformance result = new PredictorPerformance();
		for(Pair<LocalDateTime, Change> pred : predictions){
			LocalDateTime predictionTime = pred.getFirst();
			assert(before2316(predictionTime));
			assert(after16(predictionTime));
			BigDecimal valBefore = timeSeries.floorEntry(predictionTime).getValue();
			LocalDateTime nextTSAfterPrecxition = timeSeries.higherKey(predictionTime);
			if(	before2316(predictionTime) && after16(predictionTime)){
				if(nextTSAfterPrecxition != null && 
						ChronoUnit.SECONDS.between(predictionTime, nextTSAfterPrecxition)<=15){
					BigDecimal valAfter = timeSeries.get(nextTSAfterPrecxition);
					if(valAfter.equals(valBefore)){
						result.addTestExample(pred.getSecond(), Change.EQUAL);
					} else{
						if(pred.getSecond()==Change.UP){
							longResult.add(valAfter.subtract(valBefore));
						} else if(pred.getSecond()==Change.DOWN){
							shortResult.add(valBefore.subtract(valAfter));
						}
						if(valAfter.compareTo(valBefore)>0){
							result.addTestExample(pred.getSecond(), Change.UP);
						} else if(valAfter.compareTo(valBefore)<0){
							result.addTestExample(pred.getSecond(), Change.DOWN);
						}
					}
				} else{
					result.addTestExample(pred.getSecond(), Change.EQUAL);
					//was equal
				}
			}
		}
		return result;
	}
	
	protected boolean before2316(LocalDateTime timestamp) {
		if(timestamp.getHour()<=23){
			return true;
		} else{
			return timestamp.getMinute()<=15;
		}
	}

	protected boolean after16(LocalDateTime timestamp) {
		if(timestamp.getHour() >=16){
			return true;
		} else{
			return false;
		}
	}

	private Set<LocalDate> getAllDates() {
		return companyMovements.values().stream().flatMap(m -> m.keySet().stream()).map(dt -> LocalDate.from(dt)).collect(Collectors.toSet());
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

	private TreeMap<LocalDateTime, BigDecimal> getTargetPriceMovementForDay(String companyID,LocalDate day, Map<String, TreeMap<LocalDateTime, BigDecimal>> timeSeriesDB) {
		List<Entry<LocalDateTime, BigDecimal>> a = timeSeriesDB.get(companyID).entrySet().stream().filter(p -> LocalDate.from(p.getKey()).equals(day)).collect(Collectors.toList());
		TreeMap<LocalDateTime, BigDecimal> thisDay = new TreeMap<LocalDateTime, BigDecimal>();
		a.forEach(e -> thisDay.put(e.getKey(), e.getValue()));
		return thisDay;
	}

	private Map<String, Map<LocalDate, List<Pair<LocalDateTime, Change>>>> getOrganizedPredictions(List<EvaluationFiles> companies) throws IOException {
		Map<String, Map<LocalDate, List<Pair<LocalDateTime, Change>>>> organizedPredictions = new HashMap<>();
		for(EvaluationFiles company : companies){
			List<Pair<LocalDateTime, Change>> predictions = IOService.deserializePairList(company.getPredictionsFile());
			predictions = predictions.stream().filter(p -> isInTimeBounds(p.getFirst())).collect(Collectors.toList());
			Map<LocalDate, List<Pair<LocalDateTime, Change>>> byDay = predictions.stream().collect(Collectors.groupingBy(p -> LocalDate.from(p.getFirst())));
			organizedPredictions.put(company.getCompanyID(), byDay);
		}
		return organizedPredictions;
	}

	private boolean isInTimeBounds(LocalDateTime timestamp) {
		return after16(timestamp) && before2316(timestamp);
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

	

}
