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
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import data.stream.PredictorPerformance;

public class EvaluationResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<LocalDate,BigDecimal> relativeReturnsByDay = new HashMap<>();
	private Map<LocalDate,BigDecimal> relativeSmoothedReturnsByDay = new HashMap<>();
	private Map<LocalDate,BigDecimal> absoluteReturnsByDay = new HashMap<>();
	private Map<LocalDate,BigDecimal> absoluteSmoothedReturnsByDay = new HashMap<>();
	private Map<LocalDate,PredictorPerformance> performanceByDay = new HashMap<>();
	private long trainingTimeNs;
	private long testTimeNs;
	
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
		relativeReturnsByDay.put(day, rateOfReturn);
	}

	public void putPredictorPerformance(LocalDate day, PredictorPerformance perf) {
		performanceByDay.put(day, perf);
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

	public BigDecimal getSummedReturn() {
		return getSum(relativeReturnsByDay);
	}

	private BigDecimal getSum(Map<LocalDate, BigDecimal> map) {
		Optional<BigDecimal> sum = map.values().stream().reduce((a,b)->a.add(b));
		if(sum.isPresent()){
			return sum.get();
		} else{
			return BigDecimal.ZERO;
		}
	}
	
	public PredictorPerformance getTotalPerformance(){
		return new PredictorPerformance(performanceByDay.values());
	}
	

	public Set<LocalDate> getAllDays(){
		assert(performanceByDay.keySet().equals(relativeReturnsByDay.keySet()));
		return performanceByDay.keySet();
	}

	public BigDecimal getReturn(LocalDate date) {
		return relativeReturnsByDay.get(date);
	}

	public PredictorPerformance getPerformance(LocalDate date) {
		return performanceByDay.get(date);
	}

	public BigDecimal getSummedSmoothedReturn() {
		return getSum(relativeSmoothedReturnsByDay);
	}

	public BigDecimal getSmoothedReturn(LocalDate date) {
		return relativeSmoothedReturnsByDay.get(date);
	}

	public void putSmoothedReturnOfInvestment(LocalDate day, BigDecimal rateOfReturn) {
		relativeSmoothedReturnsByDay.put(day, rateOfReturn);
	}

	public void putAbsoluteReturn(LocalDate day, BigDecimal totalReturn) {
		absoluteReturnsByDay.put(day, totalReturn);		
	}

	public void putAbsoluteSmoothedReturn(LocalDate day, BigDecimal totalSmoothedReturn) {
		absoluteSmoothedReturnsByDay.put(day, totalSmoothedReturn);
		
	}

	public BigDecimal getSummedAbsoluteReturn() {
		return getSum(absoluteReturnsByDay);
	}

	public BigDecimal getSummedAbsoluteSmoothedReturn() {
		return getSum(absoluteSmoothedReturnsByDay);
	}

	public BigDecimal getAbsoluteReturn(LocalDate date) {
		return absoluteReturnsByDay.get(date);
	}

	public BigDecimal getAbsoluteSmoothedReturn(LocalDate date) {
		return absoluteSmoothedReturnsByDay.get(date);
	}
	

}
