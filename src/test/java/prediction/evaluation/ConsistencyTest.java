package prediction.evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.junit.Test;

import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.CategoricalEventStream;
import data.stream.StreamWindow;
import data.stream.StreamWindowSlider;
import evaluation.ConfusionMatrix;
import experiment.EvaluationConfig;
import prediction.models.Method;
import prediction.models.PredictiveModel;
import semantics.SemanticKnowledgeCollector;
import util.IOService;
import util.NumericUtil;
import util.Pair;

public class ConsistencyTest {

	private static File annotatedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series\\");
	private static File annotatedSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series\\");
	
	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	private static File lowLevelSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\");
	
	private static File lowLevelSmoothedStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series Smoothed\\");
	private static File lowLevelSmoothedSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series Smoothed\\");
	
	private static EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.75, 0.75, 20);
	private static File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Toy stuff\\Test Run With New Stuff");
	
	private static PrintWriter writer;
	private static File targetFile = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Toy stuff\\toEval.csv");
	
	@Test
	public void test() throws ClassNotFoundException, IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		Random random = new Random(13);
		Set<CategoricalEventType> eventAlphabet = CategoricalEventType.loadEventAlphabet(annotatedCompanyCodes);
		List<CategoricalEventType> toDo = eventAlphabet.stream().filter(e -> e.getChange()==Change.UP).collect(Collectors.toList());
		List<Double> results = new ArrayList<>();
		writer = new PrintWriter(new FileWriter(targetFile));
		writer.println("company,diff,expectedReturn,realReturn");
		for(CategoricalEventType event : toDo){
			String cmpId = event.getCompanyID();
			System.out.println("doing "+cmpId);
			List<File> streamDirs = new ArrayList<>();
			streamDirs.add(annotatedStreamDirDesktop);
			if(config.isUseSemantics()){
				streamDirs.add(annotatedSectorStreamDirDesktop);
			}
//			InMemoryMultiTimeSeriesAnnotatedEventStream stream = new InMemoryMultiTimeSeriesAnnotatedEventStream(streamDirs);
//			RandomGuessingModel model = new RandomGuessingModel(random);
//			applyPredictor(event,stream,model,Method.RandomGuessing);
			TestResult result = evalPredictor(cmpId);
			TreeMap<LocalDateTime, BigDecimal> timeSeries = IOService.readTimeSeries(cmpId, lowLevelStreamDirDesktop);
			List<BigDecimal> diff = getNonZeroRelativeDiff(timeSeries);
			List<BigDecimal> positives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)>0).collect(Collectors.toList());
			List<BigDecimal> negatives = diff.stream().filter(d -> d.compareTo(BigDecimal.ZERO)<0).collect(Collectors.toList());
			BigDecimal meanGain = NumericUtil.mean(positives,100);
			BigDecimal meanLoss = NumericUtil.mean(negatives,100);
			double expectedBalance = getExpectedBalance(result.getResult(),meanGain.doubleValue(),meanLoss.doubleValue(),positives.size(),negatives.size());
			double actualBalance = NumericUtil.sum(result.getLongResult()).doubleValue() + NumericUtil.sum(result.getShortResult()).doubleValue();
			//System.out.println("expected : \t\t" + expectedBalance);
			BigDecimal startPrice = timeSeries.values().iterator().next();
			BigDecimal endValue = timeSeries.lastEntry().getValue();
			double buyAndHoldBalance = endValue.doubleValue() - startPrice.doubleValue();
			System.out.println("actual: \t\t" + actualBalance / startPrice.doubleValue());
			System.out.println("buy and hold: \t \t " + buyAndHoldBalance / startPrice.doubleValue());
			System.out.println("difference: \t\t" + (expectedBalance - actualBalance) /startPrice.doubleValue());
			results.add(actualBalance / startPrice.doubleValue());
			printList(cmpId,positives,expectedBalance / startPrice.doubleValue(),actualBalance / startPrice.doubleValue());
			printList(cmpId,negatives,expectedBalance / startPrice.doubleValue(),actualBalance / startPrice.doubleValue());
		}
		writer.close();
		System.out.println("avg return: " +results.stream().mapToDouble(d -> d.doubleValue()).average());
		results.forEach(d -> System.out.println(d));
	}
	
	private void printList(String cmpId, List<BigDecimal> negatives, double expectedReturn, double actualReturn) {
		for(BigDecimal d: negatives){
			writer.println(cmpId + "," + d.doubleValue() + "," + expectedReturn + "," + actualReturn);
		}
	}

	private double getExpectedBalance(ConfusionMatrix perf,double meanGain,double meanLoss,int numUp,int numDown){
		return perf.getUp_UP()*meanGain + perf.getUp_DOWN()*meanLoss - perf.getDOWN_DOWN()*meanLoss - perf.getDOWN_UP()*meanGain;
		//return perf.getRecall(Change.UP)*meanGain*numUp + perf.getFalsePositiveRate(Change.UP)*meanLoss*numDown - perf.getRecall(Change.DOWN)*meanLoss*numDown - perf.getFalsePositiveRate(Change.DOWN)*meanGain*numDown;
	}

	private List<BigDecimal> getNonZeroRelativeDiff(TreeMap<LocalDateTime,BigDecimal> timeSeries) {
		List<BigDecimal> diff = new ArrayList<>();
		Iterator<LocalDateTime> datetimes = timeSeries.keySet().iterator();
		LocalDateTime prev = datetimes.next();
		BigDecimal startPrice = timeSeries.get(prev);
		while(datetimes.hasNext()){
			LocalDateTime cur = datetimes.next();
			LocalDate prevDay = LocalDate.from(prev);
			BigDecimal prevVal = timeSeries.get(prev);
			LocalDate curDay = LocalDate.from(cur);
			BigDecimal curVal = timeSeries.get(cur);
			if(curDay.equals(prevDay) && !curVal.equals(prevVal)){
				diff.add(curVal.subtract(prevVal).divide(startPrice, 100, RoundingMode.FLOOR));
			}
			prev = cur;
		}
		return diff;
	}

	private TestResult evalPredictor(String cmpId) throws IOException {
		File predictionsFile = IOService.buildPredictionsTargetFile(cmpId, Method.RandomGuessing, resultDir);
		List<Pair<LocalDateTime, Change>> predictions = IOService.deserializePairList(predictionsFile);
		TreeMap<LocalDateTime, BigDecimal> timeSeries = IOService.readTimeSeries(cmpId,lowLevelStreamDirDesktop);
		ConfusionMatrix result = new ConfusionMatrix();
		List<BigDecimal> longResult = new ArrayList<>();
		List<BigDecimal> shortResult = new ArrayList<>();
		for(Pair<LocalDateTime, Change> pred : predictions){
			LocalDateTime predictionTime = pred.getFirst();
			assert(before2316(predictionTime));
			assert(after16(predictionTime));
			BigDecimal valBefore = timeSeries.floorEntry(predictionTime).getValue();
			LocalDateTime nextTSAfterPrecxition = timeSeries.higherKey(predictionTime);
			if(	before2316(predictionTime) &&after16(predictionTime)){
				if(nextTSAfterPrecxition != null && 
						ChronoUnit.SECONDS.between(predictionTime, nextTSAfterPrecxition)<=15){
					BigDecimal valAfter = timeSeries.get(nextTSAfterPrecxition);
					if(valAfter.equals(valBefore)){
						result.addTestExample(pred.getSecond(), Change.EQUAL);
					} else{
						if(pred.getSecond()==Change.UP){
							longResult.add(valAfter.subtract(valBefore));
						} else if(pred.getSecond()==Change.DOWN){
							shortResult.add(valBefore.subtract(valAfter));
						}
						if(valAfter.compareTo(valBefore)>0){
							result.addTestExample(pred.getSecond(), Change.UP);
						} else if(valAfter.compareTo(valBefore)<0){
							result.addTestExample(pred.getSecond(), Change.DOWN);
						}
					}
				} else{
					result.addTestExample(pred.getSecond(), Change.EQUAL);
					//was equal
				}
			}
		}
		return new TestResult(longResult,shortResult,result);
	}

	protected boolean before2316(LocalDateTime timestamp) {
		if(timestamp.getHour()<=23){
			return true;
		} else{
			return timestamp.getMinute()<=15;
		}
	}

	protected boolean after16(LocalDateTime timestamp) {
		if(timestamp.getHour() >=16){
			return true;
		} else{
			return false;
		}
	}

	private void applyPredictor(CategoricalEventType toPredict, CategoricalEventStream stream,PredictiveModel model, Method method) throws IOException {
		StreamWindowSlider slider = new StreamWindowSlider(stream,config.getWindowSizeInSeconds());
		List<Pair<LocalDateTime,Change>> predictions = new ArrayList<>();
		while(slider.canSlide()){
			StreamWindow currentWindow = slider.getCurrentWindow();
			assert(ChronoUnit.SECONDS.between(currentWindow.getWindowBorders().getFirst(),currentWindow.getWindowBorders().getSecond())<=config.getWindowSizeInSeconds());
			if(stream.hasNext()){
				assert(stream.peek().getTimestamp().compareTo(currentWindow.getWindowBorders().getSecond())>0);
			}
			Change predicted = model.predict(currentWindow);
			LocalDateTime curTs = currentWindow.getWindowBorders().getSecond();
			predictions.add(new Pair<>(curTs,predicted));
			slider.slideForward();
		}
		//serialize results
		IOService.serializePairList(predictions,IOService.buildPredictionsTargetFile(toPredict.getCompanyID(),method,resultDir));
	}
}
