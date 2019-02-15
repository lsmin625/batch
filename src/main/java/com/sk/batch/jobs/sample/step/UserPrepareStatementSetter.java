package com.sk.batch.jobs.sample.step;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.springframework.batch.item.database.ItemPreparedStatementSetter;

import com.sk.batch.jobs.sample.data.UserXml;

public class UserPrepareStatementSetter implements ItemPreparedStatementSetter<UserXml> {

	@Override
	public void setValues(UserXml item, PreparedStatement ps) throws SQLException {
        ps.setInt(1, item.getUserId());
        ps.setString(2, item.getUserName());
        ps.setDate(3, new Date(item.getTransactionDate().getTime()));
        ps.setDouble(4, item.getTransactionAmount());
        ps.setDate(5, new Date(item.getUpdatedDate().getTime()));
	}
}
