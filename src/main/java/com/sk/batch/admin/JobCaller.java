package com.sk.batch.admin;

import org.springframework.batch.core.JobExecution;

public interface JobCaller {
	String getCallerName();
	void jobStarted(TriggerJobInfo jobInfo, JobExecution exec);
	void jobFinished(TriggerJobInfo jobInfo, JobExecution exec);
}
