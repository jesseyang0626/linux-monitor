package com.bitct.service;

import java.io.IOException;
import java.util.List;

import ch.ethz.ssh2.Connection;

import com.bitct.vo.ServiceGroup;

public interface CommandService {
	List<ServiceGroup> checkAll() throws Exception;
	List<ServiceGroup> checkAllByMultiThreads() throws Exception;
	List<ServiceGroup> checkAllFromCache() throws Exception;
	void doCommand(Connection connection,String ...commands);
	void serviceStart(String ip,String serviceName) throws Exception;
	void serviceStop(String ip,String serviceName) throws Exception;
	void serviceRestart(String ip,String serviceName) throws Exception;
	void updateServiceGroupCache(ServiceGroup group);
	ServiceGroup checkServiceStatus(ServiceGroup group,Connection connection);
	ServiceGroup checkDiskSpace(ServiceGroup serviceGroup,Connection connection);
	ServiceGroup checkMem(ServiceGroup serviceGroup,Connection connection);
	void uploadFile(String ip,String localFile,String remoteDir);
	void uploadFile(Connection connection,String localFile,String remoteDir);
	void updateWar(String localPath)  throws IOException ;
	void updateWarFail();
	void updateSQL(String localSqlFilePath);
	
	
//	ServiceGroup checkDiskSpace(ServiceGroup groups);
	
}
