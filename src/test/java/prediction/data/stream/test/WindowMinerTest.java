package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.FixedStreamWindow;
import data.stream.MultiFileCategoricalEventStream;
import prediction.training.WindowMiner;

public class WindowMinerTest {


	private static List<File> streamFile = Arrays.asList(new File("resources/testdata/AnnotatedEventStream/streamFile1.csv"));
	private static int windowDuration = 15;
	private static int numWindows = 5;
	
	@Test
	public void test() throws IOException {
		MultiFileCategoricalEventStream stream = new MultiFileCategoricalEventStream(streamFile , windowDuration*2 );
		WindowMiner miner = new WindowMiner(stream, new CategoricalEventType("A",Change.UP), numWindows, windowDuration);
		assertEquals(2,miner.getPredictiveWindows().size());
		assertBorders(new CategoricalEventType("B",Change.DOWN),new CategoricalEventType("E",Change.DOWN),miner.getPredictiveWindows().get(0));
		assertBorders(new CategoricalEventType("B",Change.DOWN),new CategoricalEventType("C",Change.DOWN),miner.getPredictiveWindows().get(1));
		assertEquals(2,miner.getInversePredictiveWindows().size());
		assertEquals(1,miner.getInversePredictiveWindows().get(0).getEvents().size());
		assertBorders(new CategoricalEventType("B",Change.DOWN),new CategoricalEventType("F",Change.UP),miner.getInversePredictiveWindows().get(1));
		System.out.println(miner.getNeutralWindows().size());

	}

	private void assertBorders(CategoricalEventType first, CategoricalEventType last,
			FixedStreamWindow fixedStreamWindow) {
		assertEquals(first,fixedStreamWindow.getEvents().get(0).getEventType());
		assertEquals(last,fixedStreamWindow.getEvents().get(fixedStreamWindow.getEvents().size()-1).getEventType());
		
	}

}
