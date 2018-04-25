package com.bitct.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.bitct.controller.CommandController;
import com.bitct.service.CommandService;


@EnableScheduling
@Component
public class CheckServiceStatusSchedule {

	final static Logger log = LoggerFactory.getLogger(CheckServiceStatusSchedule.class);
	
	@Autowired
	private CommandService commandService;
	
	@Scheduled(cron = "${checkServiceStatus}")
	public void checkServiceStatus(){
		try {
			commandService.checkAllByMultiThreads();
			log.debug("定时查询服务器状态执行");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("定时查询服务器状态任务发生错误："+e.getMessage());
		}
	}
	
}
