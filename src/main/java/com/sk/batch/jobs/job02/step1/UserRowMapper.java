package com.sk.batch.jobs.job02.step1;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.sk.batch.jobs.job01.data.UserXml;

public class UserRowMapper implements RowMapper<UserXml> {

	@Override
	public UserXml mapRow(ResultSet rs, int rowNum) throws SQLException {
		UserXml userXml = new UserXml();
		userXml.setUserId(rs.getInt("user_id"));
		userXml.setUserName(rs.getString("user_name"));
		userXml.setTransactionDate(rs.getDate("transaction_date"));
		userXml.setTransactionAmount(rs.getDouble("transaction_amount"));
		userXml.setUpdatedDate(rs.getDate("updated_date"));
		return userXml;
	}
}
