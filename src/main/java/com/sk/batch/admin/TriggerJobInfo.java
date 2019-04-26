package com.sk.batch.admin;

import org.springframework.batch.core.Job;

import lombok.Data;

@Data
public class TriggerJobInfo {
	String name;
	String desc;
	String mode;
	String cron;
	String adminUrl;
	String callbackUrl;
	Job job;
}
