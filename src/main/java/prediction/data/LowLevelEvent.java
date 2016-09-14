package prediction.data;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import prediction.util.StandardDateTimeFormatter;

public class LowLevelEvent {

	private String companyId;
	private BigDecimal stockValue;
	private LocalDateTime timestamp;
	
	public LowLevelEvent(String companyId, BigDecimal stockValue, LocalDateTime timestamp) {
		super();
		this.companyId = companyId;
		this.stockValue = stockValue;
		this.timestamp = timestamp;
	}

	public static List<LowLevelEvent> readAll(File source) throws IOException {
		System.out.println("beginning file "+source.getName());
		BufferedReader br = new BufferedReader(new FileReader(source));
		try{
			List<LowLevelEvent> events = new ArrayList<>();
			br.readLine();
			String line = br.readLine();
			int lineCount = 2;
			while(line!=null && !line.equals("")){
				String[] tokens = line.split(",");
				if(tokens.length!=3){
					System.out.println(line);
					System.out.println(lineCount);
					assert(false);
				}
				if(tokens[0].equals("")){
					System.out.println("empty company at "+lineCount);
					assert(false);
				}
				if(tokens[1].equals("N/A")){
					//System.out.println("Skipping event of company "+tokens[0] + " at time " + tokens[2] );
				} else{
					events.add(new LowLevelEvent(tokens[0], new BigDecimal(tokens[1]), LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter())));
				}
				if(lineCount % 1000000==0){
					System.out.println("done with "+lineCount);
				}
				lineCount++;
				line = br.readLine();
			}
			return events;
		} finally{
			br.close();	
		}
	}
	
	public int temporalOrder(LowLevelEvent other){
		return timestamp.compareTo(other.timestamp);
	}
	
	public String getCompanyId(){
		return companyId;
	}

	public BigDecimal getValue() {
		return stockValue;
	}

	public LocalDateTime getTimestamp() {
		return timestamp;
	}

	/***
	 * opens the target file, reads the starting price for the target company and returns it.
	 * @param filename
	 * @param companyID
	 * @return
	 * @throws IOException 
	 */
	public static BigDecimal getStartingPrice(File source, String companyID) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(source));
		try{
			br.readLine();
			String line = br.readLine();
			int lineCount = 2;
			while(line!=null && !line.equals("")){
				String[] tokens = line.split(",");
				if(tokens.length!=3){
					System.out.println(line);
					System.out.println(lineCount);
					assert(false);
				}
				if(tokens[0].equals("")){
					System.out.println("empty company at "+lineCount);
					assert(false);
				}
				if(tokens[1].equals("N/A")){
					//System.out.println("Skipping event of company "+tokens[0] + " at time " + tokens[2] );
				} else{
					LowLevelEvent curEvent = new LowLevelEvent(tokens[0], new BigDecimal(tokens[1]), LocalDateTime.parse(tokens[2], StandardDateTimeFormatter.getStandardDateTimeFormatter()));
					if(curEvent.getCompanyId().equals(companyID)){
						return curEvent.getValue();
					}
				}
				lineCount++;
				line = br.readLine();
			}
			return null;
		} finally{
			br.close();	
		}
	}

}
