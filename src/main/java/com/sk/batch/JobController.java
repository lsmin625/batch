package com.sk.batch;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Import(JobScheduler.class)
public class JobController implements JobCaller{
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired
	private Job sampleJob;
 
	@Autowired
	private JobExecutor executor;
	
	@Autowired
	private JobScheduler scheduler;

    @RequestMapping("/schedule")
    public Map<String, Object> setSchedule(@RequestParam Map<String, String> param) {
    	Decoder dec = Base64.getDecoder();
    	String cron = new String(dec.decode(param.get("cron")));
    	logger.info("#### REQUESTED REST API [/schedule] CRON=" + cron);
 
    	scheduler.setCron(cron);

    	JobExecution exec = executor.getStatus();
    	return getStatus(exec);
    }

    @RequestMapping("/start")
    public Map<String, Object> doStart(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/start]");
    	JobExecution exec = executor.execute(sampleJob, this);
		return getStatus(exec);
    }

    @RequestMapping("/status")
    public Map<String, Object> doStatus(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/status]");
    	JobExecution exec = executor.getStatus();
		return getStatus(exec);
    }

    @RequestMapping("/stop")
    public Map<String, Object> doStop(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/stop]");
     	JobExecution exec = executor.forceToStop();
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
