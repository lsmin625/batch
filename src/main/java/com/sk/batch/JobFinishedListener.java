package com.sk.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobFinishedListener extends JobExecutionListenerSupport {
	private Logger logger = LoggerFactory.getLogger(JobFinishedListener.class);
	private JobCaller caller = null;

	public synchronized void setCaller(JobCaller caller) {
		logger.info("#### SET CALLER : " + caller.getCallerName());
		this.caller = caller;
	}
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		if(caller != null) {
			caller.jobStarted(jobExecution.getStatus());
		}
		logger.info("#### JOB STARTING! : " + jobExecution.getStatus());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(caller != null) {
			caller.jobFinished(jobExecution.getStatus());
		}
		if(jobExecution.getStatus() == BatchStatus.COMPLETED) {
			logger.info("#### JOB COMPLETED in SUCCESS! : " + jobExecution.getStatus());
		}
		else {
			logger.info("#### JOB COMPLETED in FAIL!!!! : " + jobExecution.getStatus());
		}
	}

}
