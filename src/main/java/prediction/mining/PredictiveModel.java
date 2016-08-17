package prediction.mining;

import prediction.data.Change;
import prediction.data.stream.StreamWindow;

public interface PredictiveModel {

	Change predict(StreamWindow currentWindow);

}
