package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.data.stream.AnnotatedEventStream;
import prediction.data.stream.InMemoryAnnotatedEventStream;
import prediction.data.stream.SlidableStreamWindow;
import prediction.data.stream.StreamWindowSlider;
import util.Pair;

public class StreamWindowSliderTest {

	//some event types for testing
	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	private static AnnotatedEventType E = new AnnotatedEventType("company", Change.EQUAL);
	private static AnnotatedEventType F = new AnnotatedEventType("names", Change.DOWN);
	private static AnnotatedEventType G = new AnnotatedEventType("names", Change.UP);
	
	@Test
	public void testSimple() throws IOException {
		AnnotatedEventStream stream = TestUtil.buildStream(1, 	
				A,A,A,A,A,
				B);
		StreamWindowSlider slider = new StreamWindowSlider(stream, 4);
		assertEquals(5,slider.getCurrentWindow().getEvents().size());
		assertEquals(A,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(A,slider.getCurrentWindow().getEvents().get(4).getEventType());
		slider.slideForward();
		assertEquals(5,slider.getCurrentWindow().getEvents().size());
		assertEquals(A,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(B,slider.getCurrentWindow().getEvents().get(4).getEventType());	
		assertFalse(slider.canSlide());
	}
	
	public void testMultipleEventsPerSlide() throws IOException{
		AnnotatedEventStream stream = TestUtil.buildStream(
				new Pair<>(A,1),new Pair<>(A,1),new Pair<>(A,1),
				new Pair<>(B,7),new Pair<>(B,7),
				new Pair<>(C,9),
				new Pair<>(D,12));
		StreamWindowSlider slider = new StreamWindowSlider(stream, 4);
		assertEquals(3,slider.getCurrentWindow().getEvents().size());
		assertEquals(A,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(A,slider.getCurrentWindow().getEvents().get(2).getEventType());
		slider.slideForward();
		assertEquals(2,slider.getCurrentWindow().getEvents().size());
		assertEquals(B,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(B,slider.getCurrentWindow().getEvents().get(1).getEventType());
		slider.slideForward();
		assertEquals(3,slider.getCurrentWindow().getEvents().size());
		assertEquals(B,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(C,slider.getCurrentWindow().getEvents().get(2).getEventType());
		slider.slideForward();
		assertEquals(2,slider.getCurrentWindow().getEvents().size());
		assertEquals(C,slider.getCurrentWindow().getEvents().get(0).getEventType());
		assertEquals(D,slider.getCurrentWindow().getEvents().get(1).getEventType());
	}
	
	

}
