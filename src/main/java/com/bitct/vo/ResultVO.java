package com.bitct.vo;

public class ResultVO<T> {
	private T t;
	private String msg;
	private boolean success;
	public T getT() {
		return t;
	}
	public void setT(T t) {
		this.t = t;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public boolean isSuccess() {
		return success;
	}
	public void setSuccess(boolean success) {
		this.success = success;
	}
	
	
}
