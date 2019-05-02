package com.sk.batch.admin;

import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;

public class JobFinishedListener implements JobExecutionListener {
	private Logger logger = LoggerFactory.getLogger(JobFinishedListener.class);
	
	private static Hashtable<String, JobExecution> execTable = new Hashtable<String, JobExecution>();

	@Override
	public void beforeJob(JobExecution jobExecution) {
		String name = jobExecution.getJobInstance().getJobName();
		execTable.put(name, jobExecution);
		logger.debug("$$$$ beforeJob..." + jobExecution.toString());
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		logger.debug("$$$$ afterJob..." + jobExecution.toString());
	}
	
	public JobExecution getJobExecution(String name) {
		if(execTable.containsKey(name)) {
			return execTable.get(name);
		}
		else {
			return null;
		}
	}

}
