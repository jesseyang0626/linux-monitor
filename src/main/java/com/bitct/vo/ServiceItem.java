package com.bitct.vo;

@SuppressWarnings("serial")
public class ServiceItem implements java.io.Serializable{
	
	private String service;
	private String install;
	private String cmd;
	private String match;
	private String status;
	private String start;
	
	public String getStart() {
		return start;
	}
	public void setStart(String start) {
		this.start = start;
	}

	public String getService() {
		return service;
	}
	public void setService(String service) {
		this.service = service;
	}
	public String getInstall() {
		return install;
	}
	public void setInstall(String install) {
		this.install = install;
	}
	public String getCmd() {
		return cmd;
	}
	public void setCmd(String cmd) {
		this.cmd = cmd;
	}
	public String getMatch() {
		return match;
	}
	public void setMatch(String match) {
		this.match = match;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	
	
	

}
