package com.sk.batch;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import org.springframework.stereotype.Component;

@Component
public class JobFinishedListener extends JobExecutionListenerSupport {
	private JobCaller caller = null;

	public synchronized void setCaller(JobCaller caller) {
		this.caller = caller;
	}
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		if(caller != null) {
			caller.jobStarted(jobExecution);
		}
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		if(caller != null) {
			caller.jobFinished(jobExecution);
		}
	}

}
