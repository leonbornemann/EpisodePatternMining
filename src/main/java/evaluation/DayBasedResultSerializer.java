package evaluation;

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

import data.events.Change;
import prediction.models.Method;
import util.IOService;
import util.StandardDateTimeFormatter;

/***
 * Serializer class for prediction results
 * @author Leon Bornemann
 *
 */
public class DayBasedResultSerializer extends ResultSerializer{

	public void toCSV(Set<String> annotatedCompanyCodes, Method method, File resultDir) throws FileNotFoundException, ClassNotFoundException, IOException {
		Map<String,EvaluationResult> results = new HashMap<>();
		for (String id : annotatedCompanyCodes) {
			results.put(id,EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method,resultDir)));
		}
		for(String id: annotatedCompanyCodes){
			File csvResultFile = IOService.getCSVResultFile(id,method,resultDir);
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
		File target = IOService.getTotalResultByDayCsvFile(method,resultDir);
		PrintWriter writer = new PrintWriter(new FileWriter(target));
		writer.println("date,avgReturn,avgAbsoluteReturn,"
				+ "avgPrecisionIgnoreEqual_UP,avgPrecisionIgnoreEqual_DOWN,avgAccuracyIngoreEqual,");
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
		List<BigDecimal> absoluteReturns = validResults.stream().
				map(r -> r.getAbsoluteReturn(date)).collect(Collectors.toList());
		String avgAbsoluteReturnString = getAvgAsRoundedString(absoluteReturns);
		List<Double> upPrecisionsIgnoreEqual = validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredPrecision(Change.UP)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgUpPrecisionsIgnoreEqualString = getAvgAsRoundedStringForDoubleList(upPrecisionsIgnoreEqual);
		List<Double> downPrecisionsIgnoreEqual = validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredPrecision(Change.DOWN)).
				filter(d -> !d.isNaN()).collect(Collectors.toList());
		String avgDownPrecisionsIgnoreEqualString = getAvgAsRoundedStringForDoubleList(downPrecisionsIgnoreEqual);
		String avgAccuracyIgnoreEqualString = getAvgAsRoundedStringForDoubleList(validResults.stream().map(r -> r.getPerformance(date).getEqualIgnoredAccuracy()).
				filter(d -> !d.isNaN()).collect(Collectors.toList()));
		
		return avgReturnString + "," + 
			avgAbsoluteReturnString + "," + 			
			avgUpPrecisionsIgnoreEqualString + "," + 
			avgDownPrecisionsIgnoreEqualString + "," + 
			avgAccuracyIgnoreEqualString;
	}
	
	private String buildResultString(LocalDate date,EvaluationResult evaluationResult) {
		int roundTo = 5;
		String dateString = date.format(StandardDateTimeFormatter.getStandardDateFormatter());
		assert(evaluationResult.getAllDays().contains(date));
		return dateString+ "," +
				getAsRoundedString(evaluationResult.getReturn(date),roundTo) + "," +
				getAsRoundedString(evaluationResult.getAbsoluteReturn(date),roundTo) + "," +
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredPrecision(Change.UP),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredPrecision(Change.DOWN),roundTo) + ","  + 
				getAsRoundedString(evaluationResult.getPerformance(date).getEqualIgnoredAccuracy(),roundTo);
	}

	private String buildHeadLine() {
		String result = "date,"+
				"Return" + "," +
				"AbsoluteReturn" + "," +
				"PrecisionIgnoreEqual_UP" + "," +
				"PrecisionIgnoreEqual_DOWN" + "," +
				"AccuracyIngoreEqual";
				
			
		System.out.println(result);
		return result;
	}

}
