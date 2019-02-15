package com.sk.batch.jobs.sample.step;

import javax.sql.DataSource;

import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
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
import org.springframework.core.io.Resource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.oxm.jaxb.Jaxb2Marshaller;

import com.sk.batch.jobs.sample.data.User;
import com.sk.batch.jobs.sample.data.UserJson;
import com.sk.batch.jobs.sample.data.UserXml;

@Configuration
public class StepConfig {
	
    @Value("file:${jobs.file.input-csv}")
    private Resource inputCsv;
 
    @Value("file:${jobs.file.output-xml}")
    private Resource outputXml;

    @Value("file:${jobs.file.output-json}")
    private Resource outputJson;

	@Autowired
	StepBuilderFactory stepBuilderFactory;
  
    @Bean @Qualifier("step1Reader")
    public ItemReader<User> step1Reader() {
        DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer();  //default delimiter is comma(','); if any other, use .setDelimiter('')
        tokenizer.setNames(new String[]{"userName", "userId", "transactionDate", "transactionAmount"});
        
        DefaultLineMapper<User> lineMapper = new DefaultLineMapper<User>();
        lineMapper.setLineTokenizer(tokenizer);
        lineMapper.setFieldSetMapper(new UserFieldSetMapper());

        FlatFileItemReader<User> reader = new FlatFileItemReader<User>();
        reader.setLineMapper(lineMapper);
        reader.setResource(inputCsv);
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
        writer.setResource(outputXml);
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
    	reader.setResource(outputXml);
    	reader.setFragmentRootElementName("user");
    	reader.setUnmarshaller(marshaller);
        return reader;
    }

    @Bean @Qualifier("step2Processor")
    public ItemProcessor<UserXml, UserXml> step2Processor() {
        return new XmlToDbProcessor<UserXml, UserXml>();
    }
 
    @Bean @Qualifier("step2Writer")
    public ItemWriter<UserXml> step2Writer(@Qualifier("jobDataSource") DataSource dataSource, @Qualifier("jobJdbcTemplate") NamedParameterJdbcTemplate jobJdbcTemplate) {
       	JdbcBatchItemWriter<UserXml> writer = new JdbcBatchItemWriter<UserXml>();
    	writer.setDataSource(dataSource);
 		writer.setJdbcTemplate(jobJdbcTemplate);
    	writer.setSql("INSERT INTO user (user_id, user_name, transaction_date, transaction_amount, updated_date) VALUES (?, ?, ?, ?, ?)");
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
    public ItemReader<UserXml> step3Reader(@Qualifier("jobDataSource") DataSource dataSource, @Qualifier("jobJdbcTemplate") NamedParameterJdbcTemplate jobJdbcTemplate) {
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
        JsonFileItemWriter<UserJson> writer = new JsonFileItemWriter<UserJson>(outputJson, new JacksonJsonObjectMarshaller<UserJson>());
       	return writer;
    }

    @Bean @Qualifier("setp3")
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

}
