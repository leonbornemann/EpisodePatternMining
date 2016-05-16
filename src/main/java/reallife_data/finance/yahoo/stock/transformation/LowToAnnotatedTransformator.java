package reallife_data.finance.yahoo.stock.transformation;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import reallife_data.finance.yahoo.stock.data.AnnotatedEvent;
import reallife_data.finance.yahoo.stock.data.Change;
import reallife_data.finance.yahoo.stock.data.LowLevelEvent;

public class LowToAnnotatedTransformator {

	
	private File outputDir;
	private File inputDir;

	public LowToAnnotatedTransformator(File inputDir, File outputDir){
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}
	
	public void transform() throws IOException{
		List<File> allFiles = Arrays.asList(inputDir.listFiles());
		for(File file : allFiles){
			if(file.isFile() && file.getName().endsWith(".csv"))
			transform(file);
		}
	}

	private void transform(File source) throws IOException {
		String resultFilename = source.getName().substring(0, source.getName().length() - 4) + "_annotated.csv";
		File outFile = new File(outputDir.getAbsolutePath() + File.separator + resultFilename );
		List<LowLevelEvent> lowLevelEvents = LowLevelEvent.readAll(source);
		Map<String,List<LowLevelEvent>> byCompany = lowLevelEvents.stream().collect(Collectors.groupingBy(e -> e.getCompanyId()));
		Map<String,List<AnnotatedEvent>> annotatedByCompany = byCompany.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> toAnnotated(e.getValue())));
		List<AnnotatedEvent> allAnnotated = new ArrayList<>();
		annotatedByCompany.values().forEach(e -> allAnnotated.addAll(e));
		AnnotatedEvent.serialize(allAnnotated,outFile);
	}

	private List<AnnotatedEvent> toAnnotated(List<LowLevelEvent> lowLevelEvents) {
		double epsilon = 0.00001;
		List<AnnotatedEvent> annotatedEvents = new ArrayList<>();
		for(int i=1;i<lowLevelEvents.size();i++){
			LowLevelEvent before = lowLevelEvents.get(i-1);
			LowLevelEvent now = lowLevelEvents.get(i);
			Change change;
			if(equalWithinTolerance(before.getValue(), now.getValue(), epsilon)){
				change = Change.EQUAL;
			} else if(before.getValue() < now.getValue()){
				change = Change.UP;
			} else{
				change = Change.DOWN;
			}
			annotatedEvents.add(new AnnotatedEvent(before.getCompanyId(), change, now.getTimestamp()));
		}
		return annotatedEvents;
	}
	
	public static boolean equalWithinTolerance(double a, double b, double eps){
	    return Math.abs(a-b)<eps;
	}

}
