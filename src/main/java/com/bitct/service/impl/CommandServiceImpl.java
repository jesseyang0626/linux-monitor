package com.bitct.service.impl;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.Session;

import com.bitct.commands.RmtShellExecutor;
import com.bitct.conf.SystemConfig;
import com.bitct.constants.CommandsConstants;
import com.bitct.constants.SystemConstants;
import com.bitct.exception.BzException;
import com.bitct.service.CommandService;
import com.bitct.vo.CommandResult;
import com.bitct.vo.ServiceDisk;
import com.bitct.vo.ServiceGroup;
import com.bitct.vo.ServiceItem;
import com.bitct.vo.ServiceMem;

@Service
public class CommandServiceImpl implements CommandService {	
	
	final static Logger log = LoggerFactory.getLogger(CommandServiceImpl.class);
	@Autowired
	private SystemConfig systemConfig;
	
	@Autowired
	CacheManager cacheManager;
	
	final static int POOL_SIZE=4;
	
	private ExecutorService executorService;
	private CountDownLatch countDownLatch;
	
	
	public List<ServiceGroup> checkAll() throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		List<ServiceGroup> groupResults = new ArrayList<ServiceGroup>(groups.size());
		
		//遍历所有的服务器，检查状态后，更新缓存，装到结果数组里
		for (ServiceGroup serviceGroup : groups) {
			RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
			Connection connection = executor.getConnection();
			serviceGroup = checkServiceStatus(serviceGroup, connection);
			serviceGroup = checkDiskSpace(serviceGroup, connection);
			serviceGroup = checkMem(serviceGroup, connection);
			groupResults.add(serviceGroup);
			updateServiceGroupCache(serviceGroup);
			connection.close();
		}
		Collections.sort(groupResults);
		return groupResults;
	}
	
	@Override
	public List<ServiceGroup> checkAllByMultiThreads() throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		List<ServiceGroup> groupResults = new ArrayList<ServiceGroup>(groups.size());
		
		executorService = Executors.newFixedThreadPool(POOL_SIZE);
		countDownLatch = new CountDownLatch(groups.size());
		//遍历所有的服务器，检查状态后，更新缓存，装到结果数组里
		for (ServiceGroup serviceGroup : groups) {
			ServiceGroup group = serviceGroup;
			executorService.execute(new Runnable() {
				@Override
				public void run() {
					RmtShellExecutor executor = new RmtShellExecutor(group);
					Connection connection = null;
					try {
						connection = executor.getConnection();
						ServiceGroup groupRs = checkMem(checkDiskSpace(checkServiceStatus(serviceGroup, connection), connection), connection);
						groupResults.add(groupRs);
						updateServiceGroupCache(groupRs);
						countDownLatch.countDown();
					} catch (Exception e) {
						e.printStackTrace();
						log.error("多线程查询服务器发生错误："+e.getMessage());
					}finally{
						connection.close();
					}
				}
			});
		}
		countDownLatch.await();
		Collections.sort(groupResults);
		return groupResults;
	}
	
//	public List<ServiceGroup> checkAll() throws Exception {
//		List<ServiceGroup> groups = systemConfig.getGroupList();
//		List<ServiceGroup> groupResults = new ArrayList<ServiceGroup>(groups.size());
//		
//		//遍历所有的服务器，检查状态后，更新缓存，装到结果数组里
//		for (ServiceGroup serviceGroup : groups) {
//			RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
//			ServiceGroup group = executor.checkServiceIsAlive(serviceGroup);
//			groupResults.add(executor.checkDiskSpace(group));
//			updateServiceGroupCache(group);
//		}
//		
//		return groupResults;
//	}
	
//	@Override
//	public List<ServiceGroup> checkAllByMultiThreads() throws Exception {
//		List<ServiceGroup> groups = systemConfig.getGroupList();
//		List<ServiceGroup> groupResults = new ArrayList<ServiceGroup>(groups.size());
//		
//		executorService = Executors.newFixedThreadPool(POOL_SIZE);
//		countDownLatch = new CountDownLatch(groups.size());
//		//遍历所有的服务器，检查状态后，更新缓存，装到结果数组里
//		for (ServiceGroup serviceGroup : groups) {
//			executorService.execute(new Runnable() {
//				@Override
//				public void run() {
//					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
//					try {
//						ServiceGroup group = executor.checkServiceIsAlive(serviceGroup);
//						groupResults.add(executor.checkDiskSpace(group));
//						updateServiceGroupCache(group);
//						countDownLatch.countDown();
//					} catch (Exception e) {
//						e.printStackTrace();
//					}
//				}
//			});
//		}
//		countDownLatch.await();
//		return groupResults;
//	}
	

	@Override
	public List<ServiceGroup> checkAllFromCache() throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		List<ServiceGroup> groupResults = new ArrayList<ServiceGroup>(groups.size());
		for (ServiceGroup serviceGroup : groups) {
			Cache cache = cacheManager.getCache(serviceGroup.getIp());
			ValueWrapper wrapper = (ValueWrapper)cache.get(SystemConstants.SERVICE_GROUP);
			wrapper.get();
			groupResults.add((ServiceGroup)wrapper.get());
		}
		return groupResults;
	}



	@Override
	public void serviceStart(String ip, String serviceName) throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			if (ip.equals(serviceGroup.getIp())) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				executor.startService(serviceGroup, serviceName);
			}
		}
	}



	@Override
	public void serviceStop(String ip, String serviceName) throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			if (ip.equals(serviceGroup.getIp())) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				executor.stopService(serviceGroup, serviceName);
			}
		}
		
	}



	@Override
	public void serviceRestart(String ip, String serviceName) throws Exception {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			if (ip.equals(serviceGroup.getIp())) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				executor.restartService(serviceGroup, serviceName);
			}
		}
		
	}



	/*
	 * 更新缓存内servicegroup的状态
	 */
	@Override
	public void updateServiceGroupCache(ServiceGroup group) {
		Cache cache = cacheManager.getCache(group.getIp());
		cache.put(SystemConstants.SERVICE_GROUP, group);
	}

	@Override
	public ServiceGroup checkServiceStatus(ServiceGroup group,
			Connection connection) {
		Session session =null;
		try {
			session = connection.openSession();
			List<ServiceItem> serviceItems = group.getServiceList();
	    	//构造查询命令
	    	StringBuilder sb = new StringBuilder();
	    	for (ServiceItem serviceItem : serviceItems) {
				sb.append(CommandsConstants.PS_EF_GREP.replace("service", serviceItem.getService())+";");
			}
	    	//执行命令
	    	session.execCommand(sb.toString());
	    	CommandResult result = RmtShellExecutor.getCommandResult(session);
	    	log.info("*****RmtShellExecutor execute command:***** \n"+sb.toString()+"\n -----result:----- \n"+result.toString());
	    	//遍历所有服务 查看是否存在进程
	    	for (ServiceItem serviceItem : serviceItems) {
	    		Pattern p = Pattern.compile(serviceItem.getMatch());
	    		if (result.getError().equals("") || result.getError() == null ) {
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
			e.printStackTrace();
			log.error("checkServiceStatus()error:"+e.getMessage());
		}finally{
			if(session!=null){
				session.close();
			}
		}
		return group;
	}
	
	public ServiceGroup checkDiskSpace(ServiceGroup serviceGroup,Connection connection) {
		Session session = null;
		try {
			session = connection.openSession();
			session.execCommand(CommandsConstants.SPACE_DF_H);
			CommandResult result = RmtShellExecutor.getCommandResult(session);
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
		}finally{
			if(session!=null){
				session.close();
			}
		}
		return serviceGroup;
	}
	
	
	public ServiceGroup checkMem(ServiceGroup serviceGroup,Connection connection) {
		Session session = null;
		try {
			session = connection.openSession();
			session.execCommand(CommandsConstants.FREE_H);
			CommandResult result = RmtShellExecutor.getCommandResult(session);
			if(result.getStatus() == 0){
				String[] rs = result.getOut().split("\n");
				for (int i = 0; i < rs.length; i++) {
					if (rs[i].contains("Mem")) {
						String[] cols = rs[i].split("\\s+");
						ServiceMem mem = new ServiceMem(cols[1], cols[2], cols[3], cols[4], cols[5], cols[6]);
						serviceGroup.setServiceMem(mem);
					}
				}
		//		System.out.println(result.getOut().split("\n"));
				
			}
		} catch (Exception e) {
			e.printStackTrace();
			log.error("checkDiskSpace():" + e.getMessage());
		}finally{
			if(session!=null){
				session.close();
			}
		}
		return serviceGroup;
	}

	@Override
	public void uploadFile(String ip,String localFile, String remoteDir) {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			if (serviceGroup.getIp().equals(ip)) {
				RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
				Connection con = null;
				try {
					con = executor.getConnection();
					SCPClient scpClient = con.createSCPClient();
					scpClient.put(localFile,remoteDir); 
				} catch (IOException e) {
					e.printStackTrace();
					log.error("上传文件发生错误："+e.getMessage());
				}finally{
					if (con!=null) {
						con.close();
					}
				}
				return;
			}
		}
		throw new BzException("没有找到ip为:"+ip+"的服务器");
		
	}
	
	@Override
	public void uploadFile(Connection connection,String localFile, String remoteDir) {
		try {
			SCPClient scpClient = connection.createSCPClient();
			scpClient.put(localFile,remoteDir); 
		} catch (IOException e) {
			e.printStackTrace();
			log.error("上传文件发生错误："+e.getMessage());
		}finally{
			
		}
	}

	@Override
	public void updateWar(String localPath) throws IOException {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			List<ServiceItem> items = serviceGroup.getServiceList();
			//找到tomcat的服务器 上传到tomcat/webapps目录下
			for (ServiceItem serviceItem : items) {
				if (serviceItem.getService().equals("tomcat")) {
					//1 关tomcat 2 关memcached 3 备份原war包 4 复制war包 5 开memcached 6 开tomcat
					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
					Connection connection = executor.getConnection();
					String killTomcat = CommandsConstants.KILL.replace("service", "tomcat");
					String killMemcached =  CommandsConstants.KILL.replace("service", "memcached");
					String backWar =  "mv -f "+serviceItem.getInstall() + "/webapps/mss.war /var/local/mss.war.backup";
					String backWarDir =  "mv -f "+serviceItem.getInstall() + "/webapps/mss /var/local/mssBackup";
					String openMemcached = "service memcached start;";
					String openTomcat = serviceItem.getStart();
					
					doCommand(connection, killTomcat,killMemcached,backWar,backWarDir);
					uploadFile(serviceGroup.getIp(), localPath, serviceItem.getInstall()+"/webapps/");
					doCommand(connection, openMemcached,openTomcat);
					
//					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
//					Connection con = null;
//					try {
//						con = executor.getConnection();
//						SCPClient scpClient = con.createSCPClient();
//						scpClient.put(localPath,serviceItem.getInstall()+"/webapps/"); 
//					} catch (IOException e) {
//						e.printStackTrace();
//						log.error("上传文件发生错误："+e.getMessage());
//					}finally{
//						if (con!=null) {
//							con.close();
//						}
//					}
					break;
				}
			}
		}
		
	}

	@Override
	public void doCommand(Connection connection, String... commands) {
		int commandCount = commands.length;
		for (int i = 0; i < commandCount; i++) {
			Session session = null;
			try {
				session = connection.openSession();
				session.execCommand(commands[i]);
				CommandResult rs = RmtShellExecutor.getCommandResult(session);
				if (!StringUtils.isEmpty(rs.getError())) {
					log.error("command error:" + rs.getError());
			//		throw new BzException("执行命令："+commands[i]+"发生错误");
				}
			} catch (IOException e) {
				e.printStackTrace();
				log.error("doCommand error:"+e.getMessage());
			}finally{
				session.close();
			}
			
		}
		
		
	}

	/* 
	 * 更新失败的话 将mss.war放回
	 * /var/local/mss.war.backup => webapps/mss.war 
	 * /var/local/mssBackup =>webapps/mss
	 */
	@Override
	public void updateWarFail() {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			List<ServiceItem> items = serviceGroup.getServiceList();
			//找到tomcat的服务器 上传到tomcat/webapps目录下
			for (ServiceItem serviceItem : items) {
				if (serviceItem.getService().equals("tomcat")) {
					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
					Connection connection = null;
					try {
						connection = executor.getConnection();
						doCommand(connection, "mv -f /var/local/mss.war.backup " + serviceItem.getInstall() + "/webapps/mss.war");
						doCommand(connection, "mv -f /var/local/mssBackup " + serviceItem.getInstall() + "/webapps/mss");
						
					} catch (IOException e) {
						e.printStackTrace();
						log.error("回退更新失败："+e.getMessage());
					} finally{
						connection.close();
					}
					
					
				}
			}
		}
		
	}

	@Override
	public void updateSQL(String localSqlFilePath) {
		List<ServiceGroup> groups = systemConfig.getGroupList();
		for (ServiceGroup serviceGroup : groups) {
			List<ServiceItem> items = serviceGroup.getServiceList();
			//找到mysql的服务执行更新操作
			for (ServiceItem serviceItem : items) {
				if (serviceItem.getService().equals("mysql")) {
					RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
					Connection connection = null;
					try {
						connection = executor.getConnection();
						doCommand(connection,"mkdir /var/sqlFile;rm -rf /var/sqlFile/*;");
						uploadFile(serviceGroup.getIp(), localSqlFilePath+"/"+SystemConstants.DB_SQL_BASE, "/var/sqlFile");
						uploadFile(serviceGroup.getIp(), localSqlFilePath+"/"+SystemConstants.DB_SQL_MMS, "/var/sqlFile");
						uploadFile(serviceGroup.getIp(), localSqlFilePath+"/"+SystemConstants.DB_SQL_FTPDB, "/var/sqlFile");
						doCommand(connection,constructSQL());
						
					} catch (IOException e) {
						e.printStackTrace();
						log.error("执行导库命令失败："+e.getMessage());
						throw new BzException("执行导库命令失败");
					} finally{
						connection.close();
					}
					
					
				}
			}
		}
		
		
	}
	
	public String constructSQL(){
		StringBuilder sb = new StringBuilder();
		sb.append(CommandsConstants.SQL_IMPORT.replace("db", SystemConstants.DB_NAME_BASE));
		sb.append(SystemConstants.SQL_FILE_DIR);
		sb.append(SystemConstants.DB_SQL_BASE);
		sb.append(";");
		sb.append(CommandsConstants.SQL_IMPORT.replace("db", SystemConstants.DB_NAME_MMS));
		sb.append(SystemConstants.SQL_FILE_DIR);
		sb.append(SystemConstants.DB_SQL_MMS);
		sb.append(";");
		sb.append(CommandsConstants.SQL_IMPORT.replace("db", SystemConstants.DB_NAME_FTPDB));
		sb.append(SystemConstants.SQL_FILE_DIR);
		sb.append(SystemConstants.DB_SQL_FTPDB);
		sb.append(";");
		return sb.toString();
	}
	

	/* 
	 * 
	 * 
	 * 文件系统             容量  已用  可用 已用% 挂载点
	 *	/dev/mapper/cl-root   20G  5.8G   15G   29% /
	 *	devtmpfs              16G     0   16G    0% /dev
	 *	tmpfs                 16G     0   16G    0% /dev/shm
	 *	tmpfs                 16G   50M   16G    1% /run
	 */
//	@Override
//	public ServiceGroup checkDiskSpace(ServiceGroup serviceGroup) {
//		RmtShellExecutor executor = new RmtShellExecutor(serviceGroup);
//		try {
//			CommandResult result = executor.exec(CommandsConstants.SPACE_DF_H);
//			if(result.getStatus() == 0){
//				
//				String[] rs = result.getOut().split("\n");
//				List<ServiceDisk> disks = new ArrayList<ServiceDisk>();
//				for (int i = 0; i < rs.length; i++) {
//					if (rs[i].contains("/")) {
//						String[] cols = rs[i].split("\\s+");
//						ServiceDisk disk = new ServiceDisk(cols[0], cols[1], cols[2], cols[3], cols[4], cols[5]);
//						disks.add(disk);
//					}
//				}
//				serviceGroup.setServiceDiskList(disks);
//		//		System.out.println(result.getOut().split("\n"));
//				
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//			log.error("checkDiskSpace():" + e.getMessage());
//		}
//		return serviceGroup;
//		
//	}




}
