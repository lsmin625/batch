package com.sk.batch.jobs.sample.step;

import org.springframework.batch.item.ItemProcessor;

import com.sk.batch.jobs.sample.data.UserJson;
import com.sk.batch.jobs.sample.data.UserXml;

public class XmlToJsonProcessor implements ItemProcessor<UserXml, UserJson> {

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
