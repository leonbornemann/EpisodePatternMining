package data.transformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import data.LowLevelEvent;
import prediction.util.StandardDateTimeFormatter;

public class SingleTimeSeriesExtractor extends TimeSeriesExtractor{

	public SingleTimeSeriesExtractor(String dataBaseLocation) {
		super(dataBaseLocation);
	}

	public void extractTimeSeries(String id, File target) throws IOException {
		List<File> allFiles = getSortedFileList();
		PrintWriter writer = new PrintWriter(new FileWriter(target));
		writer.println("time,value");
		for(int i=0;i<allFiles.size();i++){
			File file = allFiles.get(i);
			List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(file);
			List<LowLevelEvent> timeSeries = lowLevelEvents.stream().filter(e -> e.getCompanyId().equals("\""+id + "\"")).sorted(LowLevelEvent::temporalOrder).collect(Collectors.toList());
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

	public void extractAllTimeSeries(Set<String> ids, String timeSeriesTargetLocation) throws IOException {
		List<File> allFiles = getSortedFileList();
		Map<String,PrintWriter> writers = new HashMap<>();
		for(String id : ids){
			writers.put(id, initFile(timeSeriesTargetLocation,id));
		}
		for(int i=0;i<allFiles.size();i++){
			File file = allFiles.get(i);
			List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(file);
			List<LowLevelEvent> allTimeSeries = lowLevelEvents.stream().filter(
					e -> ids.contains(e.getCompanyId().replaceAll("\"", "")) && 
						after16(e.getTimestamp()) &&
						before2316(e.getTimestamp())).
					map(e -> new LowLevelEvent(e.getCompanyId().replaceAll("\"", ""), e.getValue(), e.getTimestamp())).
					collect(Collectors.toList());
			Map<String, List<LowLevelEvent>> byId = allTimeSeries.stream().collect(Collectors.groupingBy(LowLevelEvent::getCompanyId));
			//assert(byId.keySet().equals(ids));
			for(String id : byId.keySet()){
				List<LowLevelEvent> timeSeries = byId.get(id).stream().sorted(LowLevelEvent::temporalOrder).collect(Collectors.toList());
				PrintWriter writer = writers.get(id);
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
		}
		writers.values().forEach(w -> w.close());
	}

}
