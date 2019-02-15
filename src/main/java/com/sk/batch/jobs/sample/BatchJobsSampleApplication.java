package com.sk.batch.jobs.sample;

import org.quartz.CronScheduleBuilder;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.impl.StdSchedulerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class BatchJobsSampleApplication {
	private static Logger logger = LoggerFactory.getLogger(BatchJobsSampleApplication.class);
	private static final String CRON = "/20 * * * * ?";
	private static SchedulerFactory schedulerFactory;
	private static Scheduler scheduler;
	
	public static void main(String[] args) {
		try {
			SpringApplication.run(BatchJobsSampleApplication.class, args);

			JobBuilder jobBuilder = JobBuilder.newJob(QuartzJobDetail.class);
			JobDetail jobDetail = jobBuilder.withIdentity("quartzJobDetail").build();
			Trigger trigger = TriggerBuilder.newTrigger().withSchedule(CronScheduleBuilder.cronSchedule(CRON)).build();

			logger.info("#### QUARTZ SCHEDULE CRON=" + CRON);
			schedulerFactory = new StdSchedulerFactory();
			scheduler = schedulerFactory.getScheduler();
			scheduler.scheduleJob(jobDetail, trigger);
			scheduler.start();	
		}
		catch(Exception e) {
			e.printStackTrace();
		}
	}

}

