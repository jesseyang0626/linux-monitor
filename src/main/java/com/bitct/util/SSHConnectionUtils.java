package com.bitct.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

public class SSHConnectionUtils {
	public static Connection getConnection(String hostname,String user,String password) throws IOException{
		Connection connection = new Connection(hostname);
		connection.connect();
		boolean isAuthenticated = connection.authenticateWithPassword(user, password);
		if(isAuthenticated){
			return connection;
		}else {
			throw new RuntimeException("用户名密码错误！");
		}
	}
	
	public static Session getSession(String hostname,String user,String password) throws IOException{
		Connection connection = new Connection(hostname);
		connection.connect();
		boolean isAuthenticated = connection.authenticateWithPassword(user, password);
		if(isAuthenticated){
			return connection.openSession();
		}else {
			throw new RuntimeException("用户名密码错误！");
		}
	}
	
	public static Session getSession(Connection connection) throws IOException{
		return connection.openSession();
	}
	
	
	public static BufferedReader executeCommand(Session session,final String command) throws IOException{
		InputStream stdout = null;
		BufferedReader br = null;
		try {
			session.execCommand(command);
			stdout = new StreamGobbler(session.getStdout());
			br = new BufferedReader(new InputStreamReader(stdout));
		} catch (Exception e) {
			e.printStackTrace();
			
		}finally{
		}
		
		return br;
	}
}
