package yahoo.stock_data.download;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SimpleCrawler {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\NASDAQ_Stream_2.csv";
	private static String errorLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\errorLog.txt";
	
	public static void main(String[] args) throws IOException {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		List<String> allCompanyCodes = new ArrayList<>(IOService.getAllCompanyCodes());
		Collections.sort(allCompanyCodes);
		service.scheduleAtFixedRate(new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation,errorLogLocation), 0, 15, TimeUnit.SECONDS);
	}
}
