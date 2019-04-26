package com.sk.batch.admin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Enumeration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.sk.batch.jobs.JobController;
import com.sk.batch.jobs.JobScheduler;

@Component
public class AdminRegister implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(JobController.class);
	
	@Autowired
	private JobScheduler scheduler;

	@Autowired
	private TriggerJobList jobList;
	
	private boolean notRegistered = true;

	@Override
	public void run(String... args) throws Exception {
		try {
		    Thread.sleep(5000);
			setSelfCron();
			
			while(notRegistered) {
				try {
					Enumeration<String> keys = jobList.keys();
					while(keys.hasMoreElements()) {
						String job = keys.nextElement();
						TriggerJobInfo jobInfo = jobList.get(job);
						
						URL url = new URL(getParameter(jobInfo));
						logger.info("#### BATCH ADMIN URL=" + url.toString());
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
					    	scheduler.setCron(jobInfo.getJob(), cron);
						    notRegistered = false;
						    Thread.sleep(1000);
					    }
					}
				}
			    catch(Exception e) {
					logger.error("#### BATCH ADMIN URL TIMEOUT");;
			    }
			    Thread.sleep(5000);
			}
		} catch (Exception e) {
			logger.error("#### BATCH ADMIN URL ERROR", e);;
		}
	}
	private void setSelfCron() {
		Enumeration<String> keys = jobList.keys();
		while(keys.hasMoreElements()) {
			String job = keys.nextElement();
			TriggerJobInfo jobInfo = jobList.get(job);
	    	scheduler.setCron(jobInfo.getJob(), jobInfo.getCron());
		}
	}
	
	private String getParameter(TriggerJobInfo jobInfo) {
		Encoder enc = Base64.getEncoder();

		StringBuffer buff = new StringBuffer(jobInfo.getAdminUrl());
		buff.append("?job=" + enc.encodeToString(jobInfo.getName().getBytes()));
		buff.append("&desc=" + enc.encodeToString(jobInfo.getDesc().getBytes()));
		buff.append("&callback=" + enc.encodeToString(jobInfo.getCallbackUrl().getBytes()));
		buff.append("&cron=" + enc.encodeToString(jobInfo.getCron().getBytes()));
		
		return buff.toString();
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
