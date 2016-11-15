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
import java.util.Optional;
import java.util.Set;

import data.stream.PredictorPerformance;
import episode.finance.EpisodePattern;

public class EvaluationResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<LocalDate,BigDecimal> returnsByDay = new HashMap<>();
	private Map<LocalDate,BigDecimal> smoothedReturnsByDay = new HashMap<>();
	private Map<LocalDate,PredictorPerformance> performanceByDay = new HashMap<>();
	private Map<LocalDate,PredictorPerformance> improvedPerformanceByDay = new HashMap<>();
	private BigDecimal totalReturn;
	private long trainingTimeNs;
	private long testTimeNs;
	private List<String> warnings = new ArrayList<>();
	
	public EvaluationResult(){}
	
	public EvaluationResult(long trainingTimeNS,long testTimeNS){
		this.trainingTimeNs = trainingTimeNS;
		this.testTimeNs = testTimeNS;
	}
	
	public long getTrainingTimeNs() {
		return trainingTimeNs;
	}

	public void setTrainingTimeNs(long trainingTimeNs) {
		this.trainingTimeNs = trainingTimeNs;
	}

	public long getTestTimeNs() {
		return testTimeNs;
	}

	public void setTestTimeNs(long testTimeNs) {
		this.testTimeNs = testTimeNs;
	}

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
		Optional<BigDecimal> sum = returnsByDay.values().stream().reduce((a,b)->a.add(b));
		if(sum.isPresent()){
			return sum.get();
		} else{
			return BigDecimal.ZERO;
		}
	}
	
	public PredictorPerformance getTotalPerformance(){
		return new PredictorPerformance(performanceByDay.values());
	}
	
	public PredictorPerformance getTotalImprovedPerformance(){
		return new PredictorPerformance(improvedPerformanceByDay.values());
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

	public void putImprovedMetricPerformance(LocalDate day, PredictorPerformance performance) {
		improvedPerformanceByDay.put(day, performance);
	}

	public PredictorPerformance getImprovedPerformance(LocalDate date) {
		return improvedPerformanceByDay.get(date);
	}

	public BigDecimal getSummedSmoothedReturn() {
		Optional<BigDecimal> sum = smoothedReturnsByDay.values().stream().reduce((a,b)->a.add(b));
		if(sum.isPresent()){
			return sum.get();
		} else{
			return BigDecimal.ZERO;
		}
	}

	public BigDecimal getSmoothedReturn(LocalDate date) {
		return smoothedReturnsByDay.get(date);
	}

	public void putSmoothedReturnOfInvestment(LocalDate day, BigDecimal rateOfReturn) {
		smoothedReturnsByDay.put(day, rateOfReturn);
	}
	

}
