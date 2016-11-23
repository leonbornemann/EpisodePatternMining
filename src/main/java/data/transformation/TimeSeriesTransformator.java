package data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import data.AnnotatedEvent;
import data.Change;
import prediction.util.IOService;
import util.Pair;

public class TimeSeriesTransformator {

	private File inputDir;
	private File outputDir;

	public TimeSeriesTransformator(String inputDir, String outputDir) {
		this.inputDir  = new File(inputDir);
		this.outputDir = new File(outputDir);
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
		List<AnnotatedEvent> annotated = transformToAnnotated(lowLevelEvents,source.getName().split("\\.")[0]);
		if(!annotated.isEmpty()){
			AnnotatedEvent.serialize(annotated, outFile);
		} else{
			System.out.println("warning: empty annotated file!");
		}
	}

	private List<AnnotatedEvent> transformToAnnotated(List<Pair<LocalDateTime,BigDecimal>> lowLevelEvents, String companyID) {
		List<AnnotatedEvent> annotatedEvents = new ArrayList<>();
		BigDecimal referenceValue = lowLevelEvents.get(0).getSecond();
		for(int i=1;i<lowLevelEvents.size();i++){
			Pair<LocalDateTime,BigDecimal> now = lowLevelEvents.get(i);
			Change change;
			if(now.getSecond().compareTo(referenceValue) >0  ){
				change = Change.UP;
				referenceValue = now.getSecond();
				annotatedEvents.add(new AnnotatedEvent(companyID, change, now.getFirst()));
			} else if(now.getSecond().compareTo(referenceValue) <0){
				change = Change.DOWN;
				referenceValue = now.getSecond();
				annotatedEvents.add(new AnnotatedEvent(companyID, change, now.getFirst()));
			} 
		}
		return annotatedEvents;
	}

}
