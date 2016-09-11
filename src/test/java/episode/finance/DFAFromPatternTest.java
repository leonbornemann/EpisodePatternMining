package episode.finance;

import static org.junit.Assert.*;

import org.junit.Test;

import prediction.data.AnnotatedEventType;
import prediction.data.Change;

public class DFAFromPatternTest {

	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	
	@Test
	public void testSize1() {
		SerialEpisodePattern pattern = new SerialEpisodePattern(A);
		pattern.getSimpleDFA();
	}
	
	@Test
	public void testSize2() {
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B);
		pattern.getSimpleDFA();
	}
	

}
