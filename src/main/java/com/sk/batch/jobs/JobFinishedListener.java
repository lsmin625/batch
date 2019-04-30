package com.sk.batch.jobs;

import java.util.Hashtable;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.listener.JobExecutionListenerSupport;
import com.sk.batch.admin.TriggerJobInfo;

public class JobFinishedListener extends JobExecutionListenerSupport {
	private static Hashtable<JobExecution, TriggerJobInfo> jobTable = new Hashtable<JobExecution, TriggerJobInfo>();

	public synchronized void setJobExecution(JobExecution exec, TriggerJobInfo jobInfo) {
		jobTable.put(exec, jobInfo);
	}
	
	@Override
	public void beforeJob(JobExecution jobExecution) {
		TriggerJobInfo jobInfo = jobTable.get(jobExecution);
		if(jobInfo != null) {
			jobInfo.getCaller().jobStarted(jobInfo, jobExecution);
		}
	}

	@Override
	public void afterJob(JobExecution jobExecution) {
		TriggerJobInfo jobInfo = jobTable.get(jobExecution);
		if(jobInfo != null) {
			jobInfo.getCaller().jobFinished(jobInfo, jobExecution);
		}
	}

}
