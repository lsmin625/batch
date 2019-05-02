package com.sk.batch.jobs.job02.step1;

import org.springframework.batch.item.ItemProcessor;

import com.sk.batch.jobs.job01.data.UserJson;
import com.sk.batch.jobs.job01.data.UserXml;

public class DbToJsonProcessor<T1, T2> implements ItemProcessor<UserXml, UserJson> {

	@Override
	public UserJson process(UserXml item) throws Exception {
		UserJson userJson = new UserJson();
		userJson.setUserName(item.getUserName());
		userJson.setUserId(item.getUserId());
		userJson.setTransactionDate(item.getTransactionDate());
		userJson.setTransactionAmount(item.getTransactionAmount());
		userJson.setUpdatedDate(item.getUpdatedDate());
		userJson.setUserGroup("Guest");
        return userJson;
	}
}
