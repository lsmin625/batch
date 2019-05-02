package com.sk.batch.admin;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Base64;
import java.util.Base64.Encoder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class TriggerItemProcessor implements ItemProcessor<TriggerJobInfo, TriggerJobInfo> {
	private Logger logger = LoggerFactory.getLogger(TriggerItemProcessor.class);
	private static Encoder Enc = Base64.getEncoder();
	private TriggerJobInfo triggerJobInfo;
	
	public TriggerItemProcessor(TriggerJobInfo jobInfo) {
		this.triggerJobInfo = jobInfo;
	}

	@Override
	public TriggerJobInfo process(TriggerJobInfo jobInfo) throws Exception {
    	jobInfo.setRegistered(false);

    	StringBuffer buff = new StringBuffer(jobInfo.getAdminUrl());
    	if(triggerJobInfo.isRegistered()) {
        	buff.append("/heartbeat");
    	}
    	else {
        	buff.append("/regist");
    	}
		buff.append("?job=" + Enc.encodeToString(jobInfo.getName().getBytes()));
		buff.append("&desc=" + Enc.encodeToString(jobInfo.getDesc().getBytes()));
		buff.append("&mode=" + Enc.encodeToString(jobInfo.getMode().getBytes()));
		buff.append("&cron=" + Enc.encodeToString(jobInfo.getCron().getBytes()));
		buff.append("&callback=" + Enc.encodeToString(jobInfo.getCallbackUrl().getBytes()));

		logger.info("@@@@ REGIST TO ADMIN URL=" + buff.toString());
		HttpURLConnection conn = null;
		try {
			URL url = new URL(buff.toString());
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
	        conn.setConnectTimeout(1000);
		    conn.setReadTimeout(1000);
		    if(conn.getResponseCode() == HttpURLConnection.HTTP_OK) {
		    	jobInfo.setRegistered(true);
				logger.info("@@@@ REGIST SUCCESS JOB=" + jobInfo.toString());
		    }
		}
		catch(Exception e) {
			logger.info("@@@@ REGIST TO ADMIN TIMEOUT");
		}
		finally {
			if(conn != null) {
				conn.disconnect();
			}
		}
    	return jobInfo;
	}
}
