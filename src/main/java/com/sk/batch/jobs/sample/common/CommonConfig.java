package com.sk.batch.jobs.sample.common;

import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration
public class CommonConfig {

	@Autowired
	private Environment env;

	@Bean
	public StandardPBEStringEncryptor jasyptEncryptor() {
        StandardPBEStringEncryptor encryptor = new StandardPBEStringEncryptor();  
        encryptor.setPassword(env.getProperty("jasypt.encryptor.secret"));  
        encryptor.setAlgorithm("PBEWITHMD5ANDDES");  
        return encryptor;
    }
	
}
