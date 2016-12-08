package util;

import java.time.format.DateTimeFormatter;

public class StandardDateTimeFormatter {

	public static DateTimeFormatter getStandardDateTimeFormatter(){
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	}

	public static DateTimeFormatter getStandardDateFormatter() {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd");
	}
}
