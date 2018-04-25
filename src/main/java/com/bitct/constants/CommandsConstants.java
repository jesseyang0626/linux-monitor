package com.bitct.constants;

public class CommandsConstants {

	public static final String PS_EF_GREP = "ps -ef|grep service|grep -v grep";
	public static final String KILL="ps -ef|grep service|grep -v grep|awk '{print $2}'|xargs kill -9";
	public static final String SPACE_DF_H="df -h";
	public static final String FREE_H="free -h";
	
	/*
	 * sql
	 */
	public static final String SQL_IMPORT="mysql -uroot -pmysql db <";
	
	/*
	 * 状态
	 */
	public static final String STATUS_RUNNING="运行中";
	public static final String STATUS_STOP="没有运行";
	
}
