package com.sk.batch.admin;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.sql.DataSource;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

@Configuration
public class AdminConfig implements EnvironmentAware {

	@Autowired
	private Environment env;

    @Override
    public void setEnvironment(final Environment env) {
        this.env = env;
    }

    @Bean(destroyMethod="shutdown")
    public Executor taskExecutor() {
        return Executors.newScheduledThreadPool(42);
    }

    @Bean
    public TriggerJobList triggerJobList() {
    	TriggerJobList triggerJobList = new TriggerJobList();
        return triggerJobList;
    }

    @Bean
    public JobFinishedListener jobFinishedListener() {
        return new JobFinishedListener();
    }

    @Bean
	public StandardPBEStringEncryptor jasyptEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();  
        encryptor.setPassword(env.getProperty("jasypt.encryptor.secret"));  
        encryptor.setAlgorithm("PBEWITHMD5ANDDES");  
        return encryptor;
    }

    @Bean @Qualifier("metaDataSource")
    public DataSource dataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("meta.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("meta.datasource.url"));
        dataSource.setUsername(env.getProperty("meta.datasource.username"));
        dataSource.setPassword(env.getProperty("meta.datasource.password"));
        return dataSource;
    }
/*
    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("metaDataSource") DataSource dataSource) {
    	DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        return transactionManager;
    }
    
     @Bean
    public JobRepository jobRepository(@Qualifier("metaDataSource") DataSource dataSource, 
    		PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return (JobRepository) factory.getObject();
    }

    @Bean
    public JobLauncher jobLauncher(JobRepository jobRepository) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(jobRepository);
		jobLauncher.setTaskExecutor(new SimpleAsyncTaskExecutor());
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }
*/
    
}