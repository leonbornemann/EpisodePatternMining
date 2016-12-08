package data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.events.CategoricalEvent;
import data.events.Change;
import util.IOService;
import util.Pair;

/***
 * Transforms a time series into a categorical event stream (and serializes the result). The class can operate in two different modes: normal, and threshold 
 * @author Leon Bornemann
 *
 */
public class TimeSeriesTransformator {

	private File inputDir;
	private File outputDir;
	private boolean thresholdIsSet = false;
	private double threshold;

	/***
	 * Initilizes an object that will do a normal transformation
	 * @param inputDir
	 * @param outputDir
	 */
	public TimeSeriesTransformator(String inputDir, String outputDir) {
		this.inputDir  = new File(inputDir);
		this.outputDir = new File(outputDir);
	}
	
	/***
	 * Initializes an object that will transform the time series, but only include events whose relative increase or decrease (compared to the previous value) is above the threshold
	 * @param inputDir
	 * @param outputDir
	 * @param threshold
	 */
	public TimeSeriesTransformator(String inputDir, String outputDir, double threshold) {
		this.inputDir  = new File(inputDir);
		this.outputDir = new File(outputDir);
		this.threshold = threshold;
		thresholdIsSet = true;
	}
	
	public void transform() {
		List<File> allFiles = Arrays.asList(inputDir.listFiles());
		for(File file : allFiles){
			if(file.isFile() && file.getName().endsWith(".csv")){
				String resultFilename = file.getName().substring(0, file.getName().length() - 4) + "_annotated.csv";
				File outFile = new File(outputDir.getAbsolutePath() + File.separator + resultFilename );
				if(!outFile.exists()){
					try{
						transform(file,outFile);
						System.out.println("Successfully transformed "+file.getName());
					} catch(Throwable e){
						System.out.println("error while transforming "+ file.getName());
						System.out.println("stack trace:");
						e.printStackTrace();
					}
				} else{
					System.out.println("skipping "+file.getName()+" because target already exists");
				}
			}
		}
	}
	
	private void transform(File source, File outFile) throws IOException {
		List<Pair<LocalDateTime,BigDecimal>> lowLevelEvents = IOService.readTimeSeriesData(source);
		List<CategoricalEvent> annotated;
		if(thresholdIsSet){
			annotated = transformToAnnotated(lowLevelEvents,source.getName().split("\\.")[0],threshold);
		}  else{
			annotated = transformToAnnotated(lowLevelEvents,source.getName().split("\\.")[0]);
		}
		if(!annotated.isEmpty()){
			CategoricalEvent.serialize(annotated, outFile);
		} else{
			System.out.println("warning: empty annotated file!");
		}
	}

	private List<CategoricalEvent> transformToAnnotated(List<Pair<LocalDateTime, BigDecimal>> lowLevelEvents,String companyID, double threshold) {
		List<CategoricalEvent> annotatedEvents = new ArrayList<>();
		BigDecimal prev = lowLevelEvents.get(0).getSecond();
		BigDecimal bdThreshold = new BigDecimal(threshold);
		for(int i=1;i<lowLevelEvents.size();i++){
			Pair<LocalDateTime,BigDecimal> now = lowLevelEvents.get(i);
			BigDecimal cur = now.getSecond();
			Change change;
			BigDecimal relativeDiff = cur.subtract(prev).divide(prev,100,RoundingMode.FLOOR);
			if(relativeDiff.abs().compareTo(bdThreshold) >0  ){
				if(relativeDiff.compareTo(BigDecimal.ZERO)>0){
					change = Change.UP;
					annotatedEvents.add(new CategoricalEvent(companyID, change, now.getFirst()));
				} else if(relativeDiff.compareTo(BigDecimal.ZERO) <0){
					change = Change.DOWN;
					annotatedEvents.add(new CategoricalEvent(companyID, change, now.getFirst()));
				} 
			}
			prev = cur;
		}
		return annotatedEvents;
	}

	private List<CategoricalEvent> transformToAnnotated(List<Pair<LocalDateTime,BigDecimal>> lowLevelEvents, String companyID) {
		List<CategoricalEvent> annotatedEvents = new ArrayList<>();
		BigDecimal referenceValue = lowLevelEvents.get(0).getSecond();
		for(int i=1;i<lowLevelEvents.size();i++){
			Pair<LocalDateTime,BigDecimal> now = lowLevelEvents.get(i);
			Change change;
			if(now.getSecond().compareTo(referenceValue) >0  ){
				change = Change.UP;
				referenceValue = now.getSecond();
				annotatedEvents.add(new CategoricalEvent(companyID, change, now.getFirst()));
			} else if(now.getSecond().compareTo(referenceValue) <0){
				change = Change.DOWN;
				referenceValue = now.getSecond();
				annotatedEvents.add(new CategoricalEvent(companyID, change, now.getFirst()));
			} 
		}
		return annotatedEvents;
	}

}
