package prediction.mining;

import java.util.Random;

import data.Change;
import data.stream.StreamWindow;

public class RandomGuessingModel implements PredictiveModel {

	private Random random;

	public RandomGuessingModel(Random random) {
		this.random = random;
	}

	@Override
	public Change predict(StreamWindow currentWindow) {
		int rand = random.nextInt(3);
		if(rand == 0){
			return Change.UP;
		} else if(rand == 1){
			return Change.DOWN;
		} else{
			return Change.EQUAL;
		}
	}

}
