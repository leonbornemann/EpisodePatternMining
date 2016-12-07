package info;

import java.io.File;
import java.io.IOException;

public class Main {

	private static File lowLevelStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Time Series\\");
	private static File lowLevelSectorStreamDirDesktop = new File("D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Sector Time Series\\");

	
	public static void main(String[] args) throws IOException {
		TimeSeriesInfo info = new TimeSeriesInfo(lowLevelStreamDirDesktop,lowLevelSectorStreamDirDesktop);
		info.printInfo(0.0005);
	}

}
