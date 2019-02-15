package com.sk.batch.jobs.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class JobFinishedListener extends JobExecutionListenerSupport {
	private Logger logger = LoggerFactory.getLogger(JobFinishedListener.class);

	@Override
	public void beforeJob(JobExecution jobExecution) {
		logger.info("JOB STARTING! : " + jobExecution.getStatus());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("JOB COMPLETED! : " + jobExecution.getStatus());
		}
		else {
			logger.info("JOB NOT COMPLETED!!! : " + jobExecution.getStatus());
		}
	}

}
