package prediction.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.Change;
import data.stream.PredictorPerformance;
import prediction.mining.Method;
import prediction.util.IOService;

public class CompanyBasedResultSerializer extends ResultSerializer{

	public void toCSV(Set<String> annotatedCompanyCodes, Method method) throws FileNotFoundException, ClassNotFoundException, IOException {
		List<String> orderedCompanyCodes = annotatedCompanyCodes.stream().sorted().collect(Collectors.toList());
		Map<String,EvaluationResult> results = new HashMap<>();
		for (String id : annotatedCompanyCodes) {
			results.put(id,EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method)));
		}
		File csvResultFile = IOService.getTotalResultByCompanyCsvFile(method);
		PrintWriter writer = new PrintWriter(new FileWriter(csvResultFile));
		writer.println("company,return,Precision_UP,Precision_DOWN,PrecisionIgnoreEqual_UP,PrecisionIgnoreEqual_DOWN,Accuracy,AccuracyIngoreEqual,"
				+ "ImprovedPrecision_UP,ImprovedPrecision_DOWN,ImprovedPrecisionIgnoreEqual_UP,ImprovedPrecisionIgnoreEqual_DOWN,ImprovedAccuracy,ImprovedAccuracyIngoreEqual");
		for(int i=0;i<orderedCompanyCodes.size();i++){
			String id = orderedCompanyCodes.get(i);
			if(i==orderedCompanyCodes.size()-1){
				writer.print(id+ ","+buildTotalResultString(results.get(id)));
			} else{
				writer.println(id+","+buildTotalResultString(results.get(id)));
			}
		}
		writer.close();
	}

	private String buildTotalResultString(EvaluationResult evaluationResult) {
		PredictorPerformance total = evaluationResult.getTotalPerformance();
		PredictorPerformance totalImproved = evaluationResult.getTotalImprovedPerformance();
		int roundTo = 5;
		return getAsRoundedString(evaluationResult.getSummedReturn(),roundTo) + "," +
				getAsRoundedString(total.getPrecision(Change.UP), roundTo) + "," +
				getAsRoundedString(total.getPrecision(Change.DOWN), roundTo) + "," +
				getAsRoundedString(total.getEqualIgnoredPrecision(Change.UP), roundTo) + "," +
				getAsRoundedString(total.getEqualIgnoredPrecision(Change.DOWN), roundTo) + "," +
				getAsRoundedString(total.getAccuracy(), roundTo) + "," +
				getAsRoundedString(total.getEqualIgnoredAccuracy(), roundTo) + "," + 
				
				getAsRoundedString(totalImproved.getPrecision(Change.UP), roundTo) + "," +
				getAsRoundedString(totalImproved.getPrecision(Change.DOWN), roundTo) + "," +
				getAsRoundedString(totalImproved.getEqualIgnoredPrecision(Change.UP), roundTo) + "," +
				getAsRoundedString(totalImproved.getEqualIgnoredPrecision(Change.DOWN), roundTo) + "," +
				getAsRoundedString(totalImproved.getAccuracy(), roundTo) + "," +
				getAsRoundedString(totalImproved.getEqualIgnoredAccuracy(), roundTo);
	}
}
