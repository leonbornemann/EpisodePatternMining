package yahoo.stock_data.download;

import java.time.format.DateTimeFormatter;

public class StandardDateTimeFormatter {

	public static DateTimeFormatter getStandardFormatter(){
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	}
}
