package com.sk.batch.admin;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminRegister implements CommandLineRunner {
	private Logger logger = LoggerFactory.getLogger(AdminRegister.class);
	public static String CRON_REGIST = "0 0/1 * * * ?";
	public static String CRON_HEARTBEAT = "0 0 7 * * ?";
	
	@Autowired private JobScheduler scheduler;
	@Autowired private TriggerJobList triggerJobList;
	@Autowired private TriggerJobInfo triggerJobInfo;
	@Autowired private TriggerFinishedListener triggerFinishedListener;
	
	@Override
	public void run(String... args) throws Exception {
		try {
		    Thread.sleep(3000);
		    triggerJobInfo.setCron(CRON_REGIST);
			scheduler.setCron(triggerJobInfo, CRON_REGIST);
			triggerFinishedListener.setProperties(scheduler, triggerJobInfo, CRON_HEARTBEAT);
			
    		for(TriggerJobInfo jobInfo : triggerJobList) {
				logger.info("#### LIST-JOB " + jobInfo.toString());
				if(jobInfo.getMode().equalsIgnoreCase("self")) {
			    	scheduler.setCron(jobInfo, jobInfo.getCron());
				}
			}

		} catch (Exception e) {
			logger.error("#### THREAD.sleep ERROR", e);;
		}
		return;
	}

/*	

	@Override
	public void run(String... args) throws Exception {
		try {
		    Thread.sleep(5000);
			setSelfCron();
			
			while(notRegistered) {
				Enumeration<String> keys = triggerJobList.keys();
				while(keys.hasMoreElements()) {
					String job = keys.nextElement();
					TriggerJobInfo jobInfo = triggerJobList.get(job);
					if(registToAdmin(jobInfo)) {
					    notRegistered = false;
				    }
				    Thread.sleep(1000);
				}
			    Thread.sleep(30000);
			}
		} catch (Exception e) {
			logger.error("#### BATCH ADMIN URL ERROR", e);;
		}
	}
private boolean registToAdmin(TriggerJobInfo jobInfo) {
		try {
			URL url = new URL(getParameter(jobInfo));
			logger.info("#### BATCH ADMIN URL=" + url.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
	        conn.setConnectTimeout(1000);
		    conn.setReadTimeout(1000);
		    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	String res = getResponse(conn);
		    	logger.info("#### BATCH ADMIN CONNECTED OK=" + res);
		    	JsonParser parser = new JsonParser();
		    	JsonElement element = parser.parse(res);
		    	String cron = element.getAsJsonObject().get("cron").getAsString();
		    	scheduler.setCron(jobInfo, cron);
			    return true;
		    }
		}
	    catch(Exception e) {
			logger.error("#### BATCH ADMIN URL TIMEOUT");;
	    }
		return false;
	}

	private void setSelfCron() {
		Enumeration<String> keys = triggerJobList.keys();
		while(keys.hasMoreElements()) {
			String job = keys.nextElement();
			TriggerJobInfo jobInfo = triggerJobList.get(job);
			if(jobInfo.getMode().equalsIgnoreCase("self")) {
		    	scheduler.setCron(jobInfo, jobInfo.getCron());
			}
		}
	}
	
	private String getParameter(TriggerJobInfo jobInfo) {
		StringBuffer buff = new StringBuffer(jobInfo.getAdminUrl());
		buff.append("?job=" + Enc.encodeToString(jobInfo.getName().getBytes()));
		buff.append("&desc=" + Enc.encodeToString(jobInfo.getDesc().getBytes()));
		buff.append("&mode=" + Enc.encodeToString(jobInfo.getMode().getBytes()));
		buff.append("&cron=" + Enc.encodeToString(jobInfo.getCron().getBytes()));
		buff.append("&callback=" + Enc.encodeToString(jobInfo.getCallbackUrl().getBytes()));
		
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
*/
}
