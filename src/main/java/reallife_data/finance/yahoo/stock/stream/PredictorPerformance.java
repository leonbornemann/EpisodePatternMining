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
	
	@Override
	public String toString(){
		return "[TP: "+truePositives + " TN: " + trueNegatives + " FP: " + falsePositives + " FN: " + falseNegatives + "]";
	}

	public double getPrecision() {
		return ((double) truePositives) / (truePositives + falsePositives);
	}
	
	public double getRecall(){
		return ((double) truePositives) / (truePositives + falseNegatives);
	}
	
	public double getAccuracy(){
		return ((double) truePositives + trueNegatives) / (truePositives + trueNegatives + falsePositives + falseNegatives);
	}

}
