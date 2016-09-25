package data.transformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import data.LowLevelEvent;
import prediction.util.StandardDateTimeFormatter;

public class TimeSeriesExtractor {

	private File dataBaseLocation;

	public TimeSeriesExtractor(String dataBaseLocation) {
		this.dataBaseLocation = new File(dataBaseLocation);
	}

	public void extractTimeSeries(String id, File target) throws IOException {
		List<File> allFiles = Arrays.asList(dataBaseLocation.listFiles()).stream().
				filter(f -> f.isFile() &&f.getName().endsWith(".csv")).
				sorted((f1,f2) -> f1.getName().compareTo(f2.getName())).
				collect(Collectors.toList());
		PrintWriter writer = new PrintWriter(new FileWriter(target));
		writer.println("time,value");
		for(int i=0;i<allFiles.size();i++){
			File file = allFiles.get(i);
			List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(file);
			List<LowLevelEvent> timeSeries = lowLevelEvents.stream().filter(e -> e.getCompanyId().equals("\""+id + "\"")).sorted(LowLevelEvent::temporalOrder).collect(Collectors.toList());
			System.out.println("yo");
			for(int j=0;j<timeSeries.size();j++){
				LowLevelEvent e = timeSeries.get(j);
				String toPrint = e.getTimestamp().format(StandardDateTimeFormatter.getStandardDateTimeFormatter()) + "," + e.getValue();
				if(j==timeSeries.size()-1 && i==allFiles.size()-1){
					writer.print(toPrint);
				} else{
					writer.println(toPrint);
				}
			}			
		}
		writer.close();
		
	}

}
