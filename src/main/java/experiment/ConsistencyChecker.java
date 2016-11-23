package experiment;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import data.stream.PredictorPerformance;
import prediction.evaluation.EvaluationResult;
import prediction.mining.Method;
import prediction.util.IOService;
import util.NumericUtil;
import util.Pair;

public class ConsistencyChecker {

	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	//private static File lowLevelSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\");
	
	private File resultDir;

	public ConsistencyChecker(File resultDir) {
		this.resultDir = resultDir;
	}

	public void check(Method method) throws IOException, ClassNotFoundException {
		for(File file : Arrays.asList(lowLevelStreamDirDesktop.listFiles()).stream().filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList())){
			String cmpId = file.getName().split("\\.")[0];
			List<Pair<LocalDateTime, BigDecimal>> timeSeries = IOService.readTimeSeriesData(file);
			List<BigDecimal> diff = getNonZeroDiff(timeSeries);
			List<BigDecimal> positives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
			List<BigDecimal> negatives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)<0).collect(Collectors.toList());
			BigDecimal meanGain = NumericUtil.mean(positives,100);
			BigDecimal meanLoss = NumericUtil.mean(negatives,100);
			EvaluationResult evalResult = EvaluationResult.deserialize(IOService.getEvaluationResultFile(cmpId, method, resultDir));
			double expectedReturn = getExpectedBalance(evalResult.getTotalPerformance(),meanGain.doubleValue(),meanLoss.doubleValue(),positives.size(),negatives.size());
			System.out.println(cmpId + "\t"+Math.abs(expectedReturn-evalResult.getSummedReturn().doubleValue()));
//			System.out.println("-------------------------" + cmpId + "-----------------------------------");
//			System.out.println("mean Gain: " + meanGain);
//			System.out.println("num positive: " + positives.size());
//			System.out.println("mean Loss: " + meanLoss);
//			System.out.println("num negative: " + negatives.size());
			System.out.println("expected Return: " + expectedReturn);
//			System.out.println("test examples: "+ evalResult.getTotalImprovedPerformance().getNumClassifiedExamples() );
			System.out.println("Actual Balance: " + evalResult.getSummedReturn());
//			System.out.println("Smoothed Balance: " + evalResult.getSummedSmoothedReturn());
//			System.out.println("Buy and Hold Return: " + getBuyAndHoldReturn(timeSeries));
			
		}
	}

	private double getBuyAndHoldReturn(List<Pair<LocalDateTime, BigDecimal>> timeSeries) {
		BigDecimal start = timeSeries.get(0).getSecond();
		BigDecimal end = timeSeries.get(timeSeries.size()-1).getSecond();
		return (end.doubleValue() - start.doubleValue()) / start.doubleValue();
	}

	private double getExpectedBalance(PredictorPerformance perf,double meanGain,double meanLoss,int numUp,int numDown){
		return perf.getUp_UP()*meanGain + perf.getUp_DOWN()*meanLoss - perf.getDOWN_DOWN()*meanLoss - perf.getDOWN_UP()*meanGain;
		//return perf.getRecall(Change.UP)*meanGain*numUp + perf.getFalsePositiveRate(Change.UP)*meanLoss*numDown - perf.getRecall(Change.DOWN)*meanLoss*numDown - perf.getFalsePositiveRate(Change.DOWN)*meanGain*numDown;
	}
	

	private List<BigDecimal> getNonZeroDiff(List<Pair<LocalDateTime, BigDecimal>> timeSeries) {
		List<BigDecimal> diff = new ArrayList<>();
		for(int i=1;i<timeSeries.size();i++){
			LocalDate prevDay = LocalDate.from(timeSeries.get(i-1).getFirst());
			BigDecimal prevVal = timeSeries.get(i-1).getSecond();
			LocalDate curDay = LocalDate.from(timeSeries.get(i).getFirst());
			BigDecimal curVal = timeSeries.get(i).getSecond();
			if(curDay.equals(prevDay) && !curVal.equals(prevVal)){
				diff.add(curVal.subtract(prevVal));
			}
		}
		return diff;
	}

}
