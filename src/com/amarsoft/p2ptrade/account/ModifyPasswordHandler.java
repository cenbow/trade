package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û�ע�ύ��
 * ���������
 * 		userid:	�û�id
 * 		old_password:   ��ʼ����
 * 		new_password:	������	
 *      confirm_password  ȷ������
 *      question		��ȫ����
 *      answer			�����
 * ���������
 * 		SuccessFlag:�Ƿ�ע��ɹ�	S/F
 *
 */
public class ModifyPasswordHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String userid = (String) request.get("userid");
		String old_password = (String) request.get("old_password");
		String new_password = (String) request.get("new_password");
		String confirm_password = (String) request.get("confirm_password");
		//String question = (String) request.get("question");
		//String answer = (String) request.get("answer");
		//ԭʼ���벻��Ϊ��
		if(old_password == null || old_password.length() == 0){
			throw new HandlerException("modify.emptyoldpassword");
		}
		//�����벻��Ϊ��
		if(new_password == null || new_password.length() == 0){
			throw new HandlerException("modify.emptynewpassword");
		}
		//ȷ�����벻��Ϊ��
		if(confirm_password == null || confirm_password.length() == 0){
			throw new HandlerException("modify.emptyconfirmpassword");
		}
	/*	//��ȫ���ⲻ��Ϊ��
		if(question == null || question.length() == 0){
			throw new HandlerException("modify.emptyquestion");
		}
		//����𰸲���Ϊ��
		if(answer == null || answer.length() == 0){
			throw new HandlerException("modify.emptyanswer");
		}*/
		JSONObject result = new JSONObject();
		
		try{
			//У��ԭʼ����
			old_password = MessageDigest.getDigestAsUpperHexString("MD5", old_password);
			BizObject boz = JBOFactory.getBizObjectManager("jbo.trade.user_account")
					.createQuery("select UserID from o where password=:password and userid=:userid")
					.setParameter("password", old_password.toUpperCase())
					.setParameter("userid", userid)
					.getSingleResult(false);
			if(boz==null){
				throw new HandlerException("old_password.exist.error");
			}
	/*		//У�鰲ȫ����
			BizObject boz1 = JBOFactory.getBizObjectManager("jbo.trade.user_account")
					.createQuery("select UserID from o where securityquestion=:securityquestion and userid=:userid")
					.setParameter("securityquestion", question)
					.setParameter("userid", userid)
					.getSingleResult(false);
			if(boz1==null){
				throw new HandlerException("question.exist.error");
			}
			//У�������
			BizObject boz2 = JBOFactory.getBizObjectManager("jbo.trade.user_account")
					.createQuery("select UserID from o where securityanswer=:securityanswer and userid=:userid")
					.setParameter("securityanswer", answer)
					.setParameter("userid", userid)
					.getSingleResult(false);
			if(boz2==null){
				throw new HandlerException("answer.exist.error");
			}*/
			
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("select UserID from o where UserID=:UserID ");
			query.setParameter("UserID", userid);
    
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("PASSWORD", MessageDigest.getDigestAsUpperHexString("MD5", new_password));
				System.out.println("--------------"+new_password);
				m.saveObject(o);
			
			}
			
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("register.error");
		}	
		result.put("SuccessFlag", "S");
		return result;
	}
}
