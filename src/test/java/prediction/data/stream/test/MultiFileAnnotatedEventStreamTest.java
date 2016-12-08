package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.MultiFileCategoricalEventStream;
import data.stream.StreamWindow;

public class MultiFileAnnotatedEventStreamTest {

	private static List<File> streamFile = Arrays.asList(new File("resources/testdata/AnnotatedEventStream/streamFile1.csv"));
	private static int windowDuration = 15;

	@Test
	public void testBackwardsWindow() throws IOException {
		MultiFileCategoricalEventStream stream = new MultiFileCategoricalEventStream(streamFile , windowDuration );
		stream.next();
		stream.next();
		stream.next();
		stream.next();
		stream.next();
		stream.next();
		StreamWindow win = stream.getBackwardsWindow(windowDuration);
		assertEquals(5,win.getEvents().size());
		stream.next();
		win = stream.getBackwardsWindow(windowDuration);
		assertEquals(1,win.getEvents().size());
		assertEquals(new CategoricalEventType("E", Change.DOWN),win.getEvents().get(0).getEventType());
	}
	
	@Test
	public void testPeek() throws IOException{
		MultiFileCategoricalEventStream stream = new MultiFileCategoricalEventStream(streamFile , windowDuration );
		while(stream.hasNext()){
			CategoricalEvent peekResult = stream.peek();
			stream.peek(); //call it again to assert that it is an idempotent operation
			CategoricalEvent nextResult = stream.next();
			assertEquals(peekResult.getEventType(),nextResult.getEventType());
			assertEquals(peekResult.getTimestamp(),nextResult.getTimestamp());
		}
	}

}
