package prediction.data.transformation;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import prediction.data.AnnotatedEvent;
import prediction.data.Change;
import prediction.data.LowLevelEvent;

public class LowToAnnotatedTransformator {

	
	private File outputDir;
	private File inputDir;
	private File illegalFormatDir;
	private BigDecimal relativeDelta;
	private boolean aggregate = false;

	public LowToAnnotatedTransformator(File inputDir, File outputDir,File illegalFormatDir, BigDecimal relativeDelta){
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.illegalFormatDir = illegalFormatDir;
		this.relativeDelta = relativeDelta;
		this.aggregate  = true;
	}
	
	public LowToAnnotatedTransformator(File inputDir, File outputDir,File illegalFormatDir){
		this.inputDir = inputDir;
		this.outputDir = outputDir;
		this.illegalFormatDir = illegalFormatDir;
	}
	
	public void transform() throws IOException{
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
						File garbageDst = new File(illegalFormatDir.getAbsolutePath() + File.separator + file.getName());
						Files.move(file.toPath(), garbageDst.toPath());
						System.out.println("Moved File: " + file.getName() +" to garbage folder");
					}
				} else{
					System.out.println("skipping "+file.getName()+" because target already exists");
				}
			}
		}
	}

	private void transform(File source, File outFile) throws IOException {
		List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(source);
		Map<String,List<LowLevelEvent>> byCompany = lowLevelEvents.stream().collect(Collectors.groupingBy(e -> e.getCompanyId()));
		Map<String,List<AnnotatedEvent>> annotatedByCompany = byCompany.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> toAnnotated(e.getValue())));
		List<AnnotatedEvent> allAnnotated = new ArrayList<>();
		annotatedByCompany.values().forEach(e -> allAnnotated.addAll(e));
		AnnotatedEvent.serialize(allAnnotated,outFile);
	}

	private List<AnnotatedEvent> toAnnotated(List<LowLevelEvent> lowLevelEvents) {
		if(lowLevelEvents.isEmpty()){
			return new ArrayList<>();
		}
		if(aggregate){
			return aggregateToAnnotated(lowLevelEvents);
		} else{
			return transformToAnnotated(lowLevelEvents);
		}
	}

	//TODO: refactor!
	private List<AnnotatedEvent> transformToAnnotated(List<LowLevelEvent> lowLevelEvents) {
		List<AnnotatedEvent> annotatedEvents = new ArrayList<>();
		BigDecimal referenceValue = lowLevelEvents.get(0).getValue();
		for(int i=1;i<lowLevelEvents.size();i++){
			LowLevelEvent now = lowLevelEvents.get(i);
			Change change;
			if(now.getValue().compareTo(referenceValue) >0  ){
				change = Change.UP;
				referenceValue = now.getValue();
				annotatedEvents.add(new AnnotatedEvent(now.getCompanyId(), change, now.getTimestamp()));
			} else if(now.getValue().compareTo(referenceValue) <0){
				change = Change.DOWN;
				referenceValue = now.getValue();
				annotatedEvents.add(new AnnotatedEvent(now.getCompanyId(), change, now.getTimestamp()));
			} 
		}
		return annotatedEvents;
	}

	private List<AnnotatedEvent> aggregateToAnnotated(List<LowLevelEvent> lowLevelEvents) {
		List<AnnotatedEvent> annotatedEvents = new ArrayList<>();
		BigDecimal referenceValue = lowLevelEvents.get(0).getValue();
		for(int i=1;i<lowLevelEvents.size();i++){
			LowLevelEvent now = lowLevelEvents.get(i);
			Change change;
			BigDecimal positiveBorderValue = referenceValue.add(referenceValue.multiply(relativeDelta));
			BigDecimal negativeBorderValue = referenceValue.subtract(referenceValue.multiply(relativeDelta));
			if(now.getValue().compareTo(positiveBorderValue) >= 0){
				change = Change.UP;
				referenceValue = now.getValue();
				annotatedEvents.add(new AnnotatedEvent(now.getCompanyId(), change, now.getTimestamp()));
			} else if(now.getValue().compareTo(negativeBorderValue) <= 0){
				change = Change.DOWN;
				referenceValue = now.getValue();
				annotatedEvents.add(new AnnotatedEvent(now.getCompanyId(), change, now.getTimestamp()));
			} else{
				//Lets try ignoring equality at some point and see where we get
				//change = Change.EQUAL; //TODO: should this be an event?
				//annotatedEvents.add(new AnnotatedEvent(now.getCompanyId(), change, now.getTimestamp()));
			}
		}
		return annotatedEvents;
	}
	
	public static boolean equalWithinTolerance(double a, double b, double eps){
	    return Math.abs(a-b)<eps;
	}

}
