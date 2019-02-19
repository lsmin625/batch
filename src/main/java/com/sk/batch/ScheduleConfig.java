package com.sk.batch;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@Import(MetaConfig.class)
public class ScheduleConfig {
	private Logger logger = LoggerFactory.getLogger(ScheduleConfig.class);
	
	@Autowired @Qualifier("metaJobLauncher")
	private JobLauncher jobLauncher;

	@Autowired @Qualifier("sampleBatchJob")
	private Job sampleJob;

    @Scheduled(cron = "0/10 * * * * ?")
    public void doTen() {
    	logger.info("#### DO SCHEDULE every 10 SEC.");
    	
    	if(jobLauncher == null){
    		logger.info("#### JOB LAUNCHER IS NULL !!!");
    	}
    	else {
    		logger.info("#### JOB LAUNCHER IS GOOD.");
        	try {
        		JobParametersBuilder para = new JobParametersBuilder();
        		para.addDate("date", new Date());
				jobLauncher.run(sampleJob, para.toJobParameters());
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
 
    }
}
