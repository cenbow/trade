package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û��һ����뽻��
 * ���������
 * 		userid:	�û�id
 * 		PassWord:	������	
 *      PassWord2  ȷ������
 * ���������
 * 		SuccessFlag:�Ƿ�ɹ�	S/F
 *
 */
public class FindPasswordHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String UserName = (String) request.get("UserName");
		String PassWord = (String) request.get("PassWord");
		String PassWord2 = (String) request.get("PassWord2");

		//�����벻��Ϊ��
		if(UserName == null || UserName.length() == 0){
			throw new HandlerException("find.emptyusername");
		}
		//�����벻��Ϊ��
		if(PassWord == null || PassWord.length() == 0){
			throw new HandlerException("find.emptypassword");
		}
		//�����벻��Ϊ��
		if(PassWord2 == null || PassWord2.length() == 0){
			throw new HandlerException("find.emptypassword");
		}

		JSONObject result = new JSONObject();
		
		try{
			
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("select UserID from o where (UserName=:UserName or PHONETEL=:UserName or email=:UserName) ");
			query.setParameter("UserName", UserName);
    
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("PASSWORD", MessageDigest.getDigestAsUpperHexString("MD5", PassWord));
				m.saveObject(o);			
				result.put("SuccessFlag", "S");
			}else{
				result.put("SuccessFlag", "F");				
			}
			
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("find.error");
		}	
		return result;
	}
}
