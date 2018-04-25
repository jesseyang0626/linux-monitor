package com.bitct.exception;

/*
 * 业务异常
 */
public class BzException extends RuntimeException{
	
	public BzException(){}
	
	
	public BzException(String message){
		super(message);
	}
}
