package prediction.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.Change;
import prediction.mining.Method;
import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;

public class DayBasedResultSerializer extends ResultSerializer{

	public void toCSV(Set<String> annotatedCompanyCodes, Method method) throws FileNotFoundException, ClassNotFoundException, IOException {
		Map<String,EvaluationResult> results = new HashMap<>();
		for (String id : annotatedCompanyCodes) {
			results.put(id,EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method)));
		}
		for(String id: annotatedCompanyCodes){
			File csvResultFile = IOService.getCSVResultFile(id,method);
			PrintWriter writer = new PrintWriter(new FileWriter(csvResultFile));
			List<LocalDate> orderedDates = results.get(id).getAllDays().stream().sorted().collect(Collectors.toList());
			writer.println(buildHeadLine());
			for(int i=0;i<orderedDates.size();i++){
				LocalDate date = orderedDates.get(i);
				if(i==orderedDates.size()-1){
					writer.print(buildResultString(date,results.get(id)));
				} else{
					writer.println(buildResultString(date,results.get(id)));
				}
			}
			writer.close();
		}
		List<LocalDate> allDatesOrdered = results.values().stream().flatMap(r -> r.getAllDays().stream()).sorted().distinct().collect(Collectors.toList());
		File target = IOService.getTotalResultByDayCsvFile(method);
		PrintWriter writer = new PrintWriter(new FileWriter(target));
		writer.println("date,avgReturn,avgPrecision_UP,avgPrecision_DOWN,avgPrecisionIgnoreEqual_UP,avgPrecisionIgnoreEqual_DOWN,Accuracy,AccuracyIngoreEqual,"
				+ "avgImprovedPrecision_UP,avgImprovedPrecision_DOWN,avgImprovedPrecisionIgnoreEqual_UP,avgImprovedPrecisionIgnoreEqual_DOWN,ImprovedAccuracy,ImprovedAccuracyIngoreEqual");
		for(int i=0;i<allDatesOrdered.size();i++){
			LocalDate date = allDatesOrdered.get(i);
			String dateString = date.format(StandardDateTimeFormatter.getStandardDateFormatter());
			if(i==allDatesOrdered.size()-1){
				writer.print(dateString + "," + getAvgValuesForDayAsString(date,results));
			} else{
				writer.println(dateString + "," + getAvgValuesForDayAsString(date,results));
			}
		}
		writer.close();
	}
	
	private String getAvgAsRoundedStringForDoubleList(List<Double> upPrecisions) {
		if(upPrecisions.isEmpty()){
			return "NA";
		}
		Double avg = upPrecisions.stream().mapToDouble(d -> d.doubleValue()).average().getAsDouble();
		return getAsRoundedString(avg,5);
	}

	private String getAvgAsRoundedString(List<BigDecimal> returns) {
		long numReturns = returns.size();
		BigDecimal sum = returns.stream().reduce((a,b) -> a.add(b)).get();
		BigDecimal avg = sum.divide(new BigDecimal(numReturns),100,RoundingMode.FLOOR);
		return getAsRoundedString(avg, 5);
	}
	
	private String getAvgValuesForDayAsString(LocalDate date, Map<String, EvaluationResult> results) {
		List<EvaluationResult> validResults = results.values().stream().
			filter(r -> r.getAllDays().contains(date)).collect(Collectors.toList());
		List<BigDecimal> returns = validResults.stream().
			map(r -> r.getReturn(date)).collect(Collectors.toList());
		String avgReturnString = getAvgAsRoundedString(returns);
		List<Double> upPrecisions = validResults.stream().map(r -> r.getPerformance(date).getPrecision(Change.UP)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgUpPrecisionString = getAvgAsRoundedStringForDoubleList(upPrecisions);
		List<Double> downPrecisions = validResults.stream().map(r -> r.getPerformance(date).getPrecision(Change.DOWN)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgDownPrecisionString = getAvgAsRoundedStringForDoubleList(downPrecisions);
		List<Double> upPrecisionsIgnoreEqual = validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredPrecision(Change.UP)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgUpPrecisionsIgnoreEqualString = getAvgAsRoundedStringForDoubleList(upPrecisionsIgnoreEqual);
		List<Double> downPrecisionsIgnoreEqual = validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredPrecision(Change.DOWN)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgDownPrecisionsIgnoreEqualString = getAvgAsRoundedStringForDoubleList(downPrecisionsIgnoreEqual);
		String avgAccuracyString = getAvgAsRoundedStringForDoubleList(validResults.stream().map(r -> r.getPerformance(date).getAccuracy()).
				filter(d -> !d.isNaN()).collect(Collectors.toList()));
		String avgAccuracyIgnoreEqualString = getAvgAsRoundedStringForDoubleList(validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredAccuracy()).
				filter(d -> !d.isNaN()).collect(Collectors.toList()));
		
		List<Double> upPrecisionsImproved = validResults.stream().map(r -> r.getImprovedPerformance(date).getPrecision(Change.UP)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgUpPrecisionImprovedString = getAvgAsRoundedStringForDoubleList(upPrecisionsImproved);
		List<Double> downPrecisionsImproved = validResults.stream().map(r -> r.getImprovedPerformance(date).getPrecision(Change.DOWN)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgDownPrecisionImprovedString = getAvgAsRoundedStringForDoubleList(downPrecisionsImproved);
		List<Double> upPrecisionsIgnoreEqualImproved = validResults.stream().map(r -> r.getImprovedPerformance(date).getEqualIgnoredPrecision(Change.UP)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgUpPrecisionsIgnoreEqualImprovedString = getAvgAsRoundedStringForDoubleList(upPrecisionsIgnoreEqualImproved);
		List<Double> downPrecisionsIgnoreEqualImproved = validResults.stream().map(r -> r.getImprovedPerformance(date).getEqualIgnoredPrecision(Change.DOWN)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgDownPrecisionsIgnoreEqualImprovedString = getAvgAsRoundedStringForDoubleList(downPrecisionsIgnoreEqualImproved);
		String avgAccuracyImprovedString = getAvgAsRoundedStringForDoubleList(validResults.stream().map(r -> r.getImprovedPerformance(date).getAccuracy()).
				filter(d -> !d.isNaN()).collect(Collectors.toList()));
		String avgAccuracyIgnoreEqualImprovedString = getAvgAsRoundedStringForDoubleList(validResults.stream().map(r -> r.getImprovedPerformance(date).getEqualIgnoredAccuracy()).
				filter(d -> !d.isNaN()).collect(Collectors.toList()));
		
		return avgReturnString + "," + 
			avgUpPrecisionString + "," + 
			avgDownPrecisionString + "," + 
			avgUpPrecisionsIgnoreEqualString + "," + 
			avgDownPrecisionsIgnoreEqualString + "," + 
			avgAccuracyString + "," + 
			avgAccuracyIgnoreEqualString + "," + 
			avgUpPrecisionImprovedString + "," + 
			avgDownPrecisionImprovedString + "," + 
			avgUpPrecisionsIgnoreEqualImprovedString + "," + 
			avgDownPrecisionsIgnoreEqualImprovedString + "," + 
			avgAccuracyImprovedString + "," + 
			avgAccuracyIgnoreEqualImprovedString;
	}
	
	private String buildResultString(LocalDate date,EvaluationResult evaluationResult) {
		int roundTo = 5;
		String dateString = date.format(StandardDateTimeFormatter.getStandardDateFormatter());
		assert(evaluationResult.getAllDays().contains(date));
		BigDecimal a = evaluationResult.getReturn(date);
		return dateString+ "," +
				getAsRoundedString(evaluationResult.getReturn(date),roundTo) + "," +
				getAsRoundedString(evaluationResult.getPerformance(date).getPrecision(Change.UP),roundTo) + "," +
				getAsRoundedString(evaluationResult.getPerformance(date).getPrecision(Change.DOWN),roundTo) + "," + 
				getAsRoundedString(evaluationResult.getPerformance(date).getRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getRecall(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredPrecision(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredPrecision(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredRecall(Change.DOWN),roundTo) + "," +
				
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getPrecision(Change.UP),roundTo) + "," +
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getPrecision(Change.DOWN),roundTo) + "," + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getRecall(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getEqualIgnoredPrecision(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getEqualIgnoredPrecision(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getEqualIgnoredRecall(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getImprovedPerformance(date).getEqualIgnoredRecall(Change.DOWN),roundTo);
	}

	private String buildHeadLine() {
		String result = "date,"+
				"returnOfInvestment" + "," +
				"precision_" + Change.UP + "," +
				"precision_" + Change.DOWN + "," +
				"recall_" + Change.UP + "," +
				"recall_" + Change.DOWN + "," +
				"precisionIgnoreEqual_" + Change.UP + "," +
				"precisionIgnoreEqual_" + Change.DOWN + "," +
				"recallIgnoreEqual_" + Change.UP + "," +
				"recallIgnoreEqual_" + Change.DOWN + "," +
				"Accuracy" + "," +
				"AccuracyIgnoreEqual" + "," +
				
				"improvedPrecision_" + Change.UP + "," +
				"improvedPrecision_" + Change.DOWN + "," +
				"improvedRecall_" + Change.UP + "," +
				"improvedRecall_" + Change.DOWN + "," +
				"improvedPrecisionIgnoreEqual_" + Change.UP + "," +
				"improvedPrecisionIgnoreEqual_" + Change.DOWN + "," +
				"improvedRecallIgnoreEqual_" + Change.UP + "," +
				"improvedRecallIgnoreEqual_" + Change.DOWN + "," +
				"improvedAccuracy" + "," +
				"improvedAccuracyIgnoerEqual";
		System.out.println(result);
		return result;
	}

}
