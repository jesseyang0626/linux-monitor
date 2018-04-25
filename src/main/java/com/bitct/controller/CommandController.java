package com.bitct.controller;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.bitct.constants.SystemConstants;
import com.bitct.exception.BzException;
import com.bitct.service.CommandService;
import com.bitct.vo.ResultVO;
import com.bitct.vo.ServiceGroup;

@RequestMapping("/command")
@Controller
public class CommandController {
	@Autowired
	private CommandService commandService;
	final static Logger log = LoggerFactory.getLogger(CommandController.class);

	/**
	 * @author JesseYang 2018年4月20日下午2:43:10 检查配置文件中所有服务器所有服务的状态
	 */
	@RequestMapping("/checkAll")
	@ResponseBody
	public Object checkAll() {
		ResultVO<List<ServiceGroup>> rs = new ResultVO<List<ServiceGroup>>();
		try {
			long start = System.currentTimeMillis();
			rs.setT(commandService.checkAllByMultiThreads());
			rs.setSuccess(true);
			long end = System.currentTimeMillis();
			log.info("----------------cost :" + (end - start) / 1000 + "s");
		} catch (Exception e) {
			rs.setSuccess(false);
			rs.setMsg(SystemConstants.SYSTEM_ERROR);
			e.printStackTrace();
			log.error("CommandController checkAll() :" + e.getMessage());
		}
		return rs;
	}

	/**
	 * @author JesseYang 2018年4月20日下午2:43:10 检查配置文件中所有服务器所有服务的状态
	 */
	@RequestMapping("/checkAllFromCache")
	@ResponseBody
	public Object checkAllFromCache() {
		ResultVO<List<ServiceGroup>> rs = new ResultVO<List<ServiceGroup>>();
		try {
			rs.setT(commandService.checkAllFromCache());
			rs.setSuccess(true);
		} catch (Exception e) {
			rs.setSuccess(false);
			rs.setMsg(SystemConstants.SYSTEM_ERROR);
			e.printStackTrace();
			log.error("CommandController checkAll() :" + e.getMessage());
		}
		return rs;
	}

	@RequestMapping("serviceStart")
	@ResponseBody
	public Object serviceStart(@RequestParam String ip,
			@RequestParam String serviceName) {
		ResultVO<List<ServiceGroup>> rs = new ResultVO<List<ServiceGroup>>();
		try {
			commandService.serviceStart(ip, serviceName);
			rs.setSuccess(true);
		} catch (Exception e) {
			rs.setSuccess(false);
			rs.setMsg(SystemConstants.SYSTEM_ERROR);
			e.printStackTrace();
			log.error("CommandController serviceStart() :" + e.getMessage());
		}

		return rs;
	}

	@RequestMapping("serviceStop")
	@ResponseBody
	public Object serviceStop(@RequestParam String ip,
			@RequestParam String serviceName) {
		ResultVO<List<ServiceGroup>> rs = new ResultVO<List<ServiceGroup>>();
		try {
			commandService.serviceStop(ip, serviceName);
			rs.setSuccess(true);
		} catch (Exception e) {
			rs.setSuccess(false);
			rs.setMsg(SystemConstants.SYSTEM_ERROR);
			e.printStackTrace();
			log.error("CommandController serviceStart() :" + e.getMessage());
		}

		return rs;

	}

	@RequestMapping("serviceRestart")
	@ResponseBody
	public Object serviceRestart(@RequestParam String ip,
			@RequestParam String serviceName) {
		ResultVO<List<ServiceGroup>> rs = new ResultVO<List<ServiceGroup>>();
		try {
			commandService.serviceRestart(ip, serviceName);
			rs.setSuccess(true);
		} catch (Exception e) {
			rs.setSuccess(false);
			rs.setMsg(SystemConstants.SYSTEM_ERROR);
			e.printStackTrace();
			log.error("CommandController serviceStart() :" + e.getMessage());
		}

		return rs;

	}

	@RequestMapping("updateWar")
	@ResponseBody
	public Object updateWar(String fileDir) {
		ResultVO rs = new ResultVO();
		File file = new File(fileDir+"/mss.war");
		if (!file.exists()) {
			rs.setSuccess(false);
			rs.setMsg("该目录下没有mss.war");
			return rs;
		}else {
			try {
				commandService.updateWar( fileDir+"/mss.war");
				rs.setSuccess(true);
			} catch (IOException e) {
				rs.setSuccess(false);
				rs.setMsg("更新失败");
				commandService.updateWarFail();
				e.printStackTrace();
			}
		}
		return rs;
	}
	
	@RequestMapping("updateSQL")
	@ResponseBody
	public Object updateSQL(String fileDir) {
		ResultVO rs = new ResultVO();
		File baseFile = new File(fileDir+"/"+SystemConstants.DB_SQL_BASE);
		File mmsFile = new File(fileDir+"/"+SystemConstants.DB_SQL_MMS);
		File ftpdbFile = new File(fileDir+"/"+SystemConstants.DB_SQL_FTPDB);
		if (!baseFile.exists() || !mmsFile.exists() || !ftpdbFile.exists()) {
			rs.setSuccess(false);
			rs.setMsg("缺少数据库文件，确认目录下有base.sql,mss.sql,ftp_db.sql");
			return rs;
		}else {
			try {
				commandService.updateSQL( fileDir);
				rs.setSuccess(true);
			} catch(BzException bzException){
				rs.setSuccess(false);
				rs.setMsg(bzException.getMessage());
				return rs;
			}catch (Exception e) {
				rs.setSuccess(false);
				rs.setMsg("更新失败，请重试或者回退数据库");
		//		commandService.updateWarFail();
				e.printStackTrace();
			}
		}
		return rs;
	}

}
