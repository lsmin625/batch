package com.sk.batch;

import javax.sql.DataSource;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.launch.support.SimpleJobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.repository.support.JobRepositoryFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
public class MetaConfig {

	@Autowired
	private Environment env;

	@Bean
	public StandardPBEStringEncryptor jasyptEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();  
        encryptor.setPassword(env.getProperty("jasypt.encryptor.secret"));  
        encryptor.setAlgorithm("PBEWITHMD5ANDDES");  
        return encryptor;
    }

    @Bean @Primary @Qualifier("metaDataSource")
    public DataSource metaDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("meta.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("meta.datasource.url"));
        dataSource.setUsername(env.getProperty("meta.datasource.username"));
        dataSource.setPassword(env.getProperty("meta.datasource.password"));
        return dataSource;
    }

    @Bean @Qualifier("metaTransactionManager")
    public PlatformTransactionManager metaTransactionManager(@Qualifier("metaDataSource") DataSource dataSource) {
    	DataSourceTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
        return transactionManager;
    }

    @Bean @Qualifier("metaJobRepository")
    public JobRepository metaJobRepository(@Qualifier("metaDataSource") DataSource dataSource, 
    		@Qualifier("metaTransactionManager") PlatformTransactionManager transactionManager) throws Exception {
        JobRepositoryFactoryBean factory = new JobRepositoryFactoryBean();
        factory.setDataSource(dataSource);
        factory.setTransactionManager(transactionManager);
        factory.afterPropertiesSet();
        return (JobRepository) factory.getObject();
    }

    @Bean @Qualifier("metaJobLauncher")
    public JobLauncher metaJobLauncher(@Qualifier("metaJobRepository") JobRepository metaJobRepository) throws Exception {
        SimpleJobLauncher jobLauncher = new SimpleJobLauncher();
        jobLauncher.setJobRepository(metaJobRepository);
        jobLauncher.afterPropertiesSet();
        return jobLauncher;
    }

    @Bean @Qualifier("metaJobBuilderFactory")
    public JobBuilderFactory metaJobBuilderFactory(@Qualifier("metaJobRepository") JobRepository metaJobRepository) throws Exception {
        return new JobBuilderFactory(metaJobRepository);
    }
}