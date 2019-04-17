package com.sk.batch;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

@Import(JobConfig.class)
@Component
public class JobExecutor {
	private Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	
	@Autowired @Qualifier("metaJobLauncher")
	private JobLauncher jobLauncher;

	@Autowired
	private JobFinishedListener listener;
	
	private JobExecution jobExecution;

    public synchronized JobExecution execute(Job job, JobCaller caller) {
    	if(jobExecution != null && (jobExecution.isRunning() || jobExecution.isStopping())) {
			logger.info("#### JOB IS RUNNING...IGNORED REQUEST FROM: " + caller.getCallerName());
    		return jobExecution;
    	}
 		try {
			listener.setCaller(caller);
			JobParametersBuilder para = new JobParametersBuilder();
			SimpleDateFormat formatter = new SimpleDateFormat(JobConfig.DATEFORMAT);
			para.addString("JOB", job.getName());
			para.addString("LAUNCH-TIME", formatter.format(new Date()));
			para.addString("CALLER", caller.getCallerName());
			jobExecution = jobLauncher.run(job, para.toJobParameters());
			return jobExecution;
		} catch (JobRestartException e) {
			logger.info("#### JOB INSTANCE DUPLICATED!!!");

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
   
    public JobExecution forceToStop() {
    	if(jobExecution != null && jobExecution.isRunning()) {
    		jobExecution.stop();
    	}
    	return jobExecution;
    }
}
