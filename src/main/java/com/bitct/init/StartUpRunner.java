package com.bitct.init;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import com.bitct.service.CommandService;

@Component
public class StartUpRunner implements CommandLineRunner{
	
	@Autowired
	private CommandService commandService;
	
	@Override
	public void run(String... arg0) throws Exception {
		commandService.checkAllByMultiThreads();
//		System.out.println("============start up==============");
	}

}
