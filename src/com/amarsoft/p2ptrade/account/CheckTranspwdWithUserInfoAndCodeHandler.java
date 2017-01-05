package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.personcenter.HistoryListCountHandler;

/**
 * �û�ע�ύ��
 * ���������
 * 		userid:	�û�id
 * 		phone:�ֻ���
 * 		certid:���֤��
 * ���������
 * 		result:�Ƿ���֤�ɹ�	:OK��ʾ�ɹ�
 *
 */
public class CheckTranspwdWithUserInfoAndCodeHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		
		//��ȡ����ֵ
		String userID = (String) request.get("userid");
		String phone = (String) request.get("phonetel");
		String certid = (String) request.get("certid");
		String codenum = (String) request.get("codenum");
		if(codenum==null || codenum.trim().equals("")){
			throw new HandlerException("otpcode.check.error");
		}
		request.put("UserID", userID);
		//��������Ϣ
		CheckTranspwdHandler handler = new CheckTranspwdHandler();
		handler.createResponse(request, arg1);
		//���У����
		RegisterCheckHandler rch1 = new RegisterCheckHandler();
		request.put("operate", "validatecode_clear");
		request.put("objname", phone);
		request.put("objvalue", codenum);
		JSONObject resultObj = (JSONObject)rch1.createResponse(request, arg1);
		if(!"OK".equalsIgnoreCase((String)resultObj.get("result"))){
			throw new HandlerException("otpcode.check.error");
		}
		return "OK";
	}
}
