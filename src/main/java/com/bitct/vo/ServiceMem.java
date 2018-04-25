package com.bitct.vo;

public class ServiceMem {

	private String total;
	private String used;
	private String free;
	private String shared;
	private String buffCache;
	private String available;
	
	
	
	public ServiceMem() {
		super();
	}
	public ServiceMem(String total, String used, String free, String shared,
			String buffCache, String available) {
		super();
		this.total = total;
		this.used = used;
		this.free = free;
		this.shared = shared;
		this.buffCache = buffCache;
		this.available = available;
	}
	public String getTotal() {
		return total;
	}
	public void setTotal(String total) {
		this.total = total;
	}
	public String getUsed() {
		return used;
	}
	public void setUsed(String used) {
		this.used = used;
	}
	public String getFree() {
		return free;
	}
	public void setFree(String free) {
		this.free = free;
	}
	public String getShared() {
		return shared;
	}
	public void setShared(String shared) {
		this.shared = shared;
	}
	public String getBuffCache() {
		return buffCache;
	}
	public void setBuffCache(String buffCache) {
		this.buffCache = buffCache;
	}
	public String getAvailable() {
		return available;
	}
	public void setAvailable(String available) {
		this.available = available;
	}
	
	
}
