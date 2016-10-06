package data.transformation;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import semantic.SemanticKnowledgeCollector;

public class SingleTimeSeriesExtractor {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String timeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\";
	
	public static void main(String[] args) throws IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		TimeSeriesExtractor extractor = new TimeSeriesExtractor(dataBaseLocation);
		extractor.extractAllTimeSeries(annotatedCompanyCodes,timeSeriesTargetLocation);
	}

}
