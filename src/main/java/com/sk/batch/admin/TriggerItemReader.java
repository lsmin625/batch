package com.sk.batch.admin;

import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class TriggerItemReader implements ItemReader<TriggerJobInfo> {
	private int index = 0;
	
	private List<TriggerJobInfo> list;
	
	public TriggerItemReader(List<TriggerJobInfo> list) {
		this.list = list;
	}
	

	@Override
	public TriggerJobInfo read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		if(index < list.size()) {
			return list.get(index++);
		}
		else {
			index = 0;
			return null;
		}
	}
}
