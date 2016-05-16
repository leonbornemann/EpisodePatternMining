package reallife_data.finance.yahoo.stock.download;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import reallife_data.finance.yahoo.stock.util.IOService;
import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;

public class SimpleCrawler {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String errorLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\errorLog.txt";
	
	public static void main(String[] args) throws IOException {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		List<String> allCompanyCodes = new ArrayList<>(IOService.getAllCompanyCodes());
		Collections.sort(allCompanyCodes);
		String dateToday = LocalDate.now().format(StandardDateTimeFormatter.getStandardDateFormatter());
		service.scheduleAtFixedRate(new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation + "NASDAQ_" +dateToday + ".csv",errorLogLocation), 0, 15, TimeUnit.SECONDS);
	}
}
