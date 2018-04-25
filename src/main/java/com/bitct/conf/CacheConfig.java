package com.bitct.conf;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.annotation.Resource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.guava.GuavaCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.bitct.commands.RmtShellExecutor;
import com.bitct.constants.SystemConstants;
import com.bitct.service.CommandService;
import com.bitct.vo.ServiceGroup;

@Configuration
public class CacheConfig {
	@Autowired
	private SystemConfig systemconfig;
	
//	@Resource
//	private CommandService commandService;

	@Bean
	public CacheManager getCacheManager() throws Exception {
		List<String> cacheNames = new ArrayList<String>();
		GuavaCacheManager cacheManager = new GuavaCacheManager();
		// GuavaCacheManager 的数据结构类似 Map<String,Map<Object,Object>> map =new
		// HashMap<>();

		List<ServiceGroup> groups = systemconfig.getGroupList();

		//初始化查询各服务器ip
	//	List<ServiceGroup> groups = commandService.checkAll();
		for (ServiceGroup serviceGroup : groups) {
	//		RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
	//		ServiceGroup group = executor.checkServiceIsAlive(serviceGroup);
		//	group = executor.checkDiskSpace(group);
			String ip = serviceGroup.getIp();
			//以ip为 cache的名字
			if (cacheManager.getCache(ip) == null) {
				cacheNames.add(ip);
				cacheManager.setCacheNames(cacheNames);
			}
			//一个cache是一组缓存，用ip标识
			Cache cache = cacheManager.getCache(ip);
			//每个服务器的信息，及最后缓存的时间
			cache.put(SystemConstants.SERVICE_GROUP, serviceGroup);
		}

		return cacheManager;
	}
}
