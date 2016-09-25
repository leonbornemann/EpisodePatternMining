package data.transformation;

import java.io.File;
import java.io.IOException;

public class SingleTimeSeriesExtractor {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String timeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\";
	
	public static void main(String[] args) throws IOException {
		String id = "QCOM";
		File target = new File(timeSeriesTargetLocation + id + ".csv");
		TimeSeriesExtractor extractor = new TimeSeriesExtractor(dataBaseLocation);
		extractor.extractTimeSeries(id,target);
	}

}
