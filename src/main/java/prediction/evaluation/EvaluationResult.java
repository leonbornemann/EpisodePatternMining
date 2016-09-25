package prediction.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import data.stream.PredictorPerformance;
import episode.finance.EpisodePattern;

public class EvaluationResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<LocalDate,BigDecimal> returnsByDay = new HashMap<>();
	private Map<LocalDate,PredictorPerformance> performanceByDay = new HashMap<>();
	private BigDecimal totalReturn;
	private List<String> warnings = new ArrayList<>();
	
	public void putReturnOfInvestment(LocalDate day, BigDecimal rateOfReturn) {
		returnsByDay.put(day, rateOfReturn);
	}

	public void putMetricPerformance(LocalDate day, PredictorPerformance perf) {
		performanceByDay.put(day, perf);
	}

	public void setTotalReturnOfInvestment(BigDecimal rateOfReturn) {
		totalReturn = rateOfReturn;
	}

	public void serialize(File file) throws FileNotFoundException, IOException {
		ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));
		out.writeObject(this);
		out.close();
	}

	public static EvaluationResult deserialize(File file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream in  = new ObjectInputStream(new FileInputStream(file));
		EvaluationResult result = (EvaluationResult) in.readObject();
		in.close();
		return result;
	}

	public BigDecimal getTotalReturn() {
		return totalReturn;
	}

	public BigDecimal getSummedReturn() {
		return returnsByDay.values().stream().reduce((a,b)->a.add(b)).get();
	}
	
	public PredictorPerformance getTotalPerformance(){
		return new PredictorPerformance(performanceByDay.values());
	}

	public void addWarning(String string) {
		warnings.add(string);
	}

	public Set<LocalDate> getAllDays(){
		assert(performanceByDay.keySet().equals(returnsByDay.keySet()));
		return performanceByDay.keySet();
	}

	public BigDecimal getReturn(LocalDate date) {
		return returnsByDay.get(date);
	}

	public PredictorPerformance getPerformance(LocalDate date) {
		return performanceByDay.get(date);
	}
	

}
