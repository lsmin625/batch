package com.sk.batch.admin;

import org.springframework.batch.core.Job;

import lombok.Data;

@Data
public class TriggerJobInfo {
	public static final String MODE_SELF = "SELF";
	public static final String MODE_TRIGGER = "TRIGGER";

	String name;
	String desc;
	String mode;
	String cron;
	String adminUrl;
	String callbackUrl;
	boolean registered;
	JobCaller caller;
	Job job;
	
	public String toString() {
		StringBuffer buff = new StringBuffer();
		buff.append("[name=" + name);
		buff.append(", desc=" + desc);
		buff.append(", mode=" + mode);
		buff.append(", cron=" + cron);
		buff.append(", admin=" + adminUrl);
		buff.append(", callback=" + callbackUrl);
		buff.append(", registered=" + registered);
		if(caller != null) {
			buff.append(", caller=" + caller.getCallerName());
		}
		buff.append("]");
		return buff.toString();
	}
}
