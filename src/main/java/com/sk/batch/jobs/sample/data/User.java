package com.sk.batch.jobs.sample.data;

import java.util.Date;
import lombok.Setter;
import lombok.Getter;


@Setter @Getter
public class User {
    private String userName;
    private int userId;
    private Date transactionDate;
    private double transactionAmount;
 
    @Override
    public String toString() {
        return "User [username=" + userName + ", userId=" + userId
          + ", transactionDate=" + transactionDate + ", transactionAmount=" + transactionAmount
          + "]";
    }
}
