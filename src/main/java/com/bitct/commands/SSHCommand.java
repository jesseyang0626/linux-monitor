package com.bitct.commands;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;

import ch.ethz.ssh2.Session;

import com.bitct.conf.SystemConfig;
import com.bitct.util.SSHConnectionUtils;
import com.bitct.vo.ServiceGroup;


/*
 * 这个类用来执行具体的linux语句
 * 弃用 现使用 ：RmtShellExecutor
 */
@Deprecated
public class SSHCommand {
	@Autowired
	private  SystemConfig systemConfig;
	
	public void checkAll() throws IOException{
		List<ServiceGroup> groupList = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groupList) {
			serviceGroup.getIp();
			Session session = 
					SSHConnectionUtils.getSession(serviceGroup.getIp(), serviceGroup.getUser(), serviceGroup.getPassword());
			
		}
	}
	
	
	/**
	 * @author JesseYang
	 * 2018年4月20日上午9:35:10
	 * 判断应用是否运行。
	 * match:配置文件定义的匹配字段
	 * commandResult：执行命令后返回的结果
	 */
	public boolean checkServiceIsAlive(String match,String commandResult){
		Pattern p = Pattern.compile(match);
		Matcher matcher = p.matcher(commandResult);
		if (matcher.find()) {
			return true;
		}else {
			return false;
		}
	}
	
	public static void main(String[] args) {
		SSHCommand sshCommand = new SSHCommand();
		boolean flag = 
				sshCommand.checkServiceIsAlive("mybatis", "/var/local/tomcat/sdfsdfsd");
		System.out.println(flag);
	}
	
}
