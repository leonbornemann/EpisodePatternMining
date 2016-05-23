package reallife_data.finance.yahoo.stock.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

public class IOService {

	
	public static Set<String> getAllCompanyCodes() throws IOException {
		//String companyListPath = "resources" + File.separator + "stock_data" + File.separator + "companyInfo" + File.separator + "companyList.csv";
		String companyListPath = "C:\\Users\\Leon Bornemann\\git\\EpisodePatternMining\\resources\\stock_data\\companyInfo\\companylist.csv";
		BufferedReader br = new BufferedReader(new FileReader(new File(companyListPath)));
		br.readLine();
		String line = br.readLine();
		Set<String> allCompanyCodes = new HashSet<>();
		while(line!=null){
			String curCompanyCode = line.split(",")[0].replaceAll("\"", "").trim();
			assert(!allCompanyCodes.contains(curCompanyCode));
			allCompanyCodes.add(curCompanyCode);
			line = br.readLine();
		}
		br.close();
		return allCompanyCodes;
	}

	public static void writeErrorLogEntry(String errorLogLocation, Throwable e, LocalDateTime timestamp) {
		PrintStream stream;
		try {
			stream = new PrintStream(new FileOutputStream(new File(errorLogLocation),true));
			stream.println("-----------------------------");
			stream.println(timestamp.format(StandardDateTimeFormatter.getStandardDateTimeFormatter()));
			e.printStackTrace(stream);
			stream.println("-----------------------------");
			stream.close();
		} catch (FileNotFoundException e1) {
			e1.printStackTrace();
			System.out.println("error logging broken");
			throw new AssertionError("error logging broken");
		}
	}

}
