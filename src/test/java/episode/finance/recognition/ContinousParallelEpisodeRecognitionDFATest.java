package episode.finance.recognition;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import episode.finance.ParallelEpisodePattern;
import episode.finance.recognition.ContinousEpisodeRecognitionDFA;
import prediction.data.AnnotatedEvent;
import prediction.data.AnnotatedEventType;
import prediction.data.Change;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class ContinousParallelEpisodeRecognitionDFATest {

	//some event types for testing
		private static AnnotatedEventType A = new AnnotatedEventType("foo", Change.UP);
		private static AnnotatedEventType B = new AnnotatedEventType("bar", Change.UP);
		private static AnnotatedEventType C = new AnnotatedEventType("foo", Change.DOWN);
		private static AnnotatedEventType D = new AnnotatedEventType("foo", Change.EQUAL);
		private static AnnotatedEventType E = new AnnotatedEventType("bar", Change.DOWN);
		private static AnnotatedEventType F = new AnnotatedEventType("magic", Change.UP);
		
		//counter for event timestamp creation:
		private LocalDateTime counter;
		
		@Before
		public void init(){
			counter = LocalDateTime.parse("2001-01-01 10:00:00",StandardDateTimeFormatter.getStandardDateTimeFormatter());
		}
		
		@Test
		public void simplePermutationTest() {
			ParallelEpisodePattern pattern = new ParallelEpisodePattern(A,B,A,B);
			ContinousEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(B)));
			Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(B));
			assertNotNull(occurance);
			dfa = pattern.getContinousDFA();
			assertNull(dfa.processEvent(nextEvent(B)));
			assertNull(dfa.processEvent(nextEvent(B)));
			assertNull(dfa.processEvent(nextEvent(A)));
			occurance = dfa.processEvent(nextEvent(A));
			assertNotNull(occurance);
		}
		
		@Test
		public void noisyTest(){
			ParallelEpisodePattern pattern = new ParallelEpisodePattern(A,A,B,C);
			ContinousEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
			assertNull(dfa.processEvent(nextEvent(B)));
			assertNull(dfa.processEvent(nextEvent(D)));
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(D)));
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(D)));
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(D)));
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(D)));
			assertNull(dfa.processEvent(nextEvent(A)));
			Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(C));
			assertNotNull(occurance);
			assertTimeDifferenceInSeconds(occurance,11);
			occurance = dfa.processEvent(nextEvent(B));
			assertNotNull(occurance);
			assertTimeDifferenceInSeconds(occurance,4);
			assertEquals(2,dfa.getOccuranceCount());
			
		}
		
		private void assertTimeDifferenceInSeconds(Pair<LocalDateTime, LocalDateTime> occurance, int diff) {
			assertEquals(diff,ChronoUnit.SECONDS.between(occurance.getFirst(),occurance.getSecond()));
			assertTrue(occurance.getFirst().compareTo(occurance.getSecond()) <= 0);
		}
		
		//creates a new event one second after the last event that was created via this method
		private AnnotatedEvent nextEvent(AnnotatedEventType e) {
			counter = counter.plus(1,ChronoUnit.SECONDS);
			return new AnnotatedEvent(e.getCompanyID(), e.getChange(), counter);
		}

}
