package com.bitct.conf;

import java.util.ArrayList;
import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import com.bitct.vo.ServiceGroup;

@Configuration
@ConfigurationProperties("service-list")
public class SystemConfig {
	

	private final List<ServiceGroup> groupList = new ArrayList<ServiceGroup>();

	public List<ServiceGroup> getGroupList() {
		
		return groupList;
	}

	
}
