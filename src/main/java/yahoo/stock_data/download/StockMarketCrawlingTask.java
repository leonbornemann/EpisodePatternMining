package yahoo.stock_data.download;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class StockMarketCrawlingTask implements Runnable {

	private List<String> allCompanyCodes;
	private String dataBaseLocation;
	private String errorLogLocation;

	public StockMarketCrawlingTask(List<String> allCompanyCodes, String dataBaseLocation, String errorLogLocation) {
		this.allCompanyCodes = allCompanyCodes;
		this.dataBaseLocation = dataBaseLocation;
		this.errorLogLocation = errorLogLocation;
	}

	@Override
	public void run() {
		LocalDateTime timestamp = LocalDateTime.now();
		try {
			requestMultipleCompanies(allCompanyCodes,timestamp);
		} catch (IOException e) {
			IOService.writeErrorLogEntry(errorLogLocation,e,timestamp);
		}
		System.out.println("Done with data extraction of timestamp " + timestamp.format(StandardDateTimeFormatter.getStandardFormatter()));
	}

	private void requestMultipleCompanies(List<String> allCompanyCodes, LocalDateTime timestamp) throws MalformedURLException, IOException, ProtocolException {
		List<String> urlsToRead = buildURLs(allCompanyCodes);
		PrintWriter out = new PrintWriter(new FileWriter(new File(dataBaseLocation),true));
		for(String urlToRead : urlsToRead){
			writeGetResult(urlToRead,timestamp,out);
		}
		out.close();
	}

	private void writeGetResult(String urlToRead, LocalDateTime timestamp, PrintWriter out) throws MalformedURLException, IOException, ProtocolException {
		URL url = new URL(urlToRead );
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		conn.setRequestMethod("GET");
		BufferedReader rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		String line;
		while ((line = rd.readLine()) != null) {
			out.println(line + "," + timestamp.format(StandardDateTimeFormatter.getStandardFormatter()));
		}
		rd.close();
	}

	private List<String> buildURLs(List<String> allCompanyCodes) {
		int numCompaniesPerRequest = 1000;
		List<String> urls = new ArrayList<>();
		int start = 0;
		while(true){
			if(start>=allCompanyCodes.size()){
				break;
			}
			List<String> curSubList = allCompanyCodes.subList(start, Math.min(allCompanyCodes.size(), start+numCompaniesPerRequest));
			String param = curSubList.stream().reduce( (a,b) -> a + "+" + b).get();
			urls.add("http://download.finance.yahoo.com/d/quotes.csv?s="+ param +"&f=sa&e=.csv");
			start +=numCompaniesPerRequest;
		}
		return urls;
	}
}
