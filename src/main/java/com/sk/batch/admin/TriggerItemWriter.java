package com.sk.batch.admin;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class TriggerItemWriter implements ItemWriter<TriggerJobInfo> {

	@Override
	public void write(List<? extends TriggerJobInfo> items) throws Exception {
		// do nothing
	}

}
