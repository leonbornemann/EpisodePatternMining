package data.crawling;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import util.IOService;
import util.StandardDateTimeFormatter;

/***
 * Main class for the extraction of financial data from the web-source
 * @author Leon Bornemann
 *
 */
public class SimpleCrawler {

	private static String dataBaseLocation;
	private static String errorLogLocation;
	private static String outLogLocation;
	
	/***
	 * 
	 * @param args contains one argument, which is the working directory in which the started process must have read and write access)
	 * @throws IOException
	 * @throws SchedulerException
	 */
	public static void main(String[] args) throws IOException, SchedulerException {
		String workingDir = args[0];
		if(!workingDir.endsWith(File.separator)){
			workingDir = workingDir + File.separator;
		}
		dataBaseLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "Low Level Data" + File.separator;
		new File(dataBaseLocation).mkdirs();
		errorLogLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "errorLog.txt";
		outLogLocation = workingDir + "Datasets" + File.separator + "Finance" + File.separator + "log.txt";
		//this is pretty ugly, but we need to set static variables, since quartz initializes the jobs straight away:
		StockMarketCrawlingTask.errorLogLocation = errorLogLocation;
		StockMarketCrawlingTask.outLogLocation = outLogLocation;
		StockMarketCrawlingTask.workingDirectory = dataBaseLocation;
		quartz();
		//stdJava();
	}

	/***
	 * Uses quartz library to schedule the StockMarket Crawling task every 15 seconds from now on
	 * @throws SchedulerException
	 */
	private static void quartz() throws SchedulerException {
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		scheduler.start();
		JobDetail job = newJob(StockMarketCrawlingTask.class).withIdentity("job1", "group1").build();
		// Trigger the job to run now, and then repeat every 15 seconds
		Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startNow().withSchedule(simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();
		scheduler.scheduleJob(job, trigger);
	}

	/***
	 * old variant, that had some issues which terminated the process sometimes and led to weird errors
	 * @throws IOException
	 */
	private static void stdJava() throws IOException {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		List<String> allCompanyCodes = new ArrayList<>(IOService.getAllCompanyCodes());
		Collections.sort(allCompanyCodes);
		String dateToday = LocalDate.now().format(StandardDateTimeFormatter.getStandardDateFormatter());
		new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation + "NASDAQ_" +dateToday + ".csv",errorLogLocation,outLogLocation).run();
		service.scheduleAtFixedRate(new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation + "NASDAQ_" +dateToday + ".csv",errorLogLocation,outLogLocation), 0, 15, TimeUnit.SECONDS);
	}
}
