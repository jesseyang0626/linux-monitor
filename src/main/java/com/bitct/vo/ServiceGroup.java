package com.bitct.vo;

import java.util.List;

@SuppressWarnings("serial")
public class ServiceGroup implements java.io.Serializable,Comparable<ServiceGroup>{
	
	private String ip;
	private String user;
	private String password;
	private String lastUpdate;
	private List<ServiceItem> serviceList;
	private List<ServiceDisk> serviceDiskList;
	private ServiceMem serviceMem;

	
	
	public ServiceMem getServiceMem() {
		return serviceMem;
	}
	public void setServiceMem(ServiceMem serviceMem) {
		this.serviceMem = serviceMem;
	}
	public List<ServiceDisk> getServiceDiskList() {
		return serviceDiskList;
	}
	public void setServiceDiskList(List<ServiceDisk> serviceDiskList) {
		this.serviceDiskList = serviceDiskList;
	}
	public String getLastUpdate() {
		return lastUpdate;
	}
	public void setLastUpdate(String lastUpdate) {
		this.lastUpdate = lastUpdate;
	}
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public List<ServiceItem> getServiceList() {
		return serviceList;
	}
	public void setServiceList(List<ServiceItem> serviceList) {
		this.serviceList = serviceList;
	}
	@Override
	public int compareTo(ServiceGroup o) {
		
		if (Integer.valueOf(this.ip.replace(".", ""))>Integer.valueOf(o.ip.replace(".", ""))) {
			return 1;
		}
		return -1;
	}
	
	
	
	

}
