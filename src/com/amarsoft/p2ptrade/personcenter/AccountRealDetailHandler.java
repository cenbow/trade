package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import javax.swing.text.Position.Bias;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û���ʵ��Ϣ(�����Ϣ)��ѯ����
 * ��������� 
 * 			UserID:�˻���� 
 * ��������� 
 * 			RealName:��ʵ���� 
 * 			CertID:���֤�� 
 */
public class AccountRealDetailHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		
		String sUserID = request.get("UserID").toString();//�û�ID
		
		JSONObject result = new JSONObject();//���ؽ��
		
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.account_detail");//�û�����
			BizObjectQuery query = m
					.createQuery("select realname,certid from o where userid=:userid");
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false);
			if(o != null){
				String sRealName = o.getAttribute("REALNAME").toString() == null ? "":o.getAttribute("REALNAME").toString();
				String sCertID = o.getAttribute("CERTID").toString() == null ? "":o.getAttribute("CERTID").toString();
				result.put("RealName", sRealName);
				result.put("CertID", sCertID); 
				return result;
			}else{
				throw new HandlerException("common.usernotexist");
			}
		} catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getaccountdetail.error");
		}

	}
}
