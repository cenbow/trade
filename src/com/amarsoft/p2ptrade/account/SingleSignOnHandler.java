package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ��¼��¼����
 * ���������
 * 		UserID		�û�ID
 * 		SessionID   SessionID
 * 		RemoteIP	IP��ַ
 * 		ServiceName	WEB��������
 * 		ServerPort	WEB�������˿ں�
 * ���������
 * 		SuccessFlag:�ɹ���ʶ	S/F
 *
 */
public class SingleSignOnHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		JSONObject result = new JSONObject();
		String userID = (String) request.get("UserID");
		String sessionID = (String) request.get("SessionID");
		String remoteIP = (String) request.get("RemoteIP");
		String serviceName = (String) request.get("ServiceName");
		String serverPort = (String) request.get("ServerPort");
		
		if(sessionID == null || sessionID.length() == 0){
			throw new HandlerException("singlesignon.emptysessionid");
		}
		if(userID == null || userID.length() == 0){
			throw new HandlerException("singlesignon.emptyuserid");
		}
		
		//��Session����USER_LIST
		JBOTransaction tx= null;
		try {
			tx = JBOFactory.createJBOTransaction();
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager manager = jbo.getManager("jbo.trade.ti_user_list",tx);
			//ɾ������δ�����ļ�¼
			manager.createQuery("update o set finishtime=:now where o.userid=:userid and sessionid <>:sessionid and finishtime is null")
				.setParameter("userid", userID)
				.setParameter("now", StringFunction.getTodayNow())
				.setParameter("sessionid", sessionID)
				.executeUpdate();
			BizObject userList = manager.newObject();
			userList.setAttributeValue("SESSIONID",sessionID);
			userList.setAttributeValue("USERID",userID);
			userList.setAttributeValue("REMOTEIP",remoteIP);
			userList.setAttributeValue("BEGINTIME",StringFunction.getTodayNow());
			userList.setAttributeValue("SERVERNAME",serviceName);
			userList.setAttributeValue("SERVERPORT",serverPort);
			manager.saveObject(userList);
			tx.commit();
		} catch (JBOException e) {
			try{
				if(tx!=null)
					tx.rollback();
				e.printStackTrace();
			}
			catch(JBOException e1){
				e1.printStackTrace();
			}			
			throw new HandlerException("singlesignon.error");
		}
		
		result.put("SuccessFlag", "S");
		
		return result;
	}
}
