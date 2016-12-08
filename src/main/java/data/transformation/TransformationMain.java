package data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

/***
 * Main class that starts the transformation of time series to categorical event streams
 * @author Leon Bornemann
 *
 */
public class TransformationMain {
			
	private static String timeSeriesSourceLocation;
	private static String sectorTimeSeriesSourceLocation;
	
	private static String timeSeriesTargetLocation;
	private static String sectorTimeSeriesTargetLocation;
	
	private static String timeSeriesThresholdTargetLocation;
	private static String sectorTimeSeriesThresholdTargetLocation;

	/***
	 * @param args contains one argument, which is the working directory in which the started process must have read and write access
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String workingDir = args[0];
		if(!workingDir.endsWith(File.separator)){
			workingDir = workingDir + File.separator;
		}
		timeSeriesSourceLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Time Series" + File.separator;
		sectorTimeSeriesSourceLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Sector Time Series" + File.separator;
		
		timeSeriesTargetLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Event Streams" + File.separator;
		sectorTimeSeriesTargetLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Sector Event Streams" + File.separator;
		
		timeSeriesThresholdTargetLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Threshold Event Streams" + File.separator;
		sectorTimeSeriesThresholdTargetLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Categorical Sector Threshold Event Streams" + File.separator;
		mkTargetdirs();
		
		timeSeriesToAnnotated();
	}

	private static void mkTargetdirs() {
		new File(timeSeriesTargetLocation).mkdirs();
		new File(sectorTimeSeriesTargetLocation).mkdirs();
		new File(timeSeriesThresholdTargetLocation).mkdirs();
		new File(sectorTimeSeriesThresholdTargetLocation).mkdirs();
	}

	private static void timeSeriesToAnnotated() {
		normal();
		threshold();
	}

	private static void normal() {
		TimeSeriesTransformator transformer = new TimeSeriesTransformator(timeSeriesSourceLocation,timeSeriesTargetLocation);
		transformer.transform();
		transformer = new TimeSeriesTransformator(sectorTimeSeriesSourceLocation,sectorTimeSeriesTargetLocation);
		transformer.transform();
	}

	private static void threshold() {
		TimeSeriesTransformator transformer = new TimeSeriesTransformator(timeSeriesSourceLocation,timeSeriesThresholdTargetLocation,0.001);
		transformer.transform();
		transformer = new TimeSeriesTransformator(sectorTimeSeriesSourceLocation,sectorTimeSeriesThresholdTargetLocation,0.001);
		transformer.transform();
	}
}
