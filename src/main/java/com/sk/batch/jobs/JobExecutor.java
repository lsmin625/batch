package com.sk.batch.jobs;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.sk.batch.admin.TriggerJobInfo;

@Service
public class JobExecutor {
	private Logger logger = LoggerFactory.getLogger(JobExecutor.class);
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobFinishedListener jobFinishedListener;
	
	private static Hashtable<TriggerJobInfo, JobExecution> execTable = new Hashtable<TriggerJobInfo, JobExecution>();

    public synchronized JobExecution execute(TriggerJobInfo jobInfo, JobCaller caller) {
    	JobExecution jobExecution = execTable.get(jobInfo);
    	if(jobExecution != null && (jobExecution.isRunning() || jobExecution.isStopping())) {
			logger.info("#### JOB IS RUNNING...IGNORED REQUEST FROM CALLER=" + caller.getCallerName() + ", JOB: " + jobInfo.toString());
    		return jobExecution;
    	}
 		try {
			JobParametersBuilder para = new JobParametersBuilder();
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			para.addString(jobInfo.getName(), formatter.format(new Date()) + "@" + caller.getCallerName());
			jobExecution = jobLauncher.run(jobInfo.getJob(), para.toJobParameters());
			jobInfo.setCaller(caller);
			execTable.put(jobInfo, jobExecution);
			jobFinishedListener.setJobExecution(jobExecution, jobInfo);
			return jobExecution;
		} catch (JobRestartException e) {
			logger.error("#### JOB INSTANCE DUPLICATED!!!");

		} catch (Exception e) {
			e.printStackTrace();
		}
 		return null;
    }
    
    public synchronized JobExecution getStatus(TriggerJobInfo jobInfo) {
    	JobExecution jobExecution = execTable.get(jobInfo);
    	if(jobExecution != null) {
    		return jobExecution;
    	}
    	else {
    		return null;
    	}
    }
   
    public JobExecution forceToStop(TriggerJobInfo jobInfo) {
    	JobExecution jobExecution = execTable.get(jobInfo);
    	if(jobExecution != null && jobExecution.isRunning()) {
    		jobExecution.stop();
    	}
    	return jobExecution;
    }
}
