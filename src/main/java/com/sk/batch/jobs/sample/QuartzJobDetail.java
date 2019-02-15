package com.sk.batch.jobs.sample;

import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import com.sk.batch.jobs.sample.common.MetaConfig;

@Import({MetaConfig.class, JobConfig.class})
@Component
public class QuartzJobDetail implements org.quartz.Job {
	private Logger logger = LoggerFactory.getLogger(QuartzJobDetail.class);
	
	@Autowired @Qualifier("metaJobLauncher")
	private JobLauncher jobLauncher;

	@Autowired @Qualifier("sampleBatchJob")
	private org.springframework.batch.core.Job sampleBatchJob;

	@Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
		logger.info("#### QUARTZ JOB DETAIL...execute");
		try {
			JobParameters para = new JobParameters();
			if(jobLauncher == null) {
				logger.info("#### jobLauncher is NULL");
			}
			else {
				jobLauncher.run(sampleBatchJob, para);
			}
		}
		catch(Exception e){
			e.printStackTrace();
		}
	}
}
