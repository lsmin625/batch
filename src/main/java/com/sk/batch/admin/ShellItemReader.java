package com.sk.batch.admin;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class ShellItemReader implements ItemReader<String> {
	private int index = 0;
	private String script;
	
	public ShellItemReader(String script) {
		this.script = script;
	}
	

	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if(index == 0) {
			++index;
			return script;
		}
		else {
			index = 0;
			return null;
		}
	}
}
