package prediction.mining.test;

import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Test;

import data.events.Change;
import prediction.training.FeatureSelection;

public class FeatureSelectionTest {

	private static double tolerance = 0.000001;
	
	@Test
	public void test1() {
		//the target values come from the R implementation of information gain, which we use as a test oracle
		double expected = 0.6666667;
		double result = FeatureSelection.calcInfoGain(
				Arrays.asList(Change.UP,Change.UP,Change.DOWN,Change.DOWN,Change.EQUAL,Change.EQUAL),
				Arrays.asList(true,true,false,false,true,false));
		assertEquals(expected,result,tolerance);
	}
	
	@Test
	public void test2() {
		//the target values come from the R implementation of information gain, which we use as a test oracle
		double expected = 1.0;
		double result = FeatureSelection.calcInfoGain(
				Arrays.asList(Change.UP,Change.UP,Change.DOWN,Change.DOWN),
				Arrays.asList(true,true,false,false));
		assertEquals(expected,result,tolerance);
	}
	
	@Test
	public void test3() {
		//the target values come from the R implementation of information gain, which we use as a test oracle
		double expected = 0.003114759;
		double result = FeatureSelection.calcInfoGain(
				Arrays.asList(
						Change.UP,Change.UP,Change.DOWN,Change.DOWN,Change.EQUAL,
						Change.EQUAL,Change.UP,Change.DOWN,Change.EQUAL,Change.DOWN,
						Change.EQUAL,Change.UP,Change.DOWN,Change.EQUAL,Change.DOWN,
						Change.DOWN,Change.EQUAL,Change.EQUAL,Change.UP,Change.UP
						),
				Arrays.asList(
						true,true,false,false,true,
						false,false,false,false,true,
						true,false,true,false,true,
						false,false,true,false,true));
		assertEquals(expected,result,tolerance);
	}

}
