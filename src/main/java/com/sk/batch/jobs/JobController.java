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
	private TriggerJobList jobList;

    @RequestMapping("/schedule")
    public Map<String, Object> setSchedule(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
    	TriggerJobInfo jobInfo = jobList.get(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	Decoder dec = Base64.getDecoder();
    	String cron = new String(dec.decode(param.get("cron")));
    	logger.info("#### REQUESTED REST API [/schedule] JOB=" + job + ", CRON=" + cron);
 
    	scheduler.setCron(jobInfo.getJob(), cron);

    	JobExecution exec = executor.getStatus(jobInfo.getJob());
    	return getStatus(exec);
    }

    @RequestMapping("/start")
    public Map<String, Object> doStart(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
    	TriggerJobInfo jobInfo = jobList.get(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/start] JOB=" + job);
    	JobExecution exec = executor.execute(jobInfo.getJob(), this);
		return getStatus(exec);
    }

    @RequestMapping("/status")
    public Map<String, Object> doStatus(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
    	TriggerJobInfo jobInfo = jobList.get(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/status] JOB=" + job);
    	JobExecution exec = executor.getStatus(jobInfo.getJob());
		return getStatus(exec);
    }

    @RequestMapping("/stop")
    public Map<String, Object> doStop(@RequestParam Map<String, String> param) {
    	String job = param.get("job");
    	if(job == null || job.equals("")) {
    		return getError("job missed");
    	}
    	TriggerJobInfo jobInfo = jobList.get(job);
    	if(jobInfo == null) {
    		return getError("job unknown");
    	}

    	logger.info("#### REQUESTED REST API [/stop] JOB=" + job);
     	JobExecution exec = executor.forceToStop(jobInfo.getJob());
		return getStatus(exec);
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

    private Map<String, Object> getError(String error) {
    	Map<String, Object> obj = new HashMap<String, Object>();
   		obj.put("status", "unknown");
   		obj.put("error", error);
    	return obj;
    }

	@Override
	public void jobStarted(JobExecution exec) {
		logger.info("#### CONTROLLER JOB STARTED\n" + getStatus(exec));
	}

	@Override
	public void jobFinished(JobExecution exec) {
		if(exec.getStatus() == BatchStatus.COMPLETED) {
			logger.info("#### CONTROLLER JOB COMPLETED in SUCCESS!\n" + getStatus(exec));
		}
		else {
			logger.info("#### CONTROLLER JOB COMPLETED in FAIL!!!!\n" + getStatus(exec));
		}
	}

	@Override
	public String getCallerName() {
		return "CONTROLLER";
	}
}
