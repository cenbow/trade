package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ȷ���ֻ��޸�
 * ���������
 * 		UserID:		�û����
 * ���������
 * 		SuccessFlag:�Ƿ�ɹ�	S/F
 * @author dxu
 *
 */
public class ConfirmMobileChangeHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		JSONObject result = new JSONObject();
		String userID = (String) request.get("UserID");
		
		//�û���Ų���Ϊ��
		if(userID == null || userID.length() == 0){
			throw new HandlerException("common.emptyuserid");
		}
		
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager userManager = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = userManager.createQuery("UserID=:UserID");
			query.setParameter("UserID", userID);
			BizObject user = query.getSingleResult(true);
			if(user != null){
				String newMobileNo = user.getAttribute("NEWMOBILENO").getString();
				String oldMobileNo = user.getAttribute("PHONETEL").getString();
				if(newMobileNo != null){
					user.setAttributeValue("PHONETEL", newMobileNo);
					
					BizObjectManager userAcctAuditManager =jbo.getManager("jbo.trade.account_audit");
					BizObject userAcctAudit = userAcctAuditManager.newObject();
					userAcctAudit.setAttributeValue("USERID", userID);
					userAcctAudit.setAttributeValue("CHANGETYPE", "PHONETEL");
					userAcctAudit.setAttributeValue("OLDVALUE1", oldMobileNo);
					userAcctAudit.setAttributeValue("NEWVALUE1", newMobileNo);
					userAcctAudit.setAttributeValue("UPDATETIME", StringFunction.getTodayNow());
					userAcctAuditManager.saveObject(userAcctAudit);
				}
				user.setAttributeValue("MOBILECHANGEFLAG", null);
				userManager.saveObject(user);
			}else{
				throw new HandlerException("common.usernotexist");
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("confirmmobilechange.error");
		}
		
		result.put("SuccessFlag", "S");
		return result;
	}

}
