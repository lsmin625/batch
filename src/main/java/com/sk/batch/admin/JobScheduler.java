package com.sk.batch.admin;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.concurrent.Executor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.config.CronTask;
import org.springframework.scheduling.config.ScheduledTask;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.stereotype.Service;

@Service
public class JobScheduler implements JobCaller, SchedulingConfigurer {
	private Logger logger = LoggerFactory.getLogger(JobScheduler.class);

	@Autowired private JobLauncher jobLauncher;
	@Autowired private Executor taskExecutor;
	private static Hashtable<String, ScheduledTask> cronTable = new Hashtable<String, ScheduledTask>();
    private ScheduledTaskRegistrar taskRegistrar;

    @Override
	public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
    	this.taskRegistrar = taskRegistrar;
    	taskRegistrar.setScheduler(taskExecutor);
    }

	public void removeCron(TriggerJobInfo jobInfo) {
        if(cronTable.containsKey(jobInfo.getName())) {
			ScheduledTask schedule = cronTable.get(jobInfo.getName());
            logger.info("#### REMOVE EXISTING JOB=" + jobInfo.getName());
			schedule.cancel();
			cronTable.remove(jobInfo.getName());
        }
        else {
            logger.info("#### REMOVE FAILED UNKNOWN JOB=" + jobInfo.getName());
        }
 	}

	public void setCron(TriggerJobInfo jobInfo, String cron) {
        if(cronTable.containsKey(jobInfo.getName())) {
			ScheduledTask schedule = cronTable.get(jobInfo.getName());
			CronTask task = (CronTask) schedule.getTask();
            logger.info("#### CANCEL OLD JOB=" + jobInfo.getName() + ", CRON=" + task.getExpression());
			schedule.cancel();
        }
        jobInfo.setCron(cron);
        jobInfo.setCaller(this);
        CronTask cronTask = new CronTask(new TriggerJobExecutor(jobLauncher, jobInfo), cron);
        ScheduledTask schedule = taskRegistrar.scheduleCronTask(cronTask);
		cronTable.put(jobInfo.getName(), schedule);
        logger.info("#### SCHEDULE NEW JOB=" + jobInfo.getName() + ", CRON=" + cronTask.getExpression());
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
	public void jobStarted(TriggerJobInfo jobInfo, JobExecution exec) {
		logger.info("#### SCHEDULER JOB STARTED\n" + getStatus(exec));
	}

	@Override
	public void jobFinished(TriggerJobInfo jobInfo, JobExecution exec) {
		if(exec.getStatus() == BatchStatus.COMPLETED) {
			logger.info("#### SCHEDULER JOB COMPLETED in SUCCESS!" + getStatus(exec));
		}
		else {
			logger.info("#### SCHEDULER JOB COMPLETED in FAIL!!!!" + getStatus(exec));
		}
	}

	@Override
	public String getCallerName() {
		return "SCHEDULER";
	}

}
