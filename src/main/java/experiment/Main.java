package experiment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import prediction.models.Method;

/***
 * Main class to run experiments - warning - a call to the main method will take a lot of time (probably several days). 
 * In order to run a single experiment, just extract the code from the static methods and 
 * @author Leon Bornemann
 *
 */
public class Main {

	private static String sectorTimeSeriesDir;
	private static String timeSeriesDir;
	private static String categoricalSectorStreamDir;
	private static String categoricalStreamDir;
	private static String workingDir;

	public static void main(String[] args) throws ClassNotFoundException, IOException {
		workingDir = args[0];
		if(!workingDir.endsWith(File.separator)){
			workingDir = workingDir + File.separator;
		}
		sectorTimeSeriesDir = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Sector Time Series" + File.separator;
		timeSeriesDir = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Time Series" + File.separator;
		categoricalSectorStreamDir = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Sector Event Streams" + File.separator;
		categoricalStreamDir = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Event Streams" + File.separator;

		
		supportSerialRuns();
		numerOfWindowsRuns();
		permsOnly();
		semanticCounterpartRuns();
		avgForecastingRun();
		ThresholdRun();
	}

	private static void ThresholdRun() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "New Experiments" + File.separator + "Threshold" + File.separator;
		File categoricalThresholdDir = new File(workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Threshold Event Streams" + File.separator);
		File categoricalSectorThresholdDir = new File(workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Sector Threshold Event Streams" + File.separator);
		
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.2, 0.2, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, categoricalThresholdDir, categoricalSectorThresholdDir, new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute(Method.PERMS);
		resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 180, false, 0.6, 0.4, 1000);
		executor = new ExperimentExecutor(config, resultDir, categoricalThresholdDir, categoricalSectorThresholdDir, new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, true, 0.4, 0.1, 100);
		executor = new ExperimentExecutor(config, resultDir, categoricalThresholdDir, categoricalSectorThresholdDir, new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 4");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 180, false, 0.7, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir, categoricalThresholdDir, categoricalSectorThresholdDir, new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 5");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 180, true, 0.7, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir, categoricalThresholdDir, categoricalSectorThresholdDir, new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		
	}

	private static void avgForecastingRun() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Average Forecasting" + File.separator;
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute(Method.SimpleAverageForecasting);
	}

	private static void permsOnly() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Perms Only" + File.separator;
		File resultDir = new File(dirPath +"Run 15");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 10);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 16");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 30);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 17");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 40);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 18");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 50);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 19");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 100);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 20");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 200);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 21");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 300);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 22");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 400);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 23");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 500);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath +"Run 24");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 1000);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
	}

	private static void numerOfWindowsRuns() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Number Of Windows" + File.separator;
		File resultDir = new File(dirPath+"Run 2");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 9");
		resultDir.mkdir();
		config = new EvaluationConfig(150, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 10");
		resultDir.mkdir();
		config = new EvaluationConfig(200, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 11");
		resultDir.mkdir();
		config = new EvaluationConfig(250, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 12");
		resultDir.mkdir();
		config = new EvaluationConfig(300, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 13");
		resultDir.mkdir();
		config = new EvaluationConfig(350, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath+"Run 14");
		resultDir.mkdir();
		config = new EvaluationConfig(400, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
	}

	private static void supportSerialRuns() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Support Serial Modified" + File.separator;
		File resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 4");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 5");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 6");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.4, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		
	}

	private static void semanticRuns() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Using semantics" + File.separator;
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, true, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, true, 0.95, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, true, 0.95, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
	}
	
	private static void semanticCounterpartRuns() throws ClassNotFoundException, IOException {
		String dirPath = workingDir + "Experiments" + File.separator + "Using semantics Counterpart" + File.separator;
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.95, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.95, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir, new File(categoricalStreamDir), new File(categoricalSectorStreamDir), new File(timeSeriesDir), new File(sectorTimeSeriesDir));
		executor.execute();
	}

}
