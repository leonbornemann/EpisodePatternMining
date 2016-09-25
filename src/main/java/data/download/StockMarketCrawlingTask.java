package data.download;

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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;

public class StockMarketCrawlingTask implements Runnable, org.quartz.Job{

	private List<String> allCompanyCodes;
	private String dataBaseLocation;
	private String errorLogLocation;
	private String outLogLocation;

	public StockMarketCrawlingTask(List<String> allCompanyCodes, String dataBaseLocation, String errorLogLocation, String outLogLocation) {
		this.allCompanyCodes = allCompanyCodes;
		this.dataBaseLocation = dataBaseLocation;
		this.errorLogLocation = errorLogLocation;
		this.outLogLocation = outLogLocation;
	}
	
	public StockMarketCrawlingTask() throws IOException{
		errorLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\errorLog.txt";
		outLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\log.txt";
		allCompanyCodes = new ArrayList<>(IOService.getAllCompanyCodes());
		Collections.sort(allCompanyCodes);
		String dateToday = LocalDate.now().format(StandardDateTimeFormatter.getStandardDateFormatter());
		dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\NASDAQ_" +dateToday + ".csv";
	}

	@Override
	public void run() {
		try{
			IOService.writeLogEntry(outLogLocation, "starting execution");
			LocalDateTime timestamp = LocalDateTime.now();
			try {
				if(!(new File(dataBaseLocation).exists())){
					PrintWriter out = new PrintWriter(new FileWriter(new File(dataBaseLocation),true));
					out.println("company,value,timestamp");
					out.close();
				}
				requestMultipleCompanies(allCompanyCodes,timestamp);
			} catch (Throwable e) {
				IOService.writeLogEntry(outLogLocation, "encountered exception or error");
				IOService.writeErrorLogEntry(errorLogLocation,e,timestamp);
			}
			IOService.writeLogEntry(outLogLocation, "Done with data extraction of timestamp " + timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter()));
		} catch(Throwable e){
			System.out.println("Caught outer Exception");
		}
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
			out.println(line + "," + timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter()));
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

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		run();		
	}
}
