package prediction.evaluation;

import java.math.BigDecimal;
import java.util.List;

import data.stream.PredictorPerformance;

public class TestResult {

	private List<BigDecimal> longResult;
	private List<BigDecimal> shortResult;
	private PredictorPerformance result;
	
	public TestResult(List<BigDecimal> longResult, List<BigDecimal> shortResult, PredictorPerformance result) {
		super();
		this.longResult = longResult;
		this.shortResult = shortResult;
		this.result = result;
	}

	public List<BigDecimal> getLongResult() {
		return longResult;
	}

	public void setLongResult(List<BigDecimal> longResult) {
		this.longResult = longResult;
	}

	public List<BigDecimal> getShortResult() {
		return shortResult;
	}

	public void setShortResult(List<BigDecimal> shortResult) {
		this.shortResult = shortResult;
	}

	public PredictorPerformance getResult() {
		return result;
	}

	public void setResult(PredictorPerformance result) {
		this.result = result;
	}
	
	

}
