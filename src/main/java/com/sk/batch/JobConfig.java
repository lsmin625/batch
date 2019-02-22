package com.sk.batch;

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
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.JdbcCursorItemReader;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.batch.item.json.JacksonJsonObjectMarshaller;
import org.springframework.batch.item.json.JsonFileItemWriter;
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
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sk.batch.step.CsvToXmlProcessor;
import com.sk.batch.step.User;
import com.sk.batch.step.UserFieldSetMapper;
import com.sk.batch.step.UserJson;
import com.sk.batch.step.UserPrepareStatementSetter;
import com.sk.batch.step.UserRowMapper;
import com.sk.batch.step.UserXml;
import com.sk.batch.step.XmlToDbProcessor;
import com.sk.batch.step.XmlToJsonProcessor;

@Configuration @Import(MetaConfig.class)
public class JobConfig {
	public static final String SCHEDULE = "0 0/10 * * * ?";
	public static final String DATEFORMAT = "yyyy-MM-dd HH:mm:00";
	
	@Autowired
	private Environment env;

    @Value("file:${jobs.file.step1-input}")
    private Resource inputStep1;
 
    @Value("file:${jobs.file.step1-output}")
    private Resource outputStep1;

    @Value("file:${jobs.file.step3-output}")
    private Resource outputStep3;

	@Autowired
	StepBuilderFactory stepBuilderFactory;

	@Autowired @Qualifier("metaJobBuilderFactory")
    private JobBuilderFactory jobBuilderFactory;

	@Autowired
	private JobFinishedListener jobListener;
	
    @Bean @Qualifier("jobDataSource")
    public DataSource jobDataSource() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName(env.getProperty("jobs.datasource.driver-class-name"));
        dataSource.setUrl(env.getProperty("jobs.datasource.url"));
        dataSource.setUsername(env.getProperty("jobs.datasource.username"));
        dataSource.setPassword(env.getProperty("jobs.datasource.password"));
        return dataSource;
    }

    @Bean @Qualifier("jobJdbcTemplate")
    public NamedParameterJdbcTemplate jobJdbcTemplate(@Qualifier("jobDataSource") DataSource dataSource) {
       	NamedParameterJdbcTemplate jobJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    	return jobJdbcTemplate;
    }

    @Bean @Qualifier("step1Reader")
    public ItemReader<User> step1Reader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();  //default delimiter is comma(','); if any other, use .setDelimiter('')
        tokenizer.setNames(new String[]{"userName", "userId", "transactionDate", "transactionAmount"});
        
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new UserFieldSetMapper());

        FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
        reader.setLineMapper(lineMapper);
        reader.setResource(inputStep1);
        reader.setLinesToSkip(1);
        return reader;
    }

    @Bean @Qualifier("step1Processor")
    public ItemProcessor<User, UserXml> step1Processor() {
        return new CsvToXmlProcessor();
    }
 
    @Bean @Qualifier("step1Writer")
    public ItemWriter<UserXml> step1Writer() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[] { UserXml.class });

        StaxEventItemWriter<UserXml> writer = new StaxEventItemWriter<UserXml>();
        writer.setMarshaller(marshaller);
        writer.setRootTagName("userlist");
        writer.setResource(outputStep1);
        return writer;
    }
 
    @Bean @Qualifier("setp1")
    protected Step step1(@Qualifier("step1Reader") ItemReader<User> reader, 
    		@Qualifier("step1Processor") ItemProcessor<User, UserXml> processor, 
    		@Qualifier("step1Writer") ItemWriter<UserXml> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("step1");
        SimpleStepBuilder<User, UserXml> simpleStepBuilder = stepBuilder.<User, UserXml> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }
 
    @Bean @Qualifier("step2Reader")
    public ItemReader<UserXml> step2Reader() {
        Jaxb2Marshaller marshaller = new Jaxb2Marshaller();
        marshaller.setClassesToBeBound(new Class[] { UserXml.class });

        StaxEventItemReader<UserXml> reader = new StaxEventItemReader<UserXml>();
    	reader.setResource(outputStep1);
    	reader.setFragmentRootElementName("user");
    	reader.setUnmarshaller(marshaller);
        return reader;
    }

    @Bean @Qualifier("step2Processor")
    public ItemProcessor<UserXml, UserXml> step2Processor() {
        return new XmlToDbProcessor<UserXml, UserXml>();
    }
 
    @Bean @Qualifier("step2Writer")
    public ItemWriter<UserXml> step2Writer(@Qualifier("jobDataSource") DataSource dataSource, 
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
 
    @Bean @Qualifier("setp2")
    protected Step step2(@Qualifier("step2Reader") ItemReader<UserXml> reader, 
    		@Qualifier("step2Processor") ItemProcessor<UserXml, UserXml> processor, 
    		@Qualifier("step2Writer") ItemWriter<UserXml> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("step2");
        SimpleStepBuilder<UserXml, UserXml> simpleStepBuilder = stepBuilder.<UserXml, UserXml> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }

    @Bean @Qualifier("step3Reader")
    public ItemReader<UserXml> step3Reader(@Qualifier("jobDataSource") DataSource dataSource, 
    		@Qualifier("jobJdbcTemplate") NamedParameterJdbcTemplate jobJdbcTemplate) {
    	JdbcCursorItemReader<UserXml> reader = new JdbcCursorItemReader<UserXml>();
        reader.setDataSource(dataSource);
        reader.setRowMapper(new UserRowMapper());
        reader.setSql("SELECT * FROM user");
        return reader;
    }
 
    @Bean @Qualifier("step3Processor")
    public ItemProcessor<UserXml, UserJson> step3Processor() {
        return new XmlToJsonProcessor();
    }
 
    @Bean @Qualifier("step3Writer")
    public ItemWriter<UserJson> step3Writer() {
        JsonFileItemWriter<UserJson> writer = new JsonFileItemWriter<UserJson>(outputStep3, new JacksonJsonObjectMarshaller<UserJson>());
       	return writer;
    }

    @Bean @Qualifier("step3")
    protected Step step3(@Qualifier("step3Reader") ItemReader<UserXml> reader, 
    		@Qualifier("step3Processor") ItemProcessor<UserXml, UserJson> processor, 
    		@Qualifier("step3Writer") ItemWriter<UserJson> writer) {

    	StepBuilder stepBuilder =  stepBuilderFactory.get("step3");
        SimpleStepBuilder<UserXml, UserJson> simpleStepBuilder = stepBuilder.<UserXml, UserJson> chunk(10);
        simpleStepBuilder.reader(reader);
        simpleStepBuilder.processor(processor);
        simpleStepBuilder.writer(writer);
        return simpleStepBuilder.build();
    }

 	@Bean @Qualifier("sampleBatchJob")
    public Job sampleBatchJob(@Qualifier("step1") Step step1, 
    		@Qualifier("step2") Step step2, @Qualifier("step3") Step step3) {

 		JobBuilder jobBuilder = jobBuilderFactory.get("sampleBatchJob");
        jobBuilder.incrementer(new RunIdIncrementer());
        jobBuilder.preventRestart();
        jobBuilder.listener(jobListener);

        JobFlowBuilder jobFlowBuilder = jobBuilder.flow(step1);
        jobFlowBuilder.next(step2);
        jobFlowBuilder.next(step3);
        jobFlowBuilder.end();
        
        FlowJobBuilder flowJobBuilder = jobFlowBuilder.build();
        return flowJobBuilder.build();
    }
 }
