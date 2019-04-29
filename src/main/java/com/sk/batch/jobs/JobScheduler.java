package com.sk.batch.jobs;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

import com.sk.batch.admin.TriggerJobInfo;

@Service
public class JobScheduler implements JobCaller, SchedulingConfigurer {
	private Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	@Autowired
	private JobExecutor jobExecutor;

	@Autowired
	private Executor taskExecutor;
	
	private static Hashtable<TriggerJobInfo, ScheduledTask> cronTable = new Hashtable<TriggerJobInfo, ScheduledTask>();
    private ScheduledTaskRegistrar taskRegistrar;


    @Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    	this.taskRegistrar = taskRegistrar;
    	taskRegistrar.setScheduler(taskExecutor);
    }

	public void setCron(TriggerJobInfo jobInfo, String cron) {
        logger.info("#### SET CRON JOB=" + jobInfo.getName() + ", CRON=" + cron);
        if(cronTable.containsKey(jobInfo)) {
			ScheduledTask schedule = cronTable.get(jobInfo);
			CronTask task = (CronTask) schedule.getTask();
            logger.info("#### CANCEL OLD JOB=" + jobInfo.getName() + ", CRON=" + task.getExpression());
			schedule.cancel();
        }
        jobInfo.setCron(cron);
        ScheduledTask schedule = taskRegistrar.scheduleCronTask(new CronTask(new Runnable() {
		    public void run() {
	    		doJob(jobInfo.getJob());
		    	}
			}, cron));
//		taskRegistrar.afterPropertiesSet(); //necessary when use "taskRegistrar.addCronTask()"
		CronTask task = (CronTask) schedule.getTask();
        logger.info("#### SET NEW JOB=" + jobInfo.getName() + ", CRON=" + task.getExpression());
		cronTable.put(jobInfo, schedule);
 	}

	public void doJob(Job job) {
    	logger.info("#### DO SCHEDULE JOB=" + job.getName());
    	jobExecutor.execute(job, this);
    }

    private Map<String, Object> getStatus(JobExecution exec) {
    	Map<String, Object> obj = new HashMap<String, Object>();
    	SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	if(exec != null) {
    		if(exec.getCreateTime() != null) {
    			obj.put("create-time", form.format(exec.getCreateTime()));
    		}
    		if(exec.getStartTime() != null) {
    			obj.put("start-time", form.format(exec.getStartTime()));
    		}
    		if(exec.getEndTime() != null) {
    			obj.put("end-time", form.format(exec.getEndTime()));
    		}
    		obj.put("status", exec.getStatus().toString());
    		obj.put("parameters", exec.getJobParameters().getParameters());
    	}
    	else {
    		obj.put("status", "unknown");
    	}

    	return obj;
    }

	@Override
	public void jobStarted(JobExecution exec) {
		logger.info("#### SCHEDULER JOB STARTED\n" + getStatus(exec));
	}

	@Override
	public void jobFinished(JobExecution exec) {
		if(exec.getStatus() == BatchStatus.COMPLETED) {
			logger.info("#### SCHEDULER JOB COMPLETED in SUCCESS!\n" + getStatus(exec));
		}
		else {
			logger.info("#### SCHEDULER JOB COMPLETED in FAIL!!!!\n" + getStatus(exec));
		}
	}

	@Override
	public String getCallerName() {
		return "SCHEDULER";
	}

}
