package prediction.models;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import data.events.CategoricalEventType;
import data.events.Change;
import data.stream.StreamWindow;
import util.IOService;
import util.NumericUtil;

/***
 * Implementation of the simple moving average regression model as a predictive model.
 * @author Leon Bornemann
 *
 */
public class SimpleMovingAverageForecastingModel implements PredictiveModel {

	
	private TreeMap<LocalDateTime, BigDecimal> timeSeries;
	private int d;

	public SimpleMovingAverageForecastingModel(CategoricalEventType toPredict, File timeSeriesStreamDir, int backWardsWindowSizeInSeconds) throws IOException {
		String cmpId = toPredict.getCompanyID();
		timeSeries = IOService.readTimeSeries(cmpId, timeSeriesStreamDir);
		this.d = backWardsWindowSizeInSeconds;
	}

	@Override
	public Change predict(StreamWindow currentWindow) {
		LocalDateTime endInclusive = currentWindow.getWindowBorders().getSecond();
		LocalDateTime beginInclusive = endInclusive.minusSeconds(d);
		LocalDateTime cur = timeSeries.ceilingKey(beginInclusive);
		if(cur==null){
			return Change.EQUAL;
		}
		List<BigDecimal> values = new ArrayList<>();
		while(cur.compareTo(endInclusive)<= 0){
			values.add(timeSeries.get(cur));
			LocalDateTime newKey = timeSeries.higherKey(cur);
			if(newKey==null){
				break;
			}
			cur = newKey;
		}
		if(values.size()==0){
			System.out.println("0 values to average found, returning equal");
			return Change.EQUAL;
		}
		BigDecimal avg = NumericUtil.mean(values, 100);
		BigDecimal curTsValue = timeSeries.floorEntry(endInclusive).getValue();
		if(avg.compareTo(curTsValue)<0){
			return Change.DOWN;
		} else if(avg.compareTo(curTsValue)==0){
			return Change.EQUAL;
		} else{
			return Change.UP;
		}
	}

}
