package com.amarsoft.p2ptrade.util;

public class SendMessageFactory {
	 //���ŷ���---��ͨ�Ľӿ�
	public static final String SMS_3TONG_CONFIG = "3tong.com";
	
	public static MobileServiceInvoker getClientSend(String transCode) throws Exception{
		if(SMS_3TONG_CONFIG.equals(transCode)){
			return new PhoneMessage();
		}else{
			throw new Exception("�����벻��ȷ");
		}		
	}
}
