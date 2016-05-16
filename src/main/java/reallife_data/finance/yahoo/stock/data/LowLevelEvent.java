package reallife_data.finance.yahoo.stock.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import reallife_data.finance.yahoo.stock.util.StandardDateTimeFormatter;

public class LowLevelEvent {

	private String companyId;
	private double stockValue;
	private LocalDateTime timestamp;
	
	public LowLevelEvent(String companyId, double stockValue, LocalDateTime timestamp) {
		super();
		this.companyId = companyId;
		this.stockValue = stockValue;
		this.timestamp = timestamp;
	}

	public static List<LowLevelEvent> readAll(File source) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(source));
		List<LowLevelEvent> events = new ArrayList<>();
		br.readLine();
		String line = br.readLine();
		while(line!=null && !line.equals("")){
			String[] tokens = line.split(",");
			assert(tokens.length==3);
			if(tokens[1].equals("N/A")){
				System.out.println("Skipping event of company "+tokens[0] + " at time " + tokens[2] );
			} else{
				events.add(new LowLevelEvent(tokens[0], Double.parseDouble(tokens[1]), LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter())));
			}
			line = br.readLine();
		}
		return events;
	}
	
	public String getCompanyId(){
		return companyId;
	}

	public double getValue() {
		return stockValue;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

}
