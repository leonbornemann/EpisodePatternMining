package data.transformation;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

import semantics.SemanticKnowledgeCollector;
import util.IOService;

/***
 * Main class that extracts time series from the raw data extracted by data.crawling.SimpleCrawler
 * @author Leon Bornemann
 *
 */
public class MultiTimeSeriesExtractor {

	private static String dataBaseLocation;
	private static String timeSeriesTargetLocation;
	private static String sectorTimeSeriesTargetLocation;
	
	/***
	 * @param args contains one argument, which is the working directory in which the started process must have read and write access
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		String workingDir = args[0];
		if(!workingDir.endsWith(File.separator)){
			workingDir = workingDir + File.separator;
		}
		dataBaseLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Low Level Data" + File.separator;
		timeSeriesTargetLocation = workingDir +"Datasets" + File.separator + "Finance" + File.separator + "Time Series" + File.separator;
		sectorTimeSeriesTargetLocation = workingDir +"Datasets" + File.separator + "Finance" + File.separator + "Sector Time Series" + File.separator;
		mkTargetDirs();
		normalTimeSeriesExtraction();
		sectorTimeSeriesExtraction();
	}

	private static void mkTargetDirs() {
		new File(timeSeriesTargetLocation).mkdirs();
		new File(sectorTimeSeriesTargetLocation).mkdirs();
	}

	private static void sectorTimeSeriesExtraction() throws IOException {
		Map<String,Set<String>> codesBySector = IOService.getCodeBySector();
		SectorTimeSeriesExtractor extractor = new SectorTimeSeriesExtractor(dataBaseLocation);
		extractor.extract(codesBySector,sectorTimeSeriesTargetLocation);
	}

	private static void normalTimeSeriesExtraction() throws IOException {
		Set<String> annotatedCompanyCodes = new SemanticKnowledgeCollector().getAnnotatedCompanyCodes();
		SingleTimeSeriesExtractor extractor = new SingleTimeSeriesExtractor(dataBaseLocation);
		extractor.extractAllTimeSeries(annotatedCompanyCodes,timeSeriesTargetLocation);
	}

}
