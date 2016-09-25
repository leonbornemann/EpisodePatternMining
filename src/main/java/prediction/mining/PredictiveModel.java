package prediction.mining;

import data.Change;
import data.stream.StreamWindow;

public interface PredictiveModel {

	Change predict(StreamWindow currentWindow);

}
