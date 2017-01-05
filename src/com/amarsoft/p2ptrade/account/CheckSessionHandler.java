package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ���Session�Ƿ���Ч
 * ���������
 * 		UserID		�û�ID
 * 		SessionID   SessionID
 * ���������
 * 		SuccessFlag:�ɹ���ʶ	S/F
 *
 */
public class CheckSessionHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		JSONObject result = new JSONObject();
		String userID = (String) request.get("UserID");
		String sessionID = (String) request.get("SessionID");
		
		if(sessionID == null || sessionID.length() == 0){
			throw new HandlerException("clearsession.emptysessionid");
		}
		if(userID == null || userID.length() == 0){
			throw new HandlerException("clearsession.emptyuserid");
		}

		try {
			BizObjectManager manager=JBOFactory.getBizObjectManager("jbo.trade.ti_user_list");
			BizObjectQuery query = manager.createQuery("select SessionID,UserID,finishTime from o where SessionID=:SessionID and UserID=:UserID and finishTime is not null");
			query.setParameter("SessionID", sessionID);
			query.setParameter("UserID", userID);
			BizObject user = query.getSingleResult(false);
			if(user != null){
				//finishtime���ڱ�ʾ���𴦵�¼��
				result.put("SuccessFlag", "F");
			}
			else{
				result.put("SuccessFlag", "S");
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("clearsession.error");
		}		
		return result;
	}

}
