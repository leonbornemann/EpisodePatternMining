package experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Properties;

/***
 * Config class to save all parameters of an experimental setting to evaluate the algorithms.
 * @author Leon Bornemann
 *
 */
public class EvaluationConfig {

	private int numWindows;
	private int windowSizeInSeconds;
	private boolean useSemantics;
	private double supportParallel;
	private double supportSerial;
	private int PERMSNumPredictors;
	
	public EvaluationConfig(int numWindows, int windowSizeInSeconds, boolean useSemantics, double supportParallel,double supportSerial, int pERMSNumPredictors) {
		this.numWindows = numWindows;
		this.windowSizeInSeconds = windowSizeInSeconds;
		this.useSemantics = useSemantics;
		this.supportParallel = supportParallel;
		this.supportSerial = supportSerial;
		this.PERMSNumPredictors = pERMSNumPredictors;
	}
	
	public EvaluationConfig(File file) throws FileNotFoundException, IOException{
		Properties properties = new Properties();
		properties.load(new FileReader(file));
		this.numWindows = Integer.parseInt(properties.getProperty("numWindows"));
		this.windowSizeInSeconds = Integer.parseInt(properties.getProperty("windowSizeInSeconds"));
		this.useSemantics = Boolean.parseBoolean(properties.getProperty("useSemantics"));
		this.supportParallel = Double.parseDouble(properties.getProperty("supportParallel"));
		this.supportSerial = Double.parseDouble(properties.getProperty("supportSerial"));
		this.PERMSNumPredictors = Integer.parseInt(properties.getProperty("PERMSNumPredictors"));
	}
	
	public void serialize(File file) throws IOException{
		Properties properties = new Properties();
		properties.setProperty("numWindows", ""+numWindows);
		properties.setProperty("windowSizeInSeconds", ""+windowSizeInSeconds);
		properties.setProperty("useSemantics", ""+useSemantics);
		properties.setProperty("supportParallel", ""+supportParallel);
		properties.setProperty("supportSerial", ""+supportSerial);
		properties.setProperty("PERMSNumPredictors", ""+PERMSNumPredictors);
		properties.store(new FileWriter(file), "");
	}

	public int getNumWindows() {
		return numWindows;
	}

	public int getWindowSizeInSeconds() {
		return windowSizeInSeconds;
	}

	public boolean isUseSemantics() {
		return useSemantics;
	}

	public double getSupportParallel() {
		return supportParallel;
	}

	public double getSupportSerial() {
		return supportSerial;
	}

	public int getPERMSNumPredictors() {
		return PERMSNumPredictors;
	}	
}
