package com.amarsoft.p2ptrade.account;
/**
 * �����һ��ֻ��ź󴥷��Ľ���
 * ���������	
 * 			UserID			�û���
 * 			CertID			���֤����
 * 			Answer			��
 * 			AnswerName		���ֶ���
 * 			OTPCode			������֤��
 * ���������
 */
import java.util.Properties;
import java.util.Random;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.account.ConfirmMobileChangeHandler;
import com.amarsoft.p2ptrade.account.ConfirmPasswordRetrieveHandler;
import com.amarsoft.p2ptrade.account.QueryUserAccountInfoHandler;
import com.amarsoft.p2ptrade.project.InvestUserCheckHandler;

public class QueryUserCertIDrHandler extends JSONHandler {
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		if(request.containsKey("UserID")==false || request.get("UserID")==null)
			throw new HandlerException("common.emptyuserid");

		try{
			//У�����֤����ȫ����
			BizObject obj = JBOFactory.getBizObjectManager("jbo.trade.user_authentication")
				.createQuery("select DOCID from o where  userid=:userid")
				.setParameter("userid", request.get("UserID").toString())
				.getSingleResult(false);
			if(obj == null){
				throw new HandlerException("common.usernotauth");
			}
			JSONObject result = new JSONObject();
			result.put("certid", obj.getAttribute("DOCID").getString());
			return result;
		}
		catch(JBOException je){
			je.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}

}
