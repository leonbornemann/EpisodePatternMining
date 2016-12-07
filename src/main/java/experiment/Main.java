package experiment;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import prediction.mining.Method;

public class Main {

	public static void main(String[] args) throws ClassNotFoundException, IOException {
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Toy stuff\\Run 1");
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.75, 0.75, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute(Method.RandomGuessing);
//		executor.printEvaluationResult(Method.RandomGuessing);
		//supportSerialRuns();
		//numerOfWindowsRuns();
		//permsOnly();
		//semanticCounterpartRuns();
		//avgForecastingRun();
		ThresholdRun();
		//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 1");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.75, 0.75, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
		//next run:
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 2");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 3");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.7, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 4");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.6, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 5");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 6");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.4, 20);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 7");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, true, 0.95, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 8");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.95, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 9");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(150, 90, false, 0.8, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 10");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(200, 90, false, 0.8, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 11");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(250, 90, false, 0.8, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 12");
//		resultDir.mkdir();
//		config = new EvaluationConfig(300, 90, false, 0.8, 0.8, 20);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 13");
//		resultDir.mkdir();
//		config = new EvaluationConfig(350, 90, false, 0.8, 0.8, 20);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 14");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(400, 90, false, 0.8, 0.8, 20);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		File resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 15");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 10);
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 16");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 30);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 17");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 40);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 18");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 50);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 19");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 100);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 20");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 200);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 21");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 300);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 22");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 400);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 23");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 500);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
//		resultDir = new File("C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Run 24");
//		resultDir.mkdir();
//		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 1000);
//		executor = new ExperimentExecutor(config, resultDir);
//		executor.execute();
	}

	private static void ThresholdRun() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\New Experiments\\Threshold\\";
//		File resultDir = new File(dirPath + "Run 1");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.2, 0.2, 20);
//		File annotatedDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series 20 Percent\\");
//		File annotatedSectorDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series 20 Percent\\");
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, annotatedDir,annotatedSectorDir);
//		executor.execute(Method.PERMS);
		//more support
//		File resultDir = new File(dirPath + "Run 2");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 180, false, 0.6, 0.4, 1000);
//		File annotatedDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series 20 Percent\\");
//		File annotatedSectorDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series 20 Percent\\");
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, annotatedDir,annotatedSectorDir);
//		executor.execute();
//		File resultDir = new File(dirPath + "Run 3");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 90, true, 0.4, 0.1, 100);
//		File annotatedDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series 20 Percent\\");
//		File annotatedSectorDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series 20 Percent\\");
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, annotatedDir,annotatedSectorDir);
//		executor.execute();
//		File resultDir = new File(dirPath + "Run 4");
//		resultDir.mkdir();
//		EvaluationConfig config = new EvaluationConfig(100, 180, false, 0.7, 0.7, 20);
//		File annotatedDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series 20 Percent\\");
//		File annotatedSectorDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series 20 Percent\\");
//		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, annotatedDir,annotatedSectorDir);
//		executor.execute();
		File resultDir = new File(dirPath + "Run 5");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 180, true, 0.7, 0.7, 20);
		File annotatedDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series 20 Percent\\");
		File annotatedSectorDir = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series 20 Percent\\");
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir, annotatedDir,annotatedSectorDir);
		executor.execute();
		
	}

	private static void avgForecastingRun() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Average Forecasting\\";
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute(Method.SimpleAverageForecasting);
	}

	private static void permsOnly() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Perms Only\\";
		File resultDir = new File(dirPath +"Run 15");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 10);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 16");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 30);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 17");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 40);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 18");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 50);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 19");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 100);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 20");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 200);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 21");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 300);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 22");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 400);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 23");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 500);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath +"Run 24");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 1000);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
	}

	private static void numerOfWindowsRuns() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Number Of Windows\\";
		File resultDir = new File(dirPath+"Run 2");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 9");
		resultDir.mkdir();
		config = new EvaluationConfig(150, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 10");
		resultDir.mkdir();
		config = new EvaluationConfig(200, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 11");
		resultDir.mkdir();
		config = new EvaluationConfig(250, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 12");
		resultDir.mkdir();
		config = new EvaluationConfig(300, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 13");
		resultDir.mkdir();
		config = new EvaluationConfig(350, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath+"Run 14");
		resultDir.mkdir();
		config = new EvaluationConfig(400, 90, false, 0.8, 0.8, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
	}

	private static void supportSerialRuns() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Support Serial Modified\\";
		File resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.8, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 4");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 5");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.5, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 6");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.8, 0.4, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		
	}

	private static void semanticRuns() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics\\";
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, true, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, true, 0.95, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, true, 0.95, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
	}
	
	private static void semanticCounterpartRuns() throws ClassNotFoundException, IOException {
		String dirPath = "C:\\Users\\Leon Bornemann\\Desktop\\base Folder\\Final Runs\\Using semantics Counterpart\\";
		File resultDir = new File(dirPath + "Run 1");
		resultDir.mkdir();
		EvaluationConfig config = new EvaluationConfig(100, 90, false, 0.95, 0.8, 20);
		ExperimentExecutor executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 2");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.95, 0.7, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
		resultDir = new File(dirPath + "Run 3");
		resultDir.mkdir();
		config = new EvaluationConfig(100, 90, false, 0.95, 0.6, 20);
		executor = new ExperimentExecutor(config, resultDir);
		executor.execute();
	}

}
