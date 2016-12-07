package info;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import prediction.util.IOService;
import util.Pair;

public class TimeSeriesInfo {

	private Map<String,TreeMap<LocalDateTime,BigDecimal>> timeSeries = new HashMap<>();
	
	public TimeSeriesInfo(File streamDir, File lowLevelSectorStreamDirDesktop) throws IOException{
		timeSeries = loadTimeSeries(Arrays.asList(streamDir,lowLevelSectorStreamDirDesktop));
	}
	
	public void printInfo(double percentageChange){
		System.out.println("Company \t\t Events Per Day Base \t\t Events per Day > "+percentageChange*100 + "% Change \t\t Shrincage [%]");
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.CEILING);
		double aggregatedNormal = 0.0;
		double aggregatedThreshold = 0.0;
		for(String k: timeSeries.keySet()){
			TreeMap<LocalDateTime, BigDecimal> thisTimeSeries = timeSeries.get(k);
			Map<LocalDate, List<LocalDateTime>> byDay = thisTimeSeries.keySet().stream().collect(Collectors.groupingBy(dt -> LocalDate.from(dt)));
			Map<LocalDate, List<BigDecimal>> byDayValues = new HashMap<>();
			for(LocalDate date : byDay.keySet()){
				byDayValues.put(date,byDay.get(date).stream().map(dt -> thisTimeSeries.get(dt)).collect(Collectors.toList()));
			}
			double eventsPerDayNormal = byDayValues.values().stream().map(l -> getNonZeroDiff(l)).mapToInt(l -> l.size()).average().getAsDouble();
			double eventsPerDayThreshold = byDayValues.values().stream().map(l -> getNonZeroRelativeDiff(l,percentageChange)).mapToInt(l -> l.size()).average().getAsDouble();
			System.out.println(k + "\t\t" + df.format(eventsPerDayNormal) + "\t\t" + df.format(eventsPerDayThreshold) + "\t\t" +eventsPerDayThreshold/eventsPerDayNormal*100);
			aggregatedNormal += eventsPerDayNormal;
			aggregatedThreshold += eventsPerDayThreshold;
		}
		System.out.println("Total \t\t" + df.format(aggregatedNormal) + "\t\t" + df.format(aggregatedThreshold) + "\t\t" +aggregatedThreshold/aggregatedNormal*100);
	}
	
	private List<BigDecimal> getNonZeroRelativeDiff(List<BigDecimal> timeSeries,double percentageChange) {
		List<BigDecimal> diff = new ArrayList<>();
		Iterator<BigDecimal> it = timeSeries.iterator();
		BigDecimal prev = it.next();
		BigDecimal threshold = new BigDecimal(percentageChange);
		while(it.hasNext()){
			BigDecimal cur = it.next();
			if(!cur.equals(prev)){
				BigDecimal relativeDiff = cur.subtract(prev).divide(prev, 100, RoundingMode.FLOOR);
				if(relativeDiff.abs().compareTo(threshold)>=0){
					diff.add(cur.subtract(prev).divide(prev, 100, RoundingMode.FLOOR));
				}
			}
			prev = cur;
		}
		return diff;
	}
	
	private Map<String, TreeMap<LocalDateTime,BigDecimal>> loadTimeSeries(List<File> dirs) throws IOException {
		List<File> allFiles = dirs.stream().flatMap(dir -> Arrays.stream(dir.listFiles())).collect(Collectors.toList());
		Map<String, TreeMap<LocalDateTime,BigDecimal>> companyMovements = new HashMap<>();
		for(File file : allFiles){
			if(file.isFile() && file.getName().endsWith(".csv")){
				companyMovements.put(file.getName().split("\\.")[0], IOService.readTimeSeries(file));
			}
		}
		return companyMovements;
	}
	
	private List<BigDecimal> getNonZeroDiff(List<BigDecimal> timeSeries) {
		List<BigDecimal> diff = new ArrayList<>();
		for(int i=1;i<timeSeries.size();i++){
			BigDecimal prevVal = timeSeries.get(i-1);
			BigDecimal curVal = timeSeries.get(i);
			if(!curVal.equals(prevVal)){
				diff.add(curVal.subtract(prevVal));
			}
		}
		return diff;
	}
}
