package reallife_data.finance.yahoo.stock.stream;

public class PredictorPerformance {

	private int falseNegatives = 0;
	private int falsePositives = 0;
	private int truePositives = 0;
	private int trueNegatives = 0;
	
	public void incFalseNegatives(){
		falseNegatives++;
	}
	
	public void incFalsePositives(){
		falsePositives++;
	}
	
	public void incTruePositives(){
		truePositives++;
	}
	
	public void incTrueNegatives(){
		trueNegatives++;
	}

}
