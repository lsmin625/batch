package com.sk.batch.admin;

import java.text.SimpleDateFormat;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRestartException;

public class TriggerJobExecutor implements Runnable {
	private Logger logger = LoggerFactory.getLogger(TriggerJobExecutor.class);

	private JobLauncher jobLauncher;
	private TriggerJobInfo jobInfo;

	public TriggerJobExecutor(JobLauncher jobLauncher, TriggerJobInfo jobInfo) {
		this.jobLauncher = jobLauncher;
		this.jobInfo = jobInfo;
	}
	
	@Override
	public void run() {
		try {
			JobParametersBuilder para = new JobParametersBuilder();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd-HHmmss");
			para.addString(jobInfo.getName(), formatter.format(new Date()) + "@" + jobInfo.getCaller().getCallerName());
			JobExecution jobExecution = jobLauncher.run(jobInfo.getJob(), para.toJobParameters());
			jobInfo.getCaller().jobFinished(jobInfo, jobExecution);
		} catch (JobRestartException e) {
			logger.error("#### JOB INSTANCE DUPLICATED!!!", e);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
