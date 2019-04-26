package com.sk.batch.jobs.job01.step2;

import org.springframework.batch.item.ItemProcessor;

import com.sk.batch.jobs.job01.data.UserXml;

public class XmlToDbProcessor<T1, T2> implements ItemProcessor<UserXml, UserXml> {

	@Override
	public UserXml process(UserXml item) throws Exception {
		return item;
	}

}
