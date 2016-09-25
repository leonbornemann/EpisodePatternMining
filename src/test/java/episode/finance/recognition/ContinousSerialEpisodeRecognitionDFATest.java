package episode.finance.recognition;

import static org.junit.Assert.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;

import org.junit.Before;
import org.junit.Test;

import data.AnnotatedEvent;
import data.AnnotatedEventType;
import data.Change;
import episode.finance.SerialEpisodePattern;
import episode.finance.recognition.ContinousSerialEpisodeRecognitionDFA;
import prediction.util.StandardDateTimeFormatter;
import util.Pair;

public class ContinousSerialEpisodeRecognitionDFATest {

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
	public void noisySize1Test() {
		SerialEpisodePattern pattern = new SerialEpisodePattern(A);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(D)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(A));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,0);
		//second occurance right after:
		occurance = dfa.processEvent(nextEvent(A));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,0);
		assertNull(dfa.processEvent(nextEvent(D)));
		assertNull(dfa.processEvent(nextEvent(F)));
		//final occurance after noise:
		occurance = dfa.processEvent(nextEvent(A));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,0);
		assertEquals(3, dfa.getOccuranceCount());
	}
	
	private void assertTimeDifferenceInSeconds(Pair<LocalDateTime, LocalDateTime> occurance, int diff) {
		assertEquals(diff,ChronoUnit.SECONDS.between(occurance.getFirst(),occurance.getSecond()));
		assertTrue(occurance.getFirst().compareTo(occurance.getSecond()) <= 0);
	}

	@Test
	public void basicSize5Test() {
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,A,D);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(A)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(D));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,4);
		assertEquals(1, dfa.getOccuranceCount());
	}
	
	@Test
	public void noisySize5Test() {
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,A,D);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		//noise
		assertNull(dfa.processEvent(nextEvent(F)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(E)));
		assertNull(dfa.processEvent(nextEvent(B)));
		//event
		assertNull(dfa.processEvent(nextEvent(A)));
		//noise
		assertNull(dfa.processEvent(nextEvent(F)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(E)));
		//event
		assertNull(dfa.processEvent(nextEvent(B)));
		//noise
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(E)));
		//event
		assertNull(dfa.processEvent(nextEvent(C)));
		//noise
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(E)));
		//event
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(F)));
		assertNull(dfa.processEvent(nextEvent(F)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(D));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,15);
		assertEquals(1, dfa.getOccuranceCount());
	}
	
	@Test
	public void latestOccuranceTest(){
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,A,D);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(A)));
		//noise
		assertNull(dfa.processEvent(nextEvent(F)));
		assertNull(dfa.processEvent(nextEvent(F)));
		//new start
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(A)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(D));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,4);
		System.out.println(occurance);
		assertEquals(1, dfa.getOccuranceCount());
	}
	
	@Test
	public void multiOccurance(){
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		for(int i=0;i<10;i++){
			assertNull(dfa.processEvent(nextEvent(A)));
			assertNull(dfa.processEvent(nextEvent(B)));
			Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(C));
			assertNotNull(occurance);
			assertTimeDifferenceInSeconds(occurance,2);
			assertEquals(i+1, dfa.getOccuranceCount());
		}
	}
	
	@Test
	public void singleEventPartOfMultipleEpisodes(){
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,A,B);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		//the next two events will be part of two episode occurances!
		assertNull(dfa.processEvent(nextEvent(A)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(B));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,4);
		//overlapping:
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(A)));
		occurance = dfa.processEvent(nextEvent(B));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,4);
		assertEquals(2,dfa.getOccuranceCount());
	}
	
	@Test
	public void interleavingEpisodes(){
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,D,E,F);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(D)));
		assertNull(dfa.processEvent(nextEvent(C)));
		assertNull(dfa.processEvent(nextEvent(E)));
		assertNull(dfa.processEvent(nextEvent(D)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(F));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,9);
		//finish the interleaved episode:
		assertNull(dfa.processEvent(nextEvent(E)));
		occurance = dfa.processEvent(nextEvent(F));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,9);
		assertEquals(2,dfa.getOccuranceCount());
	}
	
	@Test
	public void overlapping(){
		SerialEpisodePattern pattern = new SerialEpisodePattern(A,B,C,D);
		ContinousSerialEpisodeRecognitionDFA dfa = pattern.getContinousDFA();
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(A)));
		assertNull(dfa.processEvent(nextEvent(B)));
		assertNull(dfa.processEvent(nextEvent(C)));
		Pair<LocalDateTime, LocalDateTime> occurance = dfa.processEvent(nextEvent(D));
		assertNotNull(occurance);
		assertTimeDifferenceInSeconds(occurance,3);
	}


	//creates a new event one second after the last event that was created via this method
	private AnnotatedEvent nextEvent(AnnotatedEventType e) {
		counter = counter.plus(1,ChronoUnit.SECONDS);
		return new AnnotatedEvent(e.getCompanyID(), e.getChange(), counter);
	}

}
