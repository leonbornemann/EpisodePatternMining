package data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;

public class TransformationMain {

	private static String databaseLocationLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Low Level Data\\";
	private static String targetLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Annotated Data\\";
	private static String illegalFormatDirLaptop = "C:\\Users\\LeonBornemann\\Documents\\Uni\\Master thesis\\data\\Low Level Bad Format\\";
	
	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String target = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Data\\";
	private static String illegalFormatDir = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Illegally Formatted";
		
	private static String timeSeriesSourceLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\";
	private static String sectorTimeSeriesSourceLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\";
	
	private static String timeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Time Series\\";
	private static String sectorTimeSeriesTargetLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Annotated Sector Time Series\\";
	
	
	public static void main(String[] args) throws IOException {
		//laptop();
		//desktop();
		timeSeriesToAnnotated();
	}

	private static void timeSeriesToAnnotated() {
		TimeSeriesTransformator transformer = new TimeSeriesTransformator(timeSeriesSourceLocation,timeSeriesTargetLocation);
		transformer.transform();
		transformer = new TimeSeriesTransformator(sectorTimeSeriesSourceLocation,sectorTimeSeriesTargetLocation);
		transformer.transform();
	}

	private static void laptop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(databaseLocationLaptop),new File(targetLaptop),new File(illegalFormatDirLaptop),new BigDecimal("0.001"));
		transformer.transform();
	}

	private static void desktop() throws IOException {
		LowToAnnotatedTransformator transformer = new LowToAnnotatedTransformator(new File(dataBaseLocation),new File(target),new File(illegalFormatDir));
		transformer.transform();
	}
}
