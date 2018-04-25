package com.bitct.commands;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

import com.bitct.constants.CommandsConstants;
import com.bitct.controller.CommandController;
import com.bitct.exception.BzException;
import com.bitct.vo.CommandResult;
import com.bitct.vo.ServiceDisk;
import com.bitct.vo.ServiceGroup;
import com.bitct.vo.ServiceItem;

public class RmtShellExecutor { 
	final static Logger log = LoggerFactory.getLogger(CommandController.class);
	
    private Connection conn; 
    /** *//** 远程机器IP */ 
    private String     ip; 
    /** *//** 用户名 */ 
    private String     usr; 
    /** *//** 密码 */ 
    private String     psword; 
    private String     charset = Charset.defaultCharset().toString(); 

    private static final int TIME_OUT = 1000 * 5 * 60; 

    /** *//** 
     * 构造函数 
     * @param param 传入参数Bean 一些属性的getter setter 实现略 
     */ 
    public RmtShellExecutor(ServiceGroup group) { 
        this.ip = group.getIp(); 
        this.usr = group.getUser(); 
        this.psword = group.getPassword(); 
    } 

    /** *//** 
     * 构造函数 
     * @param ip 
     * @param usr 
     * @param ps 
     */ 
    public RmtShellExecutor(String ip, String usr, String ps) { 
        this.ip = ip; 
        this.usr = usr; 
        this.psword = ps; 
    } 

    /**
     * 登录 
     * 
     * @return 
     * @throws IOException 
     */ 
    private boolean login() throws IOException { 
        conn = new Connection(ip); 
        conn.connect(); 
        return conn.authenticateWithPassword(usr, psword); 
    } 
    

    public Connection getConnection () throws IOException{
    	conn = new Connection(ip); 
        conn.connect(); 
        conn.authenticateWithPassword(usr, psword);
    	return conn;
    }

    /** 
     * 执行脚本 
     * 
     * @param cmds 
     * @return 
     * @throws Exception 
     */ 
    public CommandResult exec(String cmds) throws Exception { 
        InputStream stdOut = null; 
        InputStream stdErr = null; 
        String outStr = ""; 
        String outErr = ""; 
        int ret = -1; 
        CommandResult result = new CommandResult();
        try { 
            if (login()) { 
                // Open a new {@link Session} on this connection 
                Session session = conn.openSession(); 
                // Execute a command on the remote machine. 
                session.execCommand(cmds); 
                
                stdOut = new StreamGobbler(session.getStdout()); 
                outStr = processStream(stdOut, charset); 
                
                stdErr = new StreamGobbler(session.getStderr()); 
                outErr = processStream(stdErr, charset); 
                
                session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT); 
                
             //   System.out.println("outStr=" + outStr); 
             //   System.out.println("outErr=" + outErr); 
                ret = session.getExitStatus() == null?0:session.getExitStatus(); 

                result.setError(outErr);
                result.setOut(outStr);
                result.setStatus(ret);
            } else { 
                throw new BzException("登录远程机器失败" + ip); // 自定义异常类 实现略 
            } 
        } finally { 
            if (conn != null) { 
                conn.close(); 
            } 
            if (stdErr !=null) {
            	stdErr.close();
			}
            if (stdOut !=null) {
            	stdOut.close();
			}
        } 
        return result; 
    } 
    
    /**
     * @author JesseYang
     * 2018年4月20日上午10:26:23
     * 检查服务进程是否存在
     */
    public ServiceGroup checkServiceIsAlive(ServiceGroup group) throws Exception{
    	List<ServiceItem> serviceItems = group.getServiceList();
    	//构造查询命令
    	StringBuilder sb = new StringBuilder();
    	for (ServiceItem serviceItem : serviceItems) {
			sb.append(CommandsConstants.PS_EF_GREP+serviceItem.getService()+";");
		}
    	//执行命令
    	CommandResult result = exec(sb.toString());
    	log.info("*****RmtShellExecutor execute command:***** \n"+sb.toString()+"\n -----result:----- \n"+result.toString());
    	//遍历所有服务 查看是否存在进程
    	for (ServiceItem serviceItem : serviceItems) {
    		Pattern p = Pattern.compile(serviceItem.getMatch());
    		if (result.getStatus()==0 ) {
        		Matcher matcher = p.matcher(result.getOut());
        		if (matcher.find()) {
        			serviceItem.setStatus(CommandsConstants.STATUS_RUNNING);
        		}else {
        			serviceItem.setStatus(CommandsConstants.STATUS_STOP);
        		}
    		}else {
    			throw new BzException("命令没有执行成功："+result.getError());
    		}
    	}
    	group.setServiceList(serviceItems);
    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
    	group.setLastUpdate(sdf.format(new Date()));
    	return group;
    }
    
    public CommandResult stopService(ServiceItem service) throws Exception{
		return exec(CommandsConstants.KILL.replace("service",service.getService()));
    }
    
    public CommandResult startService(ServiceItem service) throws Exception{
    	final String stopCommand= service.getStart();
		return exec(stopCommand);
    }
    
    public CommandResult restartService(ServiceItem service) throws Exception{
    	stopService(service);
    	return exec(service.getStart());
    }
    
    public CommandResult stopService(ServiceGroup serviceGroup,String serviceName) throws Exception{
    	CommandResult result = null;
    	//遍历机器的所有服务，找到要操作的服务
		List<ServiceItem> services = serviceGroup.getServiceList();
		for (ServiceItem serviceItem : services) {
			if (serviceItem.getService().equals(serviceName)) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				result =  executor.stopService(serviceItem);
			}
		}
		return result;
    	
    }
    
    public CommandResult startService(ServiceGroup serviceGroup,String serviceName) throws Exception{
    	CommandResult result = null;
		List<ServiceItem> services = serviceGroup.getServiceList();
		for (ServiceItem serviceItem : services) {
			if (serviceItem.getService().equals(serviceName)) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				result =  executor.startService(serviceItem);
			}
		}
		return result;
    	
    }
    
    public CommandResult restartService(ServiceGroup serviceGroup,String serviceName) throws Exception{
    	CommandResult result = null;
		List<ServiceItem> services = serviceGroup.getServiceList();
		for (ServiceItem serviceItem : services) {
			if (serviceItem.getService().equals(serviceName)) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				result =  executor.restartService(serviceItem);
			}
		}
		return result;
    	
    }
    
	public ServiceGroup checkDiskSpace(ServiceGroup serviceGroup) {
		try {
			CommandResult result = exec(CommandsConstants.SPACE_DF_H);
			if(result.getStatus() == 0){
				
				String[] rs = result.getOut().split("\n");
				List<ServiceDisk> disks = new ArrayList<ServiceDisk>();
				for (int i = 0; i < rs.length; i++) {
					if (rs[i].contains("/")) {
						String[] cols = rs[i].split("\\s+");
						ServiceDisk disk = new ServiceDisk(cols[0], cols[1], cols[2], cols[3], cols[4], cols[5]);
						disks.add(disk);
					}
				}
				serviceGroup.setServiceDiskList(disks);
		//		System.out.println(result.getOut().split("\n"));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("checkDiskSpace():" + e.getMessage());
		}
		return serviceGroup;
		
	}
    
	/*
	 * 只能在登录后使用
	 */
	public ServiceGroup checkServicesIsAlive(ServiceGroup group) throws Exception{
		try {
			CommandResult result = new CommandResult();
			Session session = conn.openSession();
			List<ServiceItem> serviceItems = group.getServiceList();
	    	//构造查询命令
	    	StringBuilder sb = new StringBuilder();
	    	for (ServiceItem serviceItem : serviceItems) {
				sb.append(CommandsConstants.PS_EF_GREP+serviceItem.getService()+";");
			}
	    	//执行命令
	    	session.execCommand(sb.toString());
	    	result = getCommandResult(session);
	    	log.info("*****RmtShellExecutor execute command:***** \n"+sb.toString()+"\n -----result:----- \n"+result.toString());
	    	//遍历所有服务 查看是否存在进程
	    	for (ServiceItem serviceItem : serviceItems) {
	    		Pattern p = Pattern.compile(serviceItem.getMatch());
	    		if (result.getStatus()==0 ) {
	        		Matcher matcher = p.matcher(result.getOut());
	        		if (matcher.find()) {
	        			serviceItem.setStatus(CommandsConstants.STATUS_RUNNING);
	        		}else {
	        			serviceItem.setStatus(CommandsConstants.STATUS_STOP);
	        		}
	    		}else {
	    			throw new BzException("命令没有执行成功："+result.getError());
	    		}
	    	}
	    	group.setServiceList(serviceItems);
	    	SimpleDateFormat sdf = new SimpleDateFormat("yyyy年MM月dd日 HH时mm分ss秒");
	    	group.setLastUpdate(sdf.format(new Date()));
		} catch (Exception e) {
			// TODO: handle exception
		}
		
		return group;
	}
	
	
    public static CommandResult getCommandResult(Session session) throws IOException{
    	InputStream stdOut = null; 
        InputStream stdErr = null; 
        String outStr = ""; 
        String outErr = ""; 
        int ret = -1; 
        CommandResult result = new CommandResult();
    	try {
        	stdOut = new StreamGobbler(session.getStdout()); 
            outStr = processStream(stdOut, Charset.defaultCharset().toString()); 
            
            stdErr = new StreamGobbler(session.getStderr()); 
            outErr = processStream(stdErr, Charset.defaultCharset().toString()); 
            
            session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT); 
            
         //   System.out.println("outStr=" + outStr); 
         //   System.out.println("outErr=" + outErr); 
            ret = session.getExitStatus() == null?0:session.getExitStatus(); 

            result.setError(outErr);
            result.setOut(outStr);
            result.setStatus(ret);
		} catch (Exception e) {
			e.printStackTrace();
			log.error(e.getMessage());
		}finally{
			if (session != null) { 
				session.close(); 
            } 
            if (stdErr !=null) {
            	stdErr.close();
			}
            if (stdOut !=null) {
            	stdOut.close();
			}
		};
    	

        
        return result;
    }
	
    
    /** *//** 
     * @param in 
     * @param charset 
     * @return 
     * @throws IOException 
     * @throws UnsupportedEncodingException 
     */ 
    private static String processStream(InputStream in, String charset) throws Exception { 
        byte[] buf = new byte[1024]; 
        StringBuilder sb = new StringBuilder(); 
        while (in.read(buf) != -1) { 
            sb.append(new String(buf, charset)); 
        } 
        return sb.toString(); 
    } 

//    public static void main(String args[]) throws Exception { 
//    	
//    	long start = System.currentTimeMillis();
//        RmtShellExecutor exe = new RmtShellExecutor("192.168.1.225", "root", "root"); 
//        System.out.println(exe.exec("ps -ef|grep tomcat;ps -ef|grep mq;ps -ef|grep memcached;").toString()); 
//        long end = System.currentTimeMillis();
//        System.out.println("cost:"+(end-start)/1000+"s");
//        
//        
//    } 
} 