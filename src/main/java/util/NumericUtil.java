package util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

public class NumericUtil {

	public static BigDecimal mean(List<BigDecimal> vals, int scale) {
		BigDecimal result = BigDecimal.ZERO;
		for(BigDecimal d : vals){
			result = result.add(d);
		}
		return result.divide(new BigDecimal(vals.size()), scale, RoundingMode.FLOOR);
	}

}
