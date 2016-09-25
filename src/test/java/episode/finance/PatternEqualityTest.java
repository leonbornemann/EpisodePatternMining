package episode.finance;

import static org.junit.Assert.*;

import org.junit.Test;

import data.AnnotatedEventType;
import data.Change;

public class PatternEqualityTest {


	private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
	private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.EQUAL);
	private static AnnotatedEventType C = new AnnotatedEventType("mystic", Change.DOWN);
	private static AnnotatedEventType D = new AnnotatedEventType("magic", Change.UP);
	
	@Test
	public void serial() {
		SerialEpisodePattern pattern1 = new SerialEpisodePattern(A,A,B,C);
		SerialEpisodePattern pattern2 = new SerialEpisodePattern(A,A,B,C);
		SerialEpisodePattern pattern3 = new SerialEpisodePattern(A,A,B,C,D);
		SerialEpisodePattern pattern4 = new SerialEpisodePattern(A,A,C,B);
		SerialEpisodePattern pattern5 = new SerialEpisodePattern(A,A,B);
		assertEquals(pattern1, pattern2);
		assertEquals(pattern1.hashCode(),pattern2.hashCode());
		assertFalse(pattern1.equals(pattern3));
		assertFalse(pattern1.equals(pattern4));
		assertFalse(pattern1.equals(pattern5));
	}
	
	@Test
	public void parallel() {
		ParallelEpisodePattern pattern1 = new ParallelEpisodePattern(A,A,B,C);
		ParallelEpisodePattern pattern2 = new ParallelEpisodePattern(A,A,B,C);
		ParallelEpisodePattern pattern3 = new ParallelEpisodePattern(A,A,C,B);
		ParallelEpisodePattern pattern4 = new ParallelEpisodePattern(A,A,B,C,D);
		ParallelEpisodePattern pattern5 = new ParallelEpisodePattern(A,A,B);
		ParallelEpisodePattern pattern6 = new ParallelEpisodePattern(A,A,A,B,C);
		assertEquals(pattern1, pattern2);
		assertEquals(pattern1.hashCode(),pattern2.hashCode());
		assertEquals(pattern1, pattern3);
		assertEquals(pattern1.hashCode(),pattern3.hashCode());
		assertFalse(pattern1.equals(pattern4));
		assertFalse(pattern1.equals(pattern5));
		assertFalse(pattern1.equals(pattern6));
	}
	
	@Test
	public void parallelAndSerial() {
		ParallelEpisodePattern pattern1 = new ParallelEpisodePattern(A,A);
		SerialEpisodePattern pattern2 = new SerialEpisodePattern(A,A);
		assertFalse(pattern1.equals(pattern2));
	}
	
	@Test
	public void size1() {
		ParallelEpisodePattern pattern1 = new ParallelEpisodePattern(A);
		ParallelEpisodePattern pattern2 = new ParallelEpisodePattern(B);
		SerialEpisodePattern pattern3 = new SerialEpisodePattern(A);
		SerialEpisodePattern pattern4 = new SerialEpisodePattern(B);
		assertEquals(pattern1, pattern3);
		assertEquals(pattern1.hashCode(),pattern3.hashCode());
		assertEquals(pattern2, pattern4);
		assertEquals(pattern2.hashCode(),pattern4.hashCode());
		assertFalse(pattern1.equals(pattern2));
		assertFalse(pattern1.equals(pattern4));
		assertFalse(pattern2.equals(pattern3));
		assertFalse(pattern3.equals(pattern4));
	}
	

}
