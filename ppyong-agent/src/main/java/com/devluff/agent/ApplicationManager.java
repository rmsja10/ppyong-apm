package com.devluff.agent;

import java.util.concurrent.CountDownLatch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.devluff.agent.network.NetworkClientThread;
import com.devluff.agent.network.NetworkServerThread;
import com.devluff.agent.scheduler.AgentScheduleInfo;
import com.devluff.agent.scheduler.AgentScheduleManager;

@Service
public class ApplicationManager {
	
	private static final Logger logger = LoggerFactory.getLogger(ApplicationManager.class);
	
	@Autowired private NetworkClientThread oNetworkClientThread;
	@Autowired private NetworkServerThread oNetworkServerThread;
	@Autowired private AgentScheduleManager oAgentScheduleManager;
	
	private CountDownLatch oLatch;
	private ApplicationConfig oApplicationConfig;
	
	public ApplicationManager() throws Exception{
		oApplicationConfig = new ApplicationConfig();
	}
	
	public boolean init() {
		if(!oApplicationConfig.loadConfig()) {
			logger.error("Loading application's config is fail...");
			return false;
		}
		oLatch = new CountDownLatch(2);
		oNetworkClientThread.setCountDownLatch(oLatch);
		oNetworkServerThread.setCountDownLatch(oLatch);
		
		AgentScheduleInfo oAgentScheduleInfo = oApplicationConfig.getAgentScheduleInfo();
		oAgentScheduleManager.setScheduleConfig(oAgentScheduleInfo);
		return true;
	}
	
	public boolean process() {
		if(!oAgentScheduleManager.startSchedule()) {
			logger.error("Fail to start agent schedule manager..");
			return false;
		}
		
		oNetworkClientThread.start();
		oNetworkServerThread.start();
		try {
			oLatch.await();
		} catch (InterruptedException e) {
			
		} catch (Exception e) {
			logger.error(e.getMessage());
			return false;
		}
		return true;
	}
	
	public boolean terminateProcess() {
		oNetworkClientThread.terminate();
		oNetworkServerThread.terminate();
		return true;
	}
	
	public boolean changeAgentScheduleInfo() {
		AgentScheduleInfo oAgentScheduleInfo = oApplicationConfig.getAgentScheduleInfo();
		oAgentScheduleManager.setScheduleConfig(oAgentScheduleInfo);
		oAgentScheduleManager.reloadSchedule();
		return true;
	}
}
