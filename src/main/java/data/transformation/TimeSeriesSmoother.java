package data.transformation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class TimeSeriesSmoother {

	public void smoothAll(File timeSeriesDir,File targetDir) throws IOException{
		for(File file : Arrays.asList(timeSeriesDir.listFiles())){
			List<Pair<LocalDateTime, BigDecimal>> data = IOService.readTimeSeriesData(file);
			List<Pair<LocalDateTime, BigDecimal>> smoothedData = smooth(data);
			PrintWriter writer = new PrintWriter(new FileWriter(targetDir.getAbsolutePath() + File.separator + file.getName()));
			writer.println("time,value");
			for(int j=0;j<smoothedData.size();j++){
				Pair<LocalDateTime, BigDecimal> e = smoothedData.get(j);
				String toPrint = e.getFirst().format(StandardDateTimeFormatter.getStandardDateTimeFormatter()) + "," + e.getSecond();
				if(j==smoothedData.size()-1){
					writer.print(toPrint);
				} else{
					writer.println(toPrint);
				}
			}
			writer.close();
		}
	}

	private List<Pair<LocalDateTime, BigDecimal>> smooth(List<Pair<LocalDateTime, BigDecimal>> data) {
		List<Pair<LocalDateTime, BigDecimal>> diffVector = getDiff(data);
		assert(diffVector.size()==data.size()-1);
		BigDecimal largeDiffThreshold = new BigDecimal("0.05");
		BigDecimal toAdd = BigDecimal.ZERO;
		List<Pair<LocalDateTime, BigDecimal>> smoothed = new ArrayList<>();
		smoothed.add(data.get(0));
		for(int i=1;i<data.size();i++){
			BigDecimal prevValue = data.get(i-1).getSecond();
			LocalDate curDate = LocalDate.from(data.get(i).getFirst());
			LocalDate prevDate = LocalDate.from(data.get(i).getFirst());
			BigDecimal curDiff = diffVector.get(i-1).getSecond();
			if(curDiff.abs().compareTo(prevValue.multiply(largeDiffThreshold))>=0 && prevDate.equals(curDate)){
				System.out.println(prevValue + "   " +curDiff);
				toAdd = toAdd.add(curDiff.negate());
			} 
			BigDecimal smoothedValue = data.get(i).getSecond().add(toAdd);
			if(smoothedValue.compareTo(BigDecimal.ZERO)<=0){
				smoothedValue = new BigDecimal("0.0001");
			}
			smoothed.add(new Pair<>(data.get(i).getFirst(),smoothedValue));
			//BigDecimal newDiff = smoothed.get(i).getSecond().subtract(smoothed.get(i-1).getSecond());
			//boolean holds = newDiff.abs().compareTo(smoothed.get(i).getSecond().multiply(largeDiffThreshold))<0;
			//assert(holds);
		}
		return smoothed;
	}
	
	public List<Pair<LocalDateTime, BigDecimal>> getDiff(List<Pair<LocalDateTime, BigDecimal>> targetMovement) {
		BigDecimal prev = targetMovement.get(0).getSecond();
		List<Pair<LocalDateTime, BigDecimal>> diff = new ArrayList<>();
		for(int i=1;i<targetMovement.size();i++){
			Pair<LocalDateTime, BigDecimal> current = targetMovement.get(i);
			diff.add(new Pair<>(current.getFirst(),current.getSecond().subtract(prev)));
			prev = current.getSecond();
		}
		return diff;
	}
}
