package com.sk.batch;

import org.springframework.batch.core.BatchStatus;

public interface JobCaller {
	String getCallerName();
	void jobStarted(BatchStatus status);
	void jobFinished(BatchStatus status);
}
