package evaluation;

import java.io.Serializable;
import java.util.Collection;

import data.events.Change;

public class ConfusionMatrix implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int[][] matrix = new int[3][3];
	
	private int toIndex(Change change){
		if(change==Change.UP){
			return 0;
		} else if(change == Change.EQUAL){
			return 1;
		} else{
			return 2;
		}
	}
	
	public ConfusionMatrix(){
		
	}
	
	public ConfusionMatrix(Collection<ConfusionMatrix> collection ){
		collection.forEach(perf ->this.addAllExamples(perf));
	}
	
	public void addTestExample(Change predicted, Change actual){
		int i = toIndex(actual);
		int j = toIndex(predicted);
		matrix[i][j]++;
	}

	public double getPrecision(Change change) {
		int i = toIndex(change);
		return (double) matrix[i][i] / (matrix[0][i] + matrix[1][i] + matrix[2][i] );
	}

	public double getRecall(Change change) {
		int i = toIndex(change);
		return (double) matrix[i][i] / (matrix[i][0] + matrix[i][1] + matrix[i][2] );
	}
	
	public double getEqualIgnoredRecall(Change change){
		assert(change != Change.EQUAL);
		int i = toIndex(change);
		return (double) matrix[i][i] / (matrix[i][0] + matrix[i][2] );
	}
	
	public double getEqualIgnoredPrecision(Change change){
		assert(change != Change.EQUAL);
		int i = toIndex(change);
		return (double) matrix[i][i] / (matrix[0][i] + matrix[2][i] );
	}
	
	public void printConfusionMatrix(){
		System.out.println("\t\t\t Predicted");
		System.out.println("\t\t UP \t EQUAL \t DOWN");
		System.out.println("\t UP \t " + matrix[0][0] + " \t " + matrix[0][1]+ " \t " + matrix[0][2]);
		System.out.println("Actual \t EQUAL \t " + matrix[1][0] + " \t " + matrix[1][1]+ " \t " + matrix[1][2]);
		System.out.println("\t DOWN \t " + matrix[2][0] + " \t " + matrix[2][1]+ " \t " + matrix[2][2]);
	}

	public void addAllExamples(ConfusionMatrix thisDayPerformance) {
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				matrix[i][j] += thisDayPerformance.matrix[i][j];
			}
		}
	}

	public int getNumClassifiedExamples() {
		int sum = 0;
		for (int i = 0; i < matrix.length; i++) {
			for (int j = 0; j < matrix[0].length; j++) {
				sum += matrix[i][j];
			}
		}
		return sum;
	}

	public double getAccuracy() {
		return ((double) matrix[0][0] + matrix[1][1] + matrix[2][2]) / (double) getNumClassifiedExamples();
	}

	public double getEqualIgnoredAccuracy() {
		return ((double) matrix[0][0] + matrix[2][2]) / ((double) matrix[0][0] + matrix[2][2] + matrix[0][2] + matrix[2][0]);
	}

	public double getFalsePositiveRate(Change change) {
		int i = toIndex(change);
		int j;
		if(change == Change.DOWN){
			j = toIndex(Change.UP);
		} else{
			j = toIndex(Change.DOWN);
		}
		return matrix[j][i] / (double)(matrix[j][0] + matrix[j][1] + matrix[j][2]);
	}

	public int getUp_UP() {
		return matrix[toIndex(Change.UP)][toIndex(Change.UP)];
	}

	public int getUp_DOWN() {
		return matrix[toIndex(Change.UP)][toIndex(Change.DOWN)];
	}
	
	public int getDOWN_DOWN() {
		return matrix[toIndex(Change.DOWN)][toIndex(Change.DOWN)];
	}

	public int getDOWN_UP() {
		return matrix[toIndex(Change.DOWN)][toIndex(Change.UP)];
	}

}
