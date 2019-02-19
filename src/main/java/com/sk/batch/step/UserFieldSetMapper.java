package com.sk.batch.step;

import java.text.SimpleDateFormat;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

import com.sk.batch.step.User;

public class UserFieldSetMapper implements FieldSetMapper<User> {

	@Override
	public User mapFieldSet(FieldSet fieldSet) throws BindException {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        User user = new User();
        user.setUserName(fieldSet.readString("userName"));
        user.setUserId(fieldSet.readInt("userId"));
        String dateString = fieldSet.readString(2);
       	try {
        	user.setTransactionDate(dateFormat.parse(dateString));
        } catch (ParseException | java.text.ParseException e) {
            e.printStackTrace();
        }
        user.setTransactionAmount(fieldSet.readDouble(3));
        return user;
	}

}
