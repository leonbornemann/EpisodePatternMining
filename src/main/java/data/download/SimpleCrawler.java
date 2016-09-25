package data.download;

import static org.quartz.JobBuilder.newJob;
import static org.quartz.SimpleScheduleBuilder.simpleSchedule;
import static org.quartz.TriggerBuilder.newTrigger;

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

import prediction.util.IOService;
import prediction.util.StandardDateTimeFormatter;

public class SimpleCrawler {

	private static String dataBaseLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\Low Level Data\\";
	private static String errorLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\errorLog.txt";
	private static String outLogLocation = "D:\\Personal\\Documents\\Uni\\Master thesis\\Datasets\\Finance\\log.txt";
	
	public static void main(String[] args) throws IOException, SchedulerException {
		quartz();
		//stdJava();
	}

	private static void quartz() throws SchedulerException {
		//new way using Quartz:
		// Grab the Scheduler instance from the Factory
		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
		// and start it off
		scheduler.start();
		
		JobDetail job = newJob(StockMarketCrawlingTask.class).withIdentity("job1", "group1").build();
		// Trigger the job to run now, and then repeat every 40 seconds
		Trigger trigger = newTrigger().withIdentity("trigger1", "group1").startNow().withSchedule(simpleSchedule().withIntervalInSeconds(15).repeatForever()).build();

		// Tell quartz to schedule the job using our trigger
		scheduler.scheduleJob(job, trigger);
	}

	private static void stdJava() throws IOException {
		ScheduledExecutorService service = Executors.newScheduledThreadPool(1);
		List<String> allCompanyCodes = new ArrayList<>(IOService.getAllCompanyCodes());
		Collections.sort(allCompanyCodes);
		String dateToday = LocalDate.now().format(StandardDateTimeFormatter.getStandardDateFormatter());
		new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation + "NASDAQ_" +dateToday + ".csv",errorLogLocation,outLogLocation).run();
		service.scheduleAtFixedRate(new StockMarketCrawlingTask(allCompanyCodes,dataBaseLocation + "NASDAQ_" +dateToday + ".csv",errorLogLocation,outLogLocation), 0, 15, TimeUnit.SECONDS);
	}
}
