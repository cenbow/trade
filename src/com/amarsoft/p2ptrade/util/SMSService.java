package com.amarsoft.p2ptrade.util;

import com.amarsoft.are.ARE;
import com.amarsoft.are.AREException;
import com.amarsoft.are.AREService;

public class SMSService implements AREService {

	private String serviceId = "���Žӿڲ�����ʼ��id";
	private String serviceDescribe = "���Žӿڲ�����ʼ��desc";
	private String serviceVersion = "1.0";
	private String serviceProvider = "Amarsoft";
	private String smsConfigFile = "SMSConfig.xml";
	@Override
	public String getServiceDescribe() {
		return serviceDescribe;
	}

	@Override
	public String getServiceId() {
		return serviceId;
	}

	@Override
	public String getServiceProvider() {
		return serviceProvider;
	}

	@Override
	public String getServiceVersion() {
		return serviceVersion;
	}

	@Override
	public void init() throws AREException {		
		try {
			smsConfigFile = getSmsConfigFile();
			ARE.getLog().debug("smsConfigFile=="+smsConfigFile);
			if(this.smsConfigFile==null||"".equals(this.smsConfigFile))
				SMSFactory.initSMSConstant();
			else
				SMSFactory.initSMSConstant(this.smsConfigFile);
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	@Override
	public void shutdown() {
		
	}

	public void setSmsConfigFile(String smsConfigFile) {
		this.smsConfigFile = smsConfigFile;
	}
	
	public String getSmsConfigFile() {
		return smsConfigFile;
	}
}
