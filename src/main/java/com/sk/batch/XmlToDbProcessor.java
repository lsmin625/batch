package com.sk.batch;

import org.springframework.batch.item.ItemProcessor;

import com.sk.batch.UserXml;

public class XmlToDbProcessor<T1, T2> implements ItemProcessor<UserXml, UserXml> {

	@Override
	public UserXml process(UserXml item) throws Exception {
		return item;
	}

}
