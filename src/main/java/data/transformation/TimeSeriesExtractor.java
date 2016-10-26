package data.transformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public abstract class TimeSeriesExtractor {

	protected File dataBaseLocation;
	
	public TimeSeriesExtractor(String dataBaseLocation) {
		this.dataBaseLocation = new File(dataBaseLocation);
	}
	
	protected PrintWriter initFile(String timeSeriesTargetLocation, String name) throws IOException {
		PrintWriter pr = new PrintWriter(new FileWriter(timeSeriesTargetLocation + name + ".csv"));
		pr.println("time,value");
		return pr;
	}

	protected List<File> getSortedFileList() {
		return Arrays.asList(dataBaseLocation.listFiles()).stream().
		filter(f -> f.isFile() &&f.getName().endsWith(".csv")).
		sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).
		collect(Collectors.toList());
	}
	
	protected boolean before2316(LocalDateTime timestamp) {
		if(timestamp.getHour()<=23){
			return true;
		} else{
			return timestamp.getMinute()<=15;
		}
	}

	protected boolean after16(LocalDateTime timestamp) {
		if(timestamp.getHour() >=16){
			return true;
		} else{
			return false;
		}
	}
}
