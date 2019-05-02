package com.sk.batch.admin;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobController implements JobCaller{
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired
	private JobLauncher jobLauncher;

	@Autowired
	private JobFinishedListener jobFinishedListener;

	@Autowired
	private TriggerJobList triggerJobList;

	@Autowired
	private JobScheduler scheduler;
	
	private static Decoder Dec = Base64.getDecoder();
	
	@RequestMapping("/schedule")
    public Map<String, Object> setSchedule(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	String cron = param.get("cron");
    	logger.info("#### REQUESTED [/schedule] JOB=" + job + ", CRON=" + cron);

    	if(job == null || job.equals("")) {
    		return getError("parameter job missed", "[/schedule]");
    	}
//    	job = new String(Dec.decode(job));

    	if(job == null || job.equals("")) {
    		return getError("parameter cron missed", "[/schedule]");
    	}
    	cron = new String(Dec.decode(cron));
    	
    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("unknown job=" + job, "[/schedule]");
    	}
 
    	logger.info("#### PROCESSONG [/schedule] OLD JOB=" + jobInfo.toString() + ", NEW CRON=" + cron);
     	scheduler.setCron(jobInfo, cron);

       	JobExecution exec = jobFinishedListener.getJobExecution(job);
       	return getStatus(jobInfo, exec, "[/schedule]");
    }

    @RequestMapping("/status")
    public Map<String, Object> doStatus(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	logger.info("#### REQUESTED [/status] JOB=" + job);

    	if(job == null || job.equals("")) {
    		return getError("parameter job missed", "[/status]");
    	}
//    	job = new String(Dec.decode(job));

    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("unknown job=" + job, "[/status]");
    	}

    	JobExecution exec = jobFinishedListener.getJobExecution(job);
    	return getStatus(jobInfo, exec, "[/status]");
    }

    @RequestMapping("/start")
    public Map<String, Object> doStart(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	logger.info("#### REQUESTED [/start] JOB=" + job);

    	if(job == null || job.equals("")) {
    		return getError("parameter job missed", "[/start]");
    	}
//    	job = new String(Dec.decode(job));

    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("unknown job=" + job, "[/start]");
    	}

    	jobInfo.setCaller(this);
		logger.info("#### PROCESSONG [/start] JOB=" + jobInfo.toString());

		TriggerJobExecutor triggerJob = new TriggerJobExecutor(jobLauncher, jobInfo);
		Thread thread = new Thread(triggerJob);
		thread.start();

		Thread.yield();
    	JobExecution exec = jobFinishedListener.getJobExecution(job);
    	return getStatus(jobInfo, exec, "[/start]");
    }

    @RequestMapping("/stop")
    public Map<String, Object> doStop(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	logger.info("#### REQUESTED [/stop] JOB=" + job);

    	if(job == null || job.equals("")) {
    		return getError("parameter job missed", "[/stop]");
    	}
//    	job = new String(Dec.decode(job));

    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("unknown job=" + job, "[/stop]");
    	}

    	jobInfo.setCaller(this);
		logger.info("#### PROCESSONG [/stop] JOB=" + jobInfo.toString());

		JobExecution exec = jobFinishedListener.getJobExecution(job);
    	if(exec != null && exec.isRunning()) {
    		exec.stop();
    	}
    	
    	return getStatus(jobInfo, exec, "[/stop]");
    }

	private TriggerJobInfo getJobInfo(String job) {
		for(TriggerJobInfo jobInfo : triggerJobList) {
			if(job.equalsIgnoreCase(jobInfo.getName())) {
				return jobInfo;
			}
		}
		return null;
	}

    private Map<String, Object> getStatus(TriggerJobInfo jobInfo, JobExecution exec, String path) {
    	Map<String, Object> obj = new HashMap<String, Object>();
    	SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
    	if(jobInfo != null) {
    		obj.put("name", jobInfo.getName());
    		obj.put("description", jobInfo.getDesc());
    		obj.put("mode", jobInfo.getMode());
    		obj.put("cron", jobInfo.getCron());
    		obj.put("admin-url", jobInfo.getAdminUrl());
    		obj.put("callback-url", jobInfo.getCallbackUrl());
    	}
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

    	logger.info("#### RESPONSE " + path + " JOB=" + jobInfo.toString() + ", RES=" + obj.toString());
    	return obj;
    }

    private Map<String, Object> getError(String error, String path) {
    	Map<String, Object> obj = new HashMap<String, Object>();
   		obj.put("status", "unknown");
   		obj.put("error", error);

   		logger.info("#### RESPONSE " + path + " RES=" + obj.toString());
   		return obj;
    }

	@Override
	public void jobStarted(TriggerJobInfo jobInfo, JobExecution exec) {

	}

	@Override
	public void jobFinished(TriggerJobInfo jobInfo, JobExecution exec) {
		if(exec.getStatus() == BatchStatus.COMPLETED) {

		}
	}

	@Override
	public String getCallerName() {
		return "CONTROLLER";
	}
}
