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
 * 		old_transpwd:   ��ʼ����
 * 		new_transpwd:	������	
 *      confirm_transpwd  ȷ������
 *      question1		��ȫ����
 *      answer1			�����
 * ���������
 * 		SuccessFlag:�Ƿ�ע��ɹ�	S/F
 *
 */
public class ModifyTranspwdHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String userid = (String) request.get("userid");
		String old_transpwd = (String) request.get("old_transpwd");
		String new_transpwd = (String) request.get("new_transpwd");
		String confirm_transpwd = (String) request.get("confirm_transpwd");
		String question = (String) request.get("question");
		String answer = (String) request.get("answer");
		//ԭʼ���벻��Ϊ��
		if(old_transpwd == null || old_transpwd.length() == 0){
			throw new HandlerException("modify.emptyoldpassword");
		}
		//�����벻��Ϊ��
		if(new_transpwd == null || new_transpwd.length() == 0){
			throw new HandlerException("modify.emptynewtranspwd");
		}
		//ȷ�����벻��Ϊ��
		if(confirm_transpwd == null || confirm_transpwd.length() == 0){
			throw new HandlerException("modify.emptyconfirmtranspwd");
		}
//		//��ȫ���ⲻ��Ϊ��
//		if(question == null || question.length() == 0){
//			throw new HandlerException("modify.emptyquestion1");
//		}
//		//����𰸲���Ϊ��
//		if(answer == null || answer.length() == 0){
//			throw new HandlerException("modify.emptyanswer1");
//		}
		JSONObject result = new JSONObject();
		
		try{
			//У��ԭʼ����
			BizObject boz = JBOFactory.getBizObjectManager("jbo.trade.user_account")
					.createQuery("select UserID from o where transpwd=:transpwd and userid=:userid")
					.setParameter("transpwd", old_transpwd.toUpperCase())
					.setParameter("userid", userid)
					.getSingleResult(false);
			if(boz==null){
				throw new HandlerException("old_transpwd.exist.error");
			}
		
//			//У�鰲ȫ����
//			BizObject boz1 = JBOFactory.getBizObjectManager("jbo.trade.user_account")
//					.createQuery("select UserID from o where securityquestion2=:securityquestion1 and userid=:userid")
//					.setParameter("securityquestion1", question1)
//					.setParameter("userid", userid)
//					.getSingleResult(false);
//			if(boz1==null){
//				throw new HandlerException("question1.exist.error");
//			}
//			//У�������
//			BizObject boz2 = JBOFactory.getBizObjectManager("jbo.trade.user_account")
//					.createQuery("select UserID from o where securityanswer2=:securityanswer1 and userid=:userid")
//					.setParameter("securityanswer1", answer1)
//					.setParameter("userid", userid)
//					.getSingleResult(false);
//			if(boz2==null){
//				throw new HandlerException("answer1.exist.error");
//			}
			
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("select UserID,TRANSPWD from o where UserID=:UserID ");
			query.setParameter("UserID", userid);
    
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("TRANSPWD", new_transpwd);
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
