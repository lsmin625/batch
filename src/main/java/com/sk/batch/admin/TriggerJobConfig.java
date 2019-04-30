package com.sk.batch.admin;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.step.builder.SimpleStepBuilder;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import com.sk.batch.jobs.JobFinishedListener;

@Configuration 
@Import(AdminConfig.class)
public class TriggerJobConfig {
	public static String CRON_REGIST = "0 0/1 * * * ?";

	@Value("${meta.admin-url}")
	private String adminUrl;
	
	@Value("${meta.callback-url}")
	private String callbackUrl;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Autowired
    private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private JobFinishedListener jobFinishedListener;

	@Autowired
	private TriggerJobList triggerJobList;
	
 	@Bean @Qualifier("triggerJobInfo")
    public TriggerJobInfo triggerJobInfo() {
		TriggerJobInfo jobInfo = new TriggerJobInfo();
        jobInfo.setName("triggerRegistJob");
        jobInfo.setDesc("Registration and Hearbeat");
        jobInfo.setMode("self");
        jobInfo.setCron(CRON_REGIST);
        jobInfo.setAdminUrl(adminUrl);
        jobInfo.setCallbackUrl(callbackUrl);
        return jobInfo;
	}
 	
    @Bean @Qualifier("triggerReader")
    public ItemReader<TriggerJobInfo> triggerReader() {
    	TriggerItemReader reader = new TriggerItemReader(triggerJobList);
        return reader;
    }
 
    @Bean @Qualifier("triggerProcessor")
    public ItemProcessor<TriggerJobInfo, TriggerJobInfo> triggerProcessor(@Qualifier("triggerJobInfo") TriggerJobInfo triggerJobInfo) {
    	return new TriggerItemProcessor(triggerJobInfo);
    }

    @Bean @Qualifier("triggerWriter")
    public ItemWriter<TriggerJobInfo> triggerWriter() {
    	TriggerItemWriter writer = new TriggerItemWriter();
       	return writer;
    }
   
 	@Bean @Qualifier("triggerStep")
 	public Step triggerStep(@Qualifier("triggerReader") ItemReader<TriggerJobInfo> reader,
    		@Qualifier("triggerProcessor") ItemProcessor<TriggerJobInfo, TriggerJobInfo> processor, 
    		@Qualifier("triggerWriter") ItemWriter<TriggerJobInfo> writer) {

 		StepBuilder stepBuilder =  stepBuilderFactory.get("triggerStep");
        SimpleStepBuilder<TriggerJobInfo, TriggerJobInfo> simpleStepBuilder = stepBuilder.<TriggerJobInfo, TriggerJobInfo> chunk(1);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
	}
	

 	@Bean @Qualifier("triggerRegistJob")
    public Job triggerRegistJob(@Qualifier("triggerStep") Step step, @Qualifier("triggerJobInfo") TriggerJobInfo triggerJobInfo) {
		JobBuilder jobBuilder = jobBuilderFactory.get("triggerRegistJob");
		jobBuilder.incrementer(new RunIdIncrementer());
		jobBuilder.preventRestart();
		jobBuilder.listener(jobFinishedListener);
		
		JobFlowBuilder jobFlowBuilder = jobBuilder.flow(step);
		jobFlowBuilder.end();

		FlowJobBuilder flowJobBuilder = jobFlowBuilder.build();
		Job job = flowJobBuilder.build();
		triggerJobInfo.setJob(job);
		
		return job;
	}
 	
}
