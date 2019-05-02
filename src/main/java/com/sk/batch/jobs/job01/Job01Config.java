package com.sk.batch.jobs.job01;

import java.net.MalformedURLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.xml.StaxEventItemReader;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sk.batch.admin.AdminConfig;
import com.sk.batch.admin.JobFinishedListener;
import com.sk.batch.admin.TriggerJobInfo;
import com.sk.batch.admin.TriggerJobList;
import com.sk.batch.jobs.job01.data.User;
import com.sk.batch.jobs.job01.data.UserXml;
import com.sk.batch.jobs.job01.step1.CsvToXmlProcessor;
import com.sk.batch.jobs.job01.step1.UserFieldSetMapper;
import com.sk.batch.jobs.job01.step2.UserPrepareStatementSetter;
import com.sk.batch.jobs.job01.step2.XmlToDbProcessor;


@Configuration 
@Import(AdminConfig.class)
public class Job01Config {
	private Logger logger = LoggerFactory.getLogger(Job01Config.class);
	
	@Autowired private Environment env;
	@Autowired private StepBuilderFactory stepBuilderFactory;
	@Autowired private JobBuilderFactory jobBuilderFactory;
	@Autowired private JobFinishedListener jobFinishedListener;
	@Autowired private TriggerJobList triggerJobList;

	@Value("${meta.admin-url}") private String adminUrl;
	@Value("${meta.callback-url}") private String callbackUrl;

	@Value("${job01.name}") private String jobName;
	@Value("${job01.desc}") private String jobDesc;
	@Value("${job01.mode}") private String jobMode;
	@Value("${job01.cron}") private String jobCron;

	@Value("file:${job01.file.step1-input}") private Resource step1Input;
    @Value("file:${job01.file.step1-output}") private Resource step1Output;
    @Value("file:${job01.file.step2-schema}") private Resource step2Schema;
    private boolean needsSchema = false;

    
    @Bean @Qualifier("jobDataSource")
    public DataSource jobDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("job.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("job.datasource.url"));
        dataSource.setUsername(env.getProperty("job.datasource.username"));
        dataSource.setPassword(env.getProperty("job.datasource.password"));
        return dataSource;
    }

    @Bean @Qualifier("jobDataSourceInitializer")
    public DataSourceInitializer job01DataSourceInitializer(@Qualifier("jobDataSource") DataSource dataSource) throws MalformedURLException {
        DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        if(needsSchema) {
	        ResourceDatabasePopulator databasePopulator = new ResourceDatabasePopulator();
	        databasePopulator.addScript(step2Schema);
	        databasePopulator.setIgnoreFailedDrops(true);
        	initializer.setDatabasePopulator(databasePopulator);
        	logger.info("#### TABLE CREATED SCHEMA-FILE=" + step2Schema.getFilename());
        }
        return initializer;
    }
    
    @Bean @Qualifier("jobJdbcTemplate")
    public NamedParameterJdbcTemplate jobJdbcTemplate(@Qualifier("jobDataSource") DataSource dataSource) {
       	NamedParameterJdbcTemplate jobJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    	return jobJdbcTemplate;
    }

    @Bean @Qualifier("job01Step1Reader")
    public ItemReader<User> job01Step1Reader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();  //default delimiter is comma(','); if any other, use .setDelimiter('')
        tokenizer.setNames(new String[]{"userName", "userId", "transactionDate", "transactionAmount"});
        
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new UserFieldSetMapper());

        FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
        reader.setLineMapper(lineMapper);
        reader.setResource(step1Input);
        reader.setLinesToSkip(1);
        return reader;
    }

    @Bean @Qualifier("job01Step1Processor")
    public ItemProcessor<User, UserXml> job01Step1Processor() {
        return new CsvToXmlProcessor();
    }
 
    @Bean @Qualifier("job01Step1Writer")
    public ItemWriter<UserXml> job01Step1Writer() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[] { UserXml.class });

        StaxEventItemWriter<UserXml> writer = new StaxEventItemWriter<UserXml>();
        writer.setMarshaller(marshaller);
        writer.setRootTagName("userlist");
        writer.setResource(step1Output);
        return writer;
    }
 
    @Bean @Qualifier("job01Step1")
    protected Step job01Setp1(@Qualifier("job01Step1Reader") ItemReader<User> reader, 
    		@Qualifier("job01Step1Processor") ItemProcessor<User, UserXml> processor, 
    		@Qualifier("job01Step1Writer") ItemWriter<UserXml> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("job01Step1");
        SimpleStepBuilder<User, UserXml> simpleStepBuilder = stepBuilder.<User, UserXml> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }
 
    @Bean @Qualifier("job01Step2Reader")
    public ItemReader<UserXml> job01Step2Reader() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[] { UserXml.class });

        StaxEventItemReader<UserXml> reader = new StaxEventItemReader<UserXml>();
    	reader.setResource(step1Output);
    	reader.setFragmentRootElementName("user");
    	reader.setUnmarshaller(marshaller);
        return reader;
    }

    @Bean @Qualifier("job01Step2Processor")
    public ItemProcessor<UserXml, UserXml> job01Step2Processor() {
        return new XmlToDbProcessor<UserXml, UserXml>();
    }
 
    @Bean @Qualifier("job01Step2Writer")
    public ItemWriter<UserXml> job01Step2Writer(@Qualifier("jobDataSource") DataSource dataSource, 
    		@Qualifier("jobJdbcTemplate") NamedParameterJdbcTemplate jobJdbcTemplate) {
       	JdbcBatchItemWriter<UserXml> writer = new JdbcBatchItemWriter<UserXml>();
       	StringBuffer sql = new StringBuffer();
       	sql.append("INSERT INTO user (user_id, user_name, transaction_date, transaction_amount, updated_date)");
       	sql.append(" VALUES (?, ?, ?, ?, ?)");
       	sql.append(" ON CONFLICT(user_id) DO UPDATE SET user_id=?;");
    	writer.setDataSource(dataSource);
 		writer.setJdbcTemplate(jobJdbcTemplate);
    	writer.setSql(sql.toString());
    	writer.setItemPreparedStatementSetter(new UserPrepareStatementSetter());
    	return writer;
    }
 
    @Bean @Qualifier("job01Step2")
    protected Step job01Setp2(@Qualifier("job01Step2Reader") ItemReader<UserXml> reader, 
    		@Qualifier("job01Step2Processor") ItemProcessor<UserXml, UserXml> processor, 
    		@Qualifier("job01Step2Writer") ItemWriter<UserXml> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("job01Step2");
        SimpleStepBuilder<UserXml, UserXml> simpleStepBuilder = stepBuilder.<UserXml, UserXml> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }

 	@Bean @Qualifier("job01")
    public Job job01(@Qualifier("job01Step1") Step step1, @Qualifier("job01Step2") Step step2) {

 		JobBuilder jobBuilder = jobBuilderFactory.get(jobName);
        jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder.preventRestart();
        jobBuilder.listener(jobFinishedListener);

        JobFlowBuilder jobFlowBuilder = jobBuilder.flow(step1);
        jobFlowBuilder.next(step2);
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