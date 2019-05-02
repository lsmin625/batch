package com.sk.batch.jobs.job02;

import javax.sql.DataSource;
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
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.Resource;
import com.sk.batch.admin.JobFinishedListener;
import com.sk.batch.admin.TriggerJobInfo;
import com.sk.batch.admin.TriggerJobList;
import com.sk.batch.jobs.job01.Job01Config;
import com.sk.batch.jobs.job01.data.UserJson;
import com.sk.batch.jobs.job01.data.UserXml;
import com.sk.batch.jobs.job02.step1.DbToJsonProcessor;
import com.sk.batch.jobs.job02.step1.UserRowMapper;


@Configuration 
@Import(Job01Config.class)
public class Job02Config {
	
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private JobFinishedListener jobFinishedListener;
	@Autowired private TriggerJobList triggerJobList;

	@Value("${meta.admin-url}") private String adminUrl;
	@Value("${meta.callback-url}") private String callbackUrl;

	@Value("${job02.name}") private String jobName;
	@Value("${job02.desc}") private String jobDesc;
	@Value("${job02.mode}") private String jobMode;
	@Value("${job02.cron}") private String jobCron;
	@Value("file:${job02.file.step1-output}") private Resource step1Output;

    @Bean @Qualifier("job02Step1Reader")
    public ItemReader<UserXml> job02Step1Reader(@Qualifier("jobDataSource") DataSource jobDataSource) {
    	JdbcCursorItemReader<UserXml> reader = new JdbcCursorItemReader<UserXml>();
        reader.setDataSource(jobDataSource);
        reader.setRowMapper(new UserRowMapper());
        reader.setSql("SELECT * FROM user");
        return reader;
    }
 
    @Bean @Qualifier("job02Step1Processor")
    public ItemProcessor<UserXml, UserJson> job02Step1Processor() {
        return new DbToJsonProcessor<UserXml, UserJson>();
    }
 
    @Bean @Qualifier("job02Step1Writer")
    public ItemWriter<UserJson> job02Step1Writer() {
        JsonFileItemWriter<UserJson> writer = new JsonFileItemWriter<UserJson>(step1Output, new JacksonJsonObjectMarshaller<UserJson>());
       	return writer;
    }

    @Bean @Qualifier("job02Step1")
    protected Step job02Step1(@Qualifier("job02Step1Reader") ItemReader<UserXml> reader, 
    		@Qualifier("job02Step1Processor") ItemProcessor<UserXml, UserJson> processor, 
    		@Qualifier("job02Step1Writer") ItemWriter<UserJson> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("job02Step1");
        SimpleStepBuilder<UserXml, UserJson> simpleStepBuilder = stepBuilder.<UserXml, UserJson> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }

 	@Bean @Qualifier("job02")
    public Job job02(@Qualifier("job02Step1") Step step1) {

 		JobBuilder jobBuilder = jobBuilderFactory.get(jobName);
        jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder.preventRestart();
        jobBuilder.listener(jobFinishedListener);

        JobFlowBuilder jobFlowBuilder = jobBuilder.flow(step1);
        jobFlowBuilder.end();
        
        FlowJobBuilder flowJobBuilder = jobFlowBuilder.build();
        Job job = flowJobBuilder.build();

        TriggerJobInfo jobInfo = new TriggerJobInfo();
        jobInfo.setName(job.getName());
        jobInfo.setDesc(jobDesc);
        jobInfo.setMode(jobMode);
        jobInfo.setCron(jobCron);
        jobInfo.setAdminUrl(adminUrl);
        jobInfo.setCallbackUrl(callbackUrl);
        jobInfo.setJob(job);
        triggerJobList.add(jobInfo);
        
        return job;
    }
 }