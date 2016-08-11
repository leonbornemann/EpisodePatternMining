package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.FixedStreamWindow;
import prediction.data.stream.MultiFileAnnotatedEventStream;
import prediction.mining.WindowMiner;

public class WindowMinerTest {


	private static List<File> streamFile = Arrays.asList(new File("resources/testdata/AnnotatedEventStream/streamFile1.csv"));
	private static int windowDuration = 15;
	private static int numWindows = 5;
	
	@Test
	public void test() throws IOException {
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(streamFile , windowDuration*2 );
		WindowMiner miner = new WindowMiner(stream, new AnnotatedEventType("A",Change.UP), numWindows, windowDuration);
		assertEquals(2,miner.getPredictiveWindows().size());
		assertBorders(new AnnotatedEventType("A",Change.DOWN),new AnnotatedEventType("E",Change.DOWN),miner.getPredictiveWindows().get(0));
		assertBorders(new AnnotatedEventType("B",Change.DOWN),new AnnotatedEventType("C",Change.DOWN),miner.getPredictiveWindows().get(1));
		assertEquals(2,miner.getInversePredictiveWindows().size());
		assertEquals(0,miner.getInversePredictiveWindows().get(0).getEvents().size());
		assertBorders(new AnnotatedEventType("B",Change.DOWN),new AnnotatedEventType("F",Change.UP),miner.getInversePredictiveWindows().get(1));
		System.out.println(miner.getNeutralWindows().size());

	}

	private void assertBorders(AnnotatedEventType first, AnnotatedEventType last,
			FixedStreamWindow fixedStreamWindow) {
		assertEquals(first,fixedStreamWindow.getEvents().get(0).getEventType());
		assertEquals(last,fixedStreamWindow.getEvents().get(fixedStreamWindow.getEvents().size()-1).getEventType());
		
	}

}
