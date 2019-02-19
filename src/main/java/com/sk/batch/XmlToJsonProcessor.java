package com.sk.batch;

import org.springframework.batch.item.ItemProcessor;

import com.sk.batch.UserJson;
import com.sk.batch.UserXml;

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
