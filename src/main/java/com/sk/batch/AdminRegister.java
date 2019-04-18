package com.sk.batch;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.core.env.Environment;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class AdminRegister extends Thread {
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	private Environment env;
	private Job job;
	private JobScheduler scheduler;

	public AdminRegister(Environment env, Job job, JobScheduler scheduler){
		this.env = env;
		this.job = job;
		this.scheduler = scheduler;
	}

	public void run() {
		try {
		    Thread.sleep(5000);
			Encoder enc = Base64.getEncoder();
	
			StringBuffer buff = new StringBuffer(env.getProperty("meta.admin-url"));
			buff.append("?job=" + enc.encodeToString(job.getName().getBytes()));
			buff.append("&desc=" + enc.encodeToString("some job description".getBytes()));
			buff.append("&callback=" + enc.encodeToString(env.getProperty("meta.callback-url").getBytes()));
			buff.append("&cron=" + enc.encodeToString(env.getProperty("jobs.schedule").getBytes()));
			
			URL url;
			url = new URL(buff.toString());
			logger.info("#### BATCH ADMIN URL=" + url.toString());
			
			while(true) {
				try {
					HttpURLConnection conn;
					conn = (HttpURLConnection) url.openConnection();
					conn.setRequestMethod("GET");
			        conn.setConnectTimeout(1000);
				    conn.setReadTimeout(1000);
				    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
				    	String res = getResponse(conn);
				    	logger.info("#### BATCH ADMIN CONNECTED OK=" + res);
				    	JsonParser parser = new JsonParser();
				    	JsonElement element = parser.parse(res);
				    	String cron = element.getAsJsonObject().get("cron").getAsString();
				    	scheduler.setCron(cron);
				    	return;
				    }
				}
			    catch(Exception e) {
					logger.error("#### BATCH ADMIN URL ERROR", e);;
			    }
			    Thread.sleep(5000);
			}
		} catch (Exception e) {
			logger.error("#### BATCH ADMIN URL ERROR", e);;
		}
	}

	private String getResponse(HttpURLConnection conn) {
    	StringBuffer response = new StringBuffer(); 
		try {
	    	BufferedReader rd;
			rd = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	    	String line;
	    	while((line = rd.readLine()) != null) {
	    	 response.append(line);
	    	 response.append('\r');
	    	}
		   	rd.close();
		} catch (IOException e) {
			logger.error("#### BATCH ADMIN URL ERROR", e);;
		}
     	return response.toString();
	}
}
