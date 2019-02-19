package com.sk.batch.step;

import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

import lombok.Getter;
import lombok.Setter;

@Setter @Getter @XmlRootElement(name = "user")
public class UserXml {
    private String userName;
    private int userId;
    private Date transactionDate;
    private double transactionAmount;
    private Date updatedDate;
 
    @Override
    public String toString() {
        return "UserXml [username=" + userName + ", userId=" + userId
          + ", transactionDate=" + transactionDate + ", transactionAmount=" + transactionAmount
          + ", updatedDate=" + updatedDate+ "]";
    }

}
