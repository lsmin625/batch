package com.sk.batch.jobs.sample;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.job.builder.FlowJobBuilder;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.job.builder.JobFlowBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import com.sk.batch.jobs.sample.common.MetaConfig;
import com.sk.batch.jobs.sample.step.StepConfig;

import org.springframework.context.annotation.Bean;

import java.net.MalformedURLException;

import javax.sql.DataSource;

@Import({MetaConfig.class, StepConfig.class})
@Configuration
@EnableBatchProcessing
public class JobConfig {

	@Autowired
	private Environment env;

	@Autowired @Qualifier("metaJobBuilderFactory")
    private JobBuilderFactory jobBuilderFactory;

	@Value("file:${jobs.datasource.populator}")
	private Resource usersSchema;

    @Bean @Qualifier("jobDataSource")
    public DataSource jobDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jobs.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("jobs.datasource.url"));
        dataSource.setUsername(env.getProperty("jobs.datasource.username"));
        dataSource.setPassword(env.getProperty("jobs.datasource.password"));
        return dataSource;
    }

  ///*
    @Bean @Qualifier("jobDataSourceInitializer")
    public DataSourceInitializer jobDataSourceInitializer(@Qualifier("jobDataSource") DataSource dataSource) throws MalformedURLException {
        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
        databasePopulator.addScript(usersSchema);
        databasePopulator.setIgnoreFailedDrops(true);
 
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        initializer.setDatabasePopulator(databasePopulator);
 
        return initializer;
    }
//*/

    @Bean @Qualifier("jobJdbcTemplate")
    public NamedParameterJdbcTemplate jobJdbcTemplate(@Qualifier("jobDataSource") DataSource dataSource) {
       	NamedParameterJdbcTemplate jobJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    	return jobJdbcTemplate;
    }

	@Bean @Qualifier("sampleBatchJob")
    public Job sampleBatchJob(JobFinishedListener listener, @Qualifier("step1") Step step1, @Qualifier("step2") Step step2, @Qualifier("step3") Step step3) {
        JobBuilder jobBuilder = jobBuilderFactory.get("sampleBatchJob");
        jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder.listener(listener);

        JobFlowBuilder jobFlowBuilder = jobBuilder.flow(step1);
        jobFlowBuilder.next(step2);
        jobFlowBuilder.next(step3);
        jobFlowBuilder.end();
        
        FlowJobBuilder flowJobBuilder = jobFlowBuilder.build(); 
        return flowJobBuilder.build();
    }

}