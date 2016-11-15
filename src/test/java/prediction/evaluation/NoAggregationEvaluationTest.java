package prediction.evaluation;

import static org.junit.Assert.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import data.Change;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class NoAggregationEvaluationTest {

	@Test
	public void getActualValueNotEnoughInWindowTestTest() throws IOException{
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		List<Pair<LocalDateTime, BigDecimal>> targetMovement = buildTargetMovement();
		assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(2), targetMovement));
	}
	
	@Test
	public void getActualValueEqualTest() throws IOException{
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		List<Pair<LocalDateTime, BigDecimal>> targetMovement = buildEqualTargetMovement();
		for(int i=0;i<14;i++){
			assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(i), targetMovement));
		}
		assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(19), targetMovement));
	}
	
	@Test
	public void getaActualValueDownTest() throws IOException{
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		List<Pair<LocalDateTime, BigDecimal>> targetMovement = buildDownTargetMovement();
		for(int i=0;i<4;i++){
			assertEquals(Change.DOWN,evaluator.getActualValue(toTime(i), targetMovement));
		}
		assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(4), targetMovement));
		for(int i=5;i<10;i++){
			System.out.println(i);
			assertEquals(Change.DOWN,evaluator.getActualValue(toTime(i), targetMovement));
		}
	}
	
	private List<Pair<LocalDateTime, BigDecimal>> buildDownTargetMovement() {
		List<Pair<LocalDateTime, BigDecimal>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(1),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(3),new BigDecimal(90.0)));
		list.add(new Pair<>(toTime(5),new BigDecimal(90.0)));
		list.add(new Pair<>(toTime(7),new BigDecimal(80.0)));
		list.add(new Pair<>(toTime(9),new BigDecimal(90.0)));
		list.add(new Pair<>(toTime(10),new BigDecimal(70.0)));
		return list;
	}

	@Test
	public void getActualValueUpTest() throws IOException{
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		List<Pair<LocalDateTime, BigDecimal>> targetMovement = buildUpTargetMovement();
		assertEquals(Change.UP,evaluator.getActualValue(toTime(1), targetMovement));
		assertEquals(Change.UP,evaluator.getActualValue(toTime(2), targetMovement));
		assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(3), targetMovement));
		for(int i=4;i<10;i++){
			assertEquals(Change.UP,evaluator.getActualValue(toTime(4), targetMovement));
		}
		assertEquals(Change.EQUAL,evaluator.getActualValue(toTime(10), targetMovement));
	}
	
	
	private List<Pair<LocalDateTime, BigDecimal>> buildUpTargetMovement() {
		List<Pair<LocalDateTime, BigDecimal>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(1),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(3),new BigDecimal(110.0)));
		list.add(new Pair<>(toTime(5),new BigDecimal(120.0)));
		list.add(new Pair<>(toTime(7),new BigDecimal(110.0)));
		list.add(new Pair<>(toTime(9),new BigDecimal(120.0)));
		list.add(new Pair<>(toTime(10),new BigDecimal(130.0)));
		return list;
	}

	private List<Pair<LocalDateTime, BigDecimal>> buildEqualTargetMovement() {
		List<Pair<LocalDateTime, BigDecimal>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(1),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(3),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(5),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(7),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(9),new BigDecimal(100.0)));
		//list.add(new Pair<>(toTime(17),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(20),new BigDecimal(110.0)));
		list.add(new Pair<>(toTime(21),new BigDecimal(110.0)));
		list.add(new Pair<>(toTime(22),new BigDecimal(100.0)));
		return list;
	}

	@Test
	public void evalRateOfReturnForDayTest_GoodPrediction() throws IOException {
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		InvestmentTracker tracker = new InvestmentTracker(new BigDecimal(100.0));
		int start = tracker.netWorth().toBigInteger().intValue();
		evaluator.evalRateOfReturnForDay(buildPred(), buildTargetMovement(), tracker);
		int end = tracker.netWorth().toBigInteger().intValue();
		assertEquals(start*2,end);
	}
	
	@Test
	public void evalRateOfReturnForDayTest_BadPrediction() throws IOException {
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		InvestmentTracker tracker = new InvestmentTracker(new BigDecimal(100.0));
		evaluator.evalRateOfReturnForDay(buildBadPred(), buildTargetMovement(), tracker);
		int end = tracker.netWorth().toBigInteger().intValue();
		assertEquals(0,end);
	}

	@Test
	public void testRateOfReturnPredTooLate() throws IOException{
		NoAggregationEvaluator evaluator = new NoAggregationEvaluator(5, null,null);
		InvestmentTracker tracker = new InvestmentTracker(new BigDecimal(100.0));
		int start = tracker.netWorth().toBigInteger().intValue();
		evaluator.evalRateOfReturnForDay(buildSmallPred(), buildSmallTargetMovement(), tracker);
		int end = tracker.netWorth().toBigInteger().intValue();
		assertEquals(start,end);
	}
	
	private List<Pair<LocalDateTime, BigDecimal>> buildSmallTargetMovement() {
		List<Pair<LocalDateTime, BigDecimal>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(1),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(10),new BigDecimal(150.0)));
		return list;
	}

	private List<Pair<LocalDateTime, Change>> buildSmallPred() {
		List<Pair<LocalDateTime, Change>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(2),Change.DOWN));
		list.add(new Pair<>(toTime(10),Change.UP));
		return list;
	}

	private List<Pair<LocalDateTime, Change>> buildBadPred() {
		List<Pair<LocalDateTime, Change>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(10),Change.DOWN));
		list.add(new Pair<>(toTime(20),Change.UP));
		list.add(new Pair<>(toTime(30),Change.UP));
		list.add(new Pair<>(toTime(40),Change.DOWN));
		return list;
	}

	private List<Pair<LocalDateTime, BigDecimal>> buildTargetMovement() {
		List<Pair<LocalDateTime, BigDecimal>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(1),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(11),new BigDecimal(150.0)));
		list.add(new Pair<>(toTime(21),new BigDecimal(100.0)));
		list.add(new Pair<>(toTime(31),new BigDecimal(50.0)));
		list.add(new Pair<>(toTime(41),new BigDecimal(100.0)));
		return list;
	}

	private List<Pair<LocalDateTime, Change>> buildPred() {
		List<Pair<LocalDateTime, Change>> list = new ArrayList<>();
		list.add(new Pair<>(toTime(10),Change.UP));
		list.add(new Pair<>(toTime(20),Change.DOWN));
		list.add(new Pair<>(toTime(30),Change.DOWN));
		list.add(new Pair<>(toTime(40),Change.UP));
		return list;
	}

	private LocalDateTime toTime(int i) {
		LocalDateTime currentTime = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		return currentTime.plus(i, ChronoUnit.SECONDS);
	}

}
