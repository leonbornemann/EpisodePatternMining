package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import data.AnnotatedEvent;
import data.AnnotatedEventType;
import data.Change;
import data.stream.MultiFileAnnotatedEventStream;
import data.stream.StreamWindow;

public class MultiFileAnnotatedEventStreamTest {

	private static List<File> streamFile = Arrays.asList(new File("resources/testdata/AnnotatedEventStream/streamFile1.csv"));
	private static int windowDuration = 15;

	@Test
	public void testBackwardsWindow() throws IOException {
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(streamFile , windowDuration );
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
		assertEquals(new AnnotatedEventType("E", Change.DOWN),win.getEvents().get(0).getEventType());
	}
	
	@Test
	public void testPeek() throws IOException{
		MultiFileAnnotatedEventStream stream = new MultiFileAnnotatedEventStream(streamFile , windowDuration );
		while(stream.hasNext()){
			AnnotatedEvent peekResult = stream.peek();
			stream.peek(); //call it again to assert that it is an idempotent operation
			AnnotatedEvent nextResult = stream.next();
			assertEquals(peekResult.getEventType(),nextResult.getEventType());
			assertEquals(peekResult.getTimestamp(),nextResult.getTimestamp());
		}
	}

}
