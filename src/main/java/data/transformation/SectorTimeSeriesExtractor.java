package data.transformation;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import data.LowLevelEvent;
import prediction.util.StandardDateTimeFormatter;

public class SectorTimeSeriesExtractor extends TimeSeriesExtractor{

	public SectorTimeSeriesExtractor(String dataBaseLocation) {
		super(dataBaseLocation);
	}

	public void extract(Map<String, Set<String>> codesBySector, String timeSeriesTargetLocation) throws IOException {
		List<File> allFiles = getSortedFileList();
		Map<String,PrintWriter> writers = new HashMap<>();
		for(String sector : codesBySector.keySet()){
			writers.put(sector, initFile(timeSeriesTargetLocation,sector));
		}
		for(int i=0;i<allFiles.size();i++){
			File file = allFiles.get(i);
			List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(file);
			for(String sector : codesBySector.keySet()){
				Set<String> targetCodes = codesBySector.get(sector);
				TreeMap<LocalDateTime, List<LowLevelEvent>> sectorCompanyValuesByTimestamp = lowLevelEvents.stream().filter(
						e -> targetCodes.contains(e.getCompanyId().replaceAll("\"", "")) && 
							after16(e.getTimestamp()) &&
							before2316(e.getTimestamp())).
						map(e -> new LowLevelEvent(e.getCompanyId().replaceAll("\"", ""), e.getValue(), e.getTimestamp())).
						collect(Collectors.groupingBy(LowLevelEvent::getTimestamp,TreeMap::new,Collectors.toList()));
						//sorted(LowLevelEvent::temporalOrder).collect(Collectors.toList());
				//assert(byId.keySet().equals(ids));
				Iterator<LocalDateTime> ascendingTimestampIterator = sectorCompanyValuesByTimestamp.keySet().iterator();
				while(ascendingTimestampIterator.hasNext()){
					LocalDateTime ts = ascendingTimestampIterator.next();
					BigDecimal curValue = sectorCompanyValuesByTimestamp.get(ts).stream().map(e -> e.getValue()).reduce((a,b) -> a.add(b) ).get();
					PrintWriter writer = writers.get(sector);
					String toPrint = ts.format(StandardDateTimeFormatter.getStandardDateTimeFormatter()) + "," + curValue;
					if(!ascendingTimestampIterator.hasNext() && i==allFiles.size()-1){
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
