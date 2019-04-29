package com.sk.batch.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.stereotype.Service;

import com.sk.batch.jobs.job01.JobConfig;

@Service
public class JobExecutor {
	private Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobFinishedListener listener;
	
	private static Hashtable<Job, JobExecution> execTable = new Hashtable<Job, JobExecution>();

    public synchronized JobExecution execute(Job job, JobCaller caller) {
    	JobExecution jobExecution = execTable.get(job);
    	if(jobExecution != null && (jobExecution.isRunning() || jobExecution.isStopping())) {
			logger.info("#### JOB IS RUNNING...IGNORED REQUEST FROM CALLER=" + caller.getCallerName() + ", JOB=" + job.getName());
    		return jobExecution;
    	}
 		try {
			listener.setCaller(caller);
			JobParametersBuilder para = new JobParametersBuilder();
			SimpleDateFormat formatter = new SimpleDateFormat(DATEFORMAT);
			para.addString("JOB", job.getName());
			para.addString("LAUNCH-TIME", formatter.format(new Date()));
			para.addString("CALLER", caller.getCallerName());
			jobExecution = jobLauncher.run(job, para.toJobParameters());
			execTable.put(job, jobExecution);
			
			return jobExecution;
		} catch (JobRestartException e) {
			logger.info("#### JOB INSTANCE DUPLICATED!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
 		return null;
    }
    
    public synchronized JobExecution getStatus(Job job) {
    	JobExecution jobExecution = execTable.get(job);
    	if(jobExecution != null) {
    		return jobExecution;
    	}
    	else {
    		return null;
    	}
    }
   
    public JobExecution forceToStop(Job job) {
    	JobExecution jobExecution = execTable.get(job);
    	if(jobExecution != null && jobExecution.isRunning()) {
    		jobExecution.stop();
    	}
    	return jobExecution;
    }
}
