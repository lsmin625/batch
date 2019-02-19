package com.sk.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class JobExecutor {
	private Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	
	@Autowired @Qualifier("metaJobLauncher")
	private JobLauncher jobLauncher;

	@Autowired
	private JobFinishedListener listener;
	
	private JobExecution jobExecution;

    public synchronized BatchStatus execute(Job job, JobCaller caller) {
 		try {
			listener.setCaller(caller);
			JobParametersBuilder para = new JobParametersBuilder();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:00");
			para.addString("TIME", formatter.format(new Date()));
			para.addString("LAUNCHER", caller.getCallerName());
			jobExecution = jobLauncher.run(job, para.toJobParameters());
			return jobExecution.getStatus();
		} catch (JobRestartException e) {
			logger.info("#### JOB INSTANCE ALREADY EXISTS!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
 		return null;
    }
    
    public synchronized JobExecution getStatus() {
    	if(jobExecution != null) {
    		return jobExecution;
    	}
    	else {
    		return null;
    	}
    }
    
    public void forceToStop() {
    	if(jobExecution != null && jobExecution.isRunning()) {
    		jobExecution.stop();
    	}
    }
}
