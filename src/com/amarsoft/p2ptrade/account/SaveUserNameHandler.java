package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �޸��û���
 * ���������
 * 		userid:	
 * 		username:	
 * ���������
 * 		SuccessFlag:�Ƿ�ɹ�	S/F
 *
 */
public class SaveUserNameHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String userid = (String) request.get("userid");
		String username = (String) request.get("username");
		//�����벻��Ϊ��
		if(userid == null || userid.length() == 0){
			throw new HandlerException("modify.emptyuserid");
		}
		//ȷ�����벻��Ϊ��
		if(username == null || username.length() == 0){
			throw new HandlerException("modify.emptyusername");
		}

		JSONObject result = new JSONObject();
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("select username from o where username=:username ");
			query.setParameter("username", username);
    
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				result.put("SuccessFlag", "F");
			}else{
				BizObjectQuery query1 = m.createQuery("select userid,username from o where userid=:userid ");
				query1.setParameter("userid", userid);
	    
				BizObject oo = query1.getSingleResult(true);
				if(oo!=null){
					oo.setAttributeValue("username", username);
					m.saveObject(oo);
					result.put("SuccessFlag", "S");
				}else{
					result.put("SuccessFlag", "F");
				}
			}			
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("modify.error");
		}		
		return result;
	}
}