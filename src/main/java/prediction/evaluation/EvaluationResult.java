package prediction.evaluation;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import episode.finance.EpisodePattern;
import prediction.data.stream.PredictorPerformance;

public class EvaluationResult implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<LocalDate,Double> returnsByDay = new HashMap<>();
	private Map<LocalDate,PredictorPerformance> performanceByDay = new HashMap<>();
	private double totalReturn;
	
	public void putReturnOfInvestment(LocalDate day, double rateOfReturn) {
		returnsByDay.put(day, rateOfReturn);
	}

	public void putMetricPerformance(LocalDate day, PredictorPerformance perf) {
		performanceByDay.put(day, perf);
	}

	public void setTotalReturnOfInvestment(double rateOfReturn) {
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
	
	
	public static Map<EpisodePattern, Double> loadEpisodeMap(File file) throws FileNotFoundException, IOException, ClassNotFoundException {
		ObjectInputStream in  = new ObjectInputStream(new FileInputStream(file));
		@SuppressWarnings("unchecked")
		Map<EpisodePattern, Double> episodeMap = (Map<EpisodePattern, Double>) in.readObject();
		in.close();
		return episodeMap;
	}

	public double getTotalReturn() {
		return totalReturn;
	}

	public double getSummedReturn() {
		return returnsByDay.values().stream().mapToDouble(d -> d.doubleValue()).sum();
	}
	
	public PredictorPerformance getTotalPerformance(){
		return new PredictorPerformance(performanceByDay.values());
	}
	

}
