package prediction.data.stream.test;

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import data.events.CategoricalEvent;
import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.CategoricalEventStream;
import data.stream.InMemoryCategoricalEventStream;
import data.stream.SlidableStreamWindow;
import data.stream.StreamWindowSlider;
import util.Pair;

public class StreamWindowSliderTest {

	//some event types for testing
	private static CategoricalEventType A = new CategoricalEventType("foo", Change.UP);
	private static CategoricalEventType B = new CategoricalEventType("bar", Change.EQUAL);
	private static CategoricalEventType C = new CategoricalEventType("mystic", Change.DOWN);
	private static CategoricalEventType D = new CategoricalEventType("magic", Change.UP);
	private static CategoricalEventType E = new CategoricalEventType("company", Change.EQUAL);
	private static CategoricalEventType F = new CategoricalEventType("names", Change.DOWN);
	private static CategoricalEventType G = new CategoricalEventType("names", Change.UP);
	
	@Test
	public void testSimple() throws IOException {
		CategoricalEventStream stream = TestUtil.buildStream(1, 	
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
		CategoricalEventStream stream = TestUtil.buildStream(
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
