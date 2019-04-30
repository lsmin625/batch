package com.sk.batch.jobs;

import org.springframework.batch.core.JobExecution;

import com.sk.batch.admin.TriggerJobInfo;

public interface JobCaller {
	String getCallerName();
	void jobStarted(TriggerJobInfo jobInfo, JobExecution exec);
	void jobFinished(TriggerJobInfo jobInfo, JobExecution exec);
}
