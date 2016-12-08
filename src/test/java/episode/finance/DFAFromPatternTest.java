package episode.finance;

import static org.junit.Assert.*;

import org.junit.Test;

import data.events.CategoricalEventType;
import data.events.Change;
import episode.pattern.SerialEpisodePattern;

public class DFAFromPatternTest {

	private static CategoricalEventType A = new CategoricalEventType("foo", Change.UP);
	private static CategoricalEventType B = new CategoricalEventType("bar", Change.EQUAL);
	private static CategoricalEventType C = new CategoricalEventType("mystic", Change.DOWN);
	private static CategoricalEventType D = new CategoricalEventType("magic", Change.UP);
	
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
