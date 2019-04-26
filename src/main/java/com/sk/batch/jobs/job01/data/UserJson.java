package com.sk.batch.jobs.job01.data;

import java.util.Date;

import lombok.Getter;
import lombok.Setter;

//@JsonTypeName("user") 
//@JsonTypeInfo(include = JsonTypeInfo.As.WRAPPER_OBJECT, use = JsonTypeInfo.Id.NAME)
@Setter @Getter 
public class UserJson {
    private String userName;
    private int userId;
    private Date transactionDate;
    private double transactionAmount;
    private Date updatedDate;
    private String userGroup;
 
    @Override
    public String toString() {
        return "UserJson [username=" + userName + ", userId=" + userId
          + ", transactionDate=" + transactionDate + ", transactionAmount=" + transactionAmount
          + ", updatedDate=" + updatedDate + ", userGroup=" + userGroup + "]";
    }
}
