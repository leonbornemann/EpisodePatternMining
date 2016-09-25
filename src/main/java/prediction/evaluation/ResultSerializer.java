package prediction.evaluation;

import java.math.BigDecimal;
import java.math.RoundingMode;

public abstract class ResultSerializer {

	protected String getAsRoundedString(double val, int roundTo) {
		//System.out.println(val);
		if(new Double(val).isNaN()){
			return "NaN";
		} else{
			return getAsRoundedString(new BigDecimal(val), roundTo);
		}
	}

	protected String getAsRoundedString(BigDecimal val, int roundTo) {
		return val.setScale(roundTo, RoundingMode.FLOOR).toString();
	}
}
