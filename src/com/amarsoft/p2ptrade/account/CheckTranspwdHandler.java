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
public class CheckTranspwdHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		JSONObject resultObj = new JSONObject();
		//��ȡ����ֵ
		String userID = (String) request.get("UserID");
		String phone = (String) request.get("phonetel");
		String certid = (String) request.get("certid");
		
		if(userID == null || userID.length() == 0){
			throw new HandlerException("common.emptyuserid");
		}
		if(phone == null || phone.length() == 0){
			throw new HandlerException("common.emptymobile");
		}
		//��֤�ֻ���
		RegisterCheckHandler rch1 = new RegisterCheckHandler();
		request.put("operate", "validatemobile_user");
		resultObj = (JSONObject)rch1.createResponse(request, arg1);
		if(!"OK".equalsIgnoreCase((String)resultObj.get("result"))){
			return "PHONETEL_ERROR";
		}
		HistoryListCountHandler hlch = new HistoryListCountHandler();
		int iTransactionRecordCount = (Integer)hlch.createResponse(request, arg1);
		if(iTransactionRecordCount>0){
			//��Ҫ��֤���֤��
			if(certid == null || certid.length() == 0){
				throw new HandlerException("certid.error");
			}
			else{
				rch1 = new RegisterCheckHandler();
				request.put("operate", "validatecertid_user");
				resultObj = (JSONObject)rch1.createResponse(request, arg1);
				if(!"OK".equalsIgnoreCase((String)resultObj.get("result"))){
					return "CERTID_ERROR";
				}
				else{
					return "OK";
				}
			}
		}
		else{
			return "OK";
		}
	}
}
