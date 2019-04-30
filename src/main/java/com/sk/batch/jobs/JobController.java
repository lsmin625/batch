package com.sk.batch.jobs;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.explore.JobExplorer;
import org.springframework.batch.core.launch.JobOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sk.batch.admin.TriggerJobInfo;
import com.sk.batch.admin.TriggerJobList;

@RestController
public class JobController implements JobCaller{
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired
	private JobExecutor executor;
	
	@Autowired
	private JobScheduler scheduler;
	
	@Autowired
	private JobOperator jobOperator;
	
	@Autowired
	private JobExplorer jobExplorer;

	@Autowired
	private TriggerJobList triggerJobList;

	private static Decoder Dec = Base64.getDecoder();
	
	private TriggerJobInfo getJobInfo(String job) {
		for(TriggerJobInfo jobInfo : triggerJobList) {
			if(job.equalsIgnoreCase(jobInfo.getName())) {
				return jobInfo;
			}
		}
		return null;
	}

    @RequestMapping("/schedule")
    public Map<String, Object> setSchedule(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
//    	TriggerJobInfo jobInfo = triggerJobList.get(Dec.decode(job));
    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	String cron = new String(Dec.decode(param.get("cron")));
    	logger.info("#### REQUESTED REST API [/schedule] JOB=" + job + ", CRON=" + cron);
 
    	scheduler.setCron(jobInfo, cron);

    	JobExecution exec = executor.getStatus(jobInfo);
    	return getStatus(jobInfo, exec);
    }

    @RequestMapping("/start")
    public Map<String, Object> doStart(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
//    	TriggerJobInfo jobInfo = triggerJobList.get(Dec.decode(job));
    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/start] JOB=" + job);
    	JobExecution exec = executor.execute(jobInfo, this);
		return getStatus(jobInfo, exec);
    }

    @RequestMapping("/status")
    public Map<String, Object> doStatus(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
//    	TriggerJobInfo jobInfo = triggerJobList.get(Dec.decode(job));
    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/status] JOB=" + job);
    	JobExecution exec = executor.getStatus(jobInfo);
		return getStatus(jobInfo, exec);
    }

    @RequestMapping("/stop")
    public Map<String, Object> doStop(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
//    	TriggerJobInfo jobInfo = triggerJobList.get(Dec.decode(job));
    	TriggerJobInfo jobInfo = getJobInfo(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/stop] JOB=" + job);
     	JobExecution exec = executor.forceToStop(jobInfo);
		return getStatus(jobInfo, exec);
    }

    private Map<String, Object> getStatus(TriggerJobInfo jobInfo, JobExecution exec) {
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

    	return obj;
    }

    private Map<String, Object> getError(String error) {
    	Map<String, Object> obj = new HashMap<String, Object>();
   		obj.put("status", "unknown");
   		obj.put("error", error);
    	return obj;
    }

	@Override
	public void jobStarted(TriggerJobInfo jobInfo, JobExecution exec) {
		logger.info("#### CONTROLLER JOB STARTED\n" + getStatus(null, exec));
	}

	@Override
	public void jobFinished(TriggerJobInfo jobInfo, JobExecution exec) {
		if(exec.getStatus() == BatchStatus.COMPLETED) {
			logger.info("#### CONTROLLER JOB COMPLETED in SUCCESS!\n" + getStatus(null, exec));
		}
		else {
			logger.info("#### CONTROLLER JOB COMPLETED in FAIL!!!!\n" + getStatus(null, exec));
		}
	}

	@Override
	public String getCallerName() {
		return "CONTROLLER";
	}
}
