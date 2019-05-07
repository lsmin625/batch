package com.sk.batch.admin;

import java.util.List;

import org.springframework.batch.item.ItemWriter;

public class ShellItemWriter implements ItemWriter<String> {

	@Override
	public void write(List<? extends String> items) throws Exception {
		// do nothing
	}

}
