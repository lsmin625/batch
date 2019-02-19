package com.sk.batch;

import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Import(JobConfig.class)
public class JobController implements JobCaller{
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired @Qualifier("sampleBatchJob")
	private Job sampleJob;
 
	@Autowired
	private JobExecutor executor;

    @RequestMapping("/restart")
    public Map<String, Object> doRestart(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/restart]");
    	Map<String, Object> res = new HashMap<String, Object>();

    	BatchStatus status = executor.execute(sampleJob, this);
    	if(status != null) {
    		res.put("execution", status.toString());
    	}
    	else {
    		res.put("execution", "failed");
    	}

		return res;
    }

    @RequestMapping("/status")
    public Map<String, Object> doStatus(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/status]");
    	Map<String, Object> res = new HashMap<String, Object>();
    	SimpleDateFormat form = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
 
    	JobExecution status = executor.getStatus();
    	if(status != null) {
    		res.put("creat-time", form.format(status.getCreateTime()));
    		res.put("start-time", form.format(status.getStartTime()));
    		res.put("end-time", form.format(status.getEndTime()));
    		res.put("status", status.getStatus().toString());
    		res.put("parameters", status.getJobParameters());
    	}
    	else {
    		res.put("status", "unknown");
    	}

    	return res;
    }

    @RequestMapping("/stop")
    public Map<String, Object> doStop(@RequestParam Map<String, String> param) {
    	logger.info("#### REQUESTED REST API [/stop]");
    	Map<String, Object> res = new HashMap<String, Object>();
 
    	executor.forceToStop();
   		res.put("stop", "forced");

    	return res;
    }


	@Override
	public void jobStarted(BatchStatus status) {
		if(status == BatchStatus.STARTED) {
			logger.info("#### CONTROLLER JOB STARTED");
		}
		else {
			logger.info("#### CONTROLLER JOB unknown status" + status);
		}
	}

	@Override
	public void jobFinished(BatchStatus status) {
		if(status == BatchStatus.COMPLETED) {
			logger.info("#### CONTROLLER JOB COMPLETED in SUCCESS!");
		}
		else {
			logger.info("#### CONTROLLER JOB COMPLETED in FAIL!!!!" + status);
		}
	}


	@Override
	public String getCallerName() {
		return "CONTROLLER";
	}
}
