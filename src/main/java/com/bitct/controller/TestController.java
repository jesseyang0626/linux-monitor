package com.bitct.controller;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;

import com.bitct.commands.RmtShellExecutor;
import com.bitct.conf.SystemConfig;
import com.bitct.util.SSHConnectionUtils;
import com.bitct.vo.ServiceGroup;
import com.bitct.vo.ServiceItem;
@Controller
public class TestController {
    @Resource SystemConfig systemConfig;
    @Autowired CacheManager cacheManager;
    
    @RequestMapping("/")
    public String indexPageBindingResult(){
    	return "index";
    }
    
    @RequestMapping("/cache")
    @ResponseBody
    public String cache(){
    	 Cache cache = cacheManager.getCache("now");
         Cache.ValueWrapper valueWrapper = cache.get("now");
         Date a= (Date) valueWrapper.get();
    	return a.toGMTString();
    }
    
	@RequestMapping("/test")  
	 public String index(){  
		List<ServiceGroup> groupList =systemConfig.getGroupList();
		if(groupList!=null){
			for(ServiceGroup serviceGroup:groupList){
				System.out.println("========================");
				System.out.println(serviceGroup.getIp()+","+serviceGroup.getUser()+","+serviceGroup.getPassword());
				 List<ServiceItem> list=serviceGroup.getServiceList();
				 if(list!=null){
					 for(ServiceItem serviceItem:list){
						 System.out.println("       "+serviceItem.getService()+","+serviceItem.getInstall()+","+serviceItem.getCmd());
							
					 }
				 }
			}
		}
	     return "test";  
	 }  
	
	@RequestMapping("/con")
	@ResponseBody
	public String conString(@RequestParam String cm){
		final String mqStop = "service mq stop";
		final String mqStart = "service mq start";
		try {
			Connection connection = SSHConnectionUtils.getConnection("192.168.1.225", "root", "root");
			Session session = SSHConnectionUtils.getSession(connection);
			BufferedReader br = SSHConnectionUtils.executeCommand(session,cm);
			while (true)
			{
				String line = br.readLine();
				if (line == null)
					break;
				System.out.println(line);
			}
			/* Show exit status, if available (otherwise "null") */
			System.out.println("ExitCode: " + session.getExitStatus());
			session.close();
			connection.close();
			
		} catch (IOException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		return "ok";
	}
	
	@RequestMapping("/checkAll")
	@ResponseBody
	public Object checkAll(){
		List<ServiceGroup> groupList =systemConfig.getGroupList();
		List<ServiceGroup> rsList =new ArrayList<ServiceGroup>(groupList.size());
		
		for (ServiceGroup serviceGroup : groupList) {
			RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
			try {
				ServiceGroup serviceResult = executor.checkServiceIsAlive(serviceGroup);
				rsList.add(serviceResult);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		return rsList;
	}
	
	
	@RequestMapping("/check")
	@ResponseBody
	public Object check(@RequestParam String ip){
		List<ServiceGroup> groupList = systemConfig.getGroupList();
		ServiceGroup serviceResult = null;
		try {
			for (ServiceGroup serviceGroup : groupList) {
				if (serviceGroup.getIp().equals(ip)) {
					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
					serviceResult = executor.checkServiceIsAlive(serviceGroup);
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return serviceResult;
		
	}
	
	
	
}
