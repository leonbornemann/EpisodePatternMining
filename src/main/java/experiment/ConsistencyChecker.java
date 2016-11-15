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

import data.Change;
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

	public void check() throws IOException, ClassNotFoundException {
		for(File file : Arrays.asList(lowLevelStreamDirDesktop.listFiles()).stream().filter(f -> f.getName().endsWith(".csv")).collect(Collectors.toList())){
			String cmpId = file.getName().split("\\.")[0];
			List<Pair<LocalDateTime, BigDecimal>> timeSeries = IOService.readTimeSeriesData(file);
			List<BigDecimal> diff = getNonZeroDiff(timeSeries);
			List<BigDecimal> positives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
			List<BigDecimal> negatives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)<0).collect(Collectors.toList());
			BigDecimal meanGain = NumericUtil.mean(positives,100);
			BigDecimal meanLoss = NumericUtil.mean(negatives,100);
			EvaluationResult evalResult = EvaluationResult.deserialize(IOService.getEvaluationResultFile(cmpId, Method.PERMS, resultDir));
			double upRecall = evalResult.getTotalImprovedPerformance().getRecall(Change.UP);
			double downRecall = evalResult.getTotalImprovedPerformance().getRecall(Change.DOWN);
			double expectedReturn = getExpectedReturn(upRecall,downRecall,meanGain.doubleValue(),meanLoss.doubleValue(),positives.size(),negatives.size());
			System.out.println("-------------------------" + cmpId + "-----------------------------------");
			System.out.println("Recall up: " + upRecall);
			System.out.println("Recall down:" + downRecall);
			System.out.println("mean Gain: " + meanGain);
			System.out.println("num positive: " + positives.size());
			System.out.println("mean Loss: " + meanLoss);
			System.out.println("num negative: " + negatives.size());
			System.out.println("expected Return: " + expectedReturn);
			System.out.println("Actual Return: " + evalResult.getSummedReturn());
			System.out.println("Smoothed Return: " + evalResult.getSummedSmoothedReturn());
			System.out.println("Buy and Hold Return: " + getBuyAndHoldReturn(timeSeries));
			
		}
	}

	private double getBuyAndHoldReturn(List<Pair<LocalDateTime, BigDecimal>> timeSeries) {
		BigDecimal start = timeSeries.get(0).getSecond();
		BigDecimal end = timeSeries.get(timeSeries.size()-1).getSecond();
		return (end.doubleValue() - start.doubleValue()) / start.doubleValue();
	}

	private double getExpectedReturn(double upRecall,double downRecall,double meanGain,double meanLoss,int numUp,int numDown){
		return upRecall*meanGain*numUp + (1-downRecall)*meanLoss*numDown;
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