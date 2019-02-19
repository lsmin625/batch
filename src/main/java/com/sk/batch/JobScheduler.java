package com.sk.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@EnableBatchProcessing @EnableScheduling
@Configuration @Import(JobConfig.class)
public class JobScheduler implements JobCaller {
	private Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	@Autowired @Qualifier("sampleBatchJob")
	private Job sampleJob;

	@Autowired
	private JobExecutor executor;

    @Scheduled(cron = "0 * * * * ?")
    public void doJob() {
    	logger.info("#### DO SCHEDULE every 1 MIN.");
		executor.execute(sampleJob, this);
    }

	@Override
	public void jobStarted(BatchStatus status) {
		if(status == BatchStatus.STARTED) {
			logger.info("#### SCHEDULER JOB STARTED");
		}
		else {
			logger.info("#### SCHEDULER JOB unknown status" + status);
		}
	}

	@Override
	public void jobFinished(BatchStatus status) {
		if(status == BatchStatus.COMPLETED) {
			logger.info("#### SCHEDULER JOB COMPLETED in SUCCESS!");
		}
		else {
			logger.info("#### SCHEDULER JOB COMPLETED in FAIL!!!!");
		}
	}

	@Override
	public String getCallerName() {
		return "SCHEDULER";
	}

}
