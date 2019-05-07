package com.sk.batch.admin;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.ItemProcessor;

public class ShellItemProcessor implements ItemProcessor<String, String> {
	private Logger logger = LoggerFactory.getLogger(ShellItemProcessor.class);

	@Override
	public String process(String script) throws Exception {
		logger.info("$$$$ SHELL STARTING SCRIPT=" + script);
		BufferedReader reader = null;
		Process proc = null;
		proc = Runtime.getRuntime().exec(script);
		reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		(proc.getOutputStream()).close();
		String line;
		while((line = reader.readLine()) != null) {
			if(!line.equals("")) {
				logger.info("$$$$ SHELL EXEC READ=" + line);
			}
		}
		try {
			if(reader != null) {
				reader.close();
			}
			if(proc != null) {
				(proc.getErrorStream()).close();
				proc.destroy();
			}
		}
		catch(Exception e) {
			logger.info("$$$$ SHELL CLOSE ERROR SCRIPT=" + script, e);
		}
		logger.info("$$$$ SHELL FINISH SCRIPT=" + script);
		return null;
	}
}
