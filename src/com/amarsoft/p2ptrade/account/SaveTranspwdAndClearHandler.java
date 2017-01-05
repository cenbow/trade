package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û����ý�������
 * ���������
 * 		userid:	�û�id
 * 		new_transpwd:	������	
 *      confirm_transpwd  ȷ������
 *      question1		��ȫ����
 *      answer1			�����
 * ���������
 * 		SuccessFlag:�Ƿ�ע��ɹ�	S/F
 *
 */
public class SaveTranspwdAndClearHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String userid = (String) request.get("userid");
		String new_transpwd = (String) request.get("new_transpwd");
		String confirm_transpwd = (String) request.get("confirm_transpwd");
		String question = (String) request.get("question");
		String answer = (String) request.get("answer");
		//�����벻��Ϊ��
		if(new_transpwd == null || new_transpwd.length() == 0){
			throw new HandlerException("modify.emptynewtranspwd");
		}
		//ȷ�����벻��Ϊ��
		if(confirm_transpwd == null || confirm_transpwd.length() == 0){
			throw new HandlerException("modify.emptyconfirmtranspwd");
		}
//		//��ȫ���ⲻ��Ϊ��
//		if(question == null || question1.length() == 0){
//			throw new HandlerException("modify.emptyquestion1");
//		}
//		//����𰸲���Ϊ��
//		if(answer == null || answer1.length() == 0){
//			throw new HandlerException("modify.emptyanswer1");
//		}
		
		JSONObject result = new JSONObject();
		JBOTransaction tx =null;
		
		try{
			tx = JBOFactory.createJBOTransaction();
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account",tx);
			
			BizObjectQuery query = m.createQuery("select UserID,TRANSPWD from o where UserID=:UserID ");
			query.setParameter("UserID", userid);
    
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("TRANSPWD", new_transpwd);
				m.saveObject(o);
				BizObjectManager m2 =jbo.getManager("jbo.trade.account_validlog",tx);
				m2.createQuery("delete from o where userid=:userid and validdate=:validdate and validtype='T' ")
					.setParameter("userid", userid)
					.setParameter("validdate", StringFunction.getToday())
					.executeUpdate();
				tx.commit();
				result.put("SuccessFlag", "S");
			}else
				result.put("SuccessFlag", "F");
			
		}catch(Exception e){
			try{
				tx.rollback();
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			
			e.printStackTrace();
			throw new HandlerException("�޸Ľ����������");
		}	
		return result;
	}
}
