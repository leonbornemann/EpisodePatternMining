package prediction.models;

import data.events.Change;
import data.stream.StreamWindow;

public interface PredictiveModel {

	Change predict(StreamWindow currentWindow);

}
