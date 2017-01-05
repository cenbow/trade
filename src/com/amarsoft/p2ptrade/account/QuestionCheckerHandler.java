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

public class QuestionCheckerHandler extends JSONHandler {
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		if(request.containsKey("CertID")==false || request.get("CertID")==null)
			throw new HandlerException("certid.error");

		if(request.containsKey("Answer")==false || request.get("Answer")==null || request.containsKey("AnswerName")==false || request.get("AnswerName")==null)
			throw new HandlerException("securityquestion.emptyanswer");
		
		return checChange(request);
	}
	
	private JSONObject checChange(JSONObject request)throws HandlerException{
		try{
			//У�����֤����ȫ����
			BizObject obj = JBOFactory.getBizObjectManager("jbo.trade.user_authentication")
				.createQuery("select userid from o where DOCID=:certid and userid=:userid")
				.setParameter("certid", request.get("CertID").toString())
				.setParameter("userid", request.get("UserID").toString())
				.getSingleResult(false);
			if(obj == null){
				throw new HandlerException("certinput.error");
			}
			String sAnswerName = request.get("AnswerName").toString();
			if(!sAnswerName.equalsIgnoreCase("SECURITYANSWER") && !sAnswerName.equalsIgnoreCase("SECURITYANSWER2") && !sAnswerName.equalsIgnoreCase("SECURITYANSWER3"))
				sAnswerName = "SECURITYANSWER";
			obj = JBOFactory.getBizObjectManager("jbo.trade.user_account")
				.createQuery("select userid from o where "+sAnswerName+"=:answer and userid=:userid")
				.setParameter("answer", request.get("Answer").toString())
				.setParameter("userid", request.get("UserID").toString())
				.getSingleResult(false);
			if(obj == null){
				throw new HandlerException("securityquestion.wrongsecurityquestion");
			}
			
			return null;
		}
		catch(JBOException je){
			throw new HandlerException("default.database.error");
		}
		
	}
	

}
