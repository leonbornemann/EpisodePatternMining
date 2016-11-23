package data.transformation;

import java.io.IOException;
import java.util.Set;

import semantic.SemanticKnowledgeCollector;

public class MultiTimeSeriesExtractor {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String timeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series New\\";
	private static String sectorTimeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\";
	
	public static void main(String[] args) throws IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		SingleTimeSeriesExtractor extractor = new SingleTimeSeriesExtractor(dataBaseLocation);
		extractor.extractAllTimeSeries(annotatedCompanyCodes,timeSeriesTargetLocation);
//		Map<String,Set<String>> codesBySector = IOService.getCodeBySector();
//		codesBySector.keySet().forEach( s -> {
//			System.out.println(s);
//			System.out.println(codesBySector.get(s).size());
//			//codesBySector.get(s).forEach(c -> System.out.println("\t" + c));;
//		});
//		SectorTimeSeriesExtractor extractor = new SectorTimeSeriesExtractor(dataBaseLocation);
//		extractor.extract(codesBySector,sectorTimeSeriesTargetLocation);
	}

}
