package prediction.evaluation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

	public void toCSV(Set<String> annotatedCompanyCodes, Method method, File resultDir) throws FileNotFoundException, ClassNotFoundException, IOException {
		List<String> orderedCompanyCodes = annotatedCompanyCodes.stream().sorted().collect(Collectors.toList());
		Map<String,EvaluationResult> results = new HashMap<>();
		for (String id : annotatedCompanyCodes) {
			results.put(id,EvaluationResult.deserialize(IOService.getEvaluationResultFile(id,method,resultDir)));
		}
		File csvResultFile = IOService.getTotalResultByCompanyCsvFile(method,resultDir);
		PrintWriter writer = new PrintWriter(new FileWriter(csvResultFile));
		writer.println("company,"
				+ "return,smoothedReturn,absoluteReturn,absoluteSmoothedReturn,"
				+ "PrecisionIgnoreEqual_UP,PrecisionIgnoreEqual_DOWN,AccuracyIngoreEqual,"
				+ "Recall_UP,Recall_DOWN,"
				+"trainingTimeNs,testTimeNS");
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
		int roundTo = 5;
		return getAsRoundedString(evaluationResult.getSummedReturn(),roundTo) + "," +
				getAsRoundedString(evaluationResult.getSummedSmoothedReturn(),roundTo) + "," +
				getAsRoundedString(evaluationResult.getSummedAbsoluteReturn(),roundTo) + "," +
				getAsRoundedString(evaluationResult.getSummedAbsoluteSmoothedReturn(),roundTo) + "," +
				
				getAsRoundedString(total.getEqualIgnoredPrecision(Change.UP), roundTo) + "," +
				getAsRoundedString(total.getEqualIgnoredPrecision(Change.DOWN), roundTo) + "," +
				getAsRoundedString(total.getEqualIgnoredAccuracy(), roundTo) + "," + 
				getAsRoundedString(total.getEqualIgnoredRecall(Change.UP), roundTo) + "," + 
				getAsRoundedString(total.getEqualIgnoredRecall(Change.DOWN), roundTo) + "," + 
				
				evaluationResult.getTrainingTimeNs() + "," +
				evaluationResult.getTestTimeNs();
	}
}
