package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectKey;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.personcenter.ClearTransPassErrorCountHandler;
/**
 * �û���Ϣ���䣺�û������ֻ���
 * ���������
 * 	UserID	�û���ţ����䣩
 *  VeryCode  У����
 *  UserName  �û���
 *  MobilePhome �ֻ���
 * �����������
 * @author flian
 *
 */
public class UserInfoSupplement extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		if(request.containsKey("UserID")==false) throw new HandlerException("request.invalid");
		//if(request.containsKey("VeryCode")==false) throw new HandlerException("request.invalid");
		String sVeryCode = "";
		String sUserName = "";
		String sMobilePhone = "";
		if(request.containsKey("VeryCode") && request.get("VeryCode")!=null)
			sVeryCode = request.get("VeryCode").toString();
		if(request.containsKey("UserName") && request.get("UserName")!=null)
			sUserName = request.get("UserName").toString().trim();
		if(request.containsKey("MobilePhone") && request.get("MobilePhone")!=null)
			sMobilePhone = request.get("MobilePhone").toString();
		
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.user_account");
			BizObjectKey key = m.getKey().setAttributeValue("UserID", request.get("UserID").toString());
			//BizObjectQuery query = m.createQuery("select username,phonetel from o where o.userid=:userid");
			//query.setParameter("UserID", request.get("UserID").toString());
			//BizObject o = query.getSingleResult(true);
			BizObject o = m.getBizObject(key);
			if(o==null){
				throw new HandlerException("accountvalid.error");
			}
			else{
				if(o.getAttribute("username").getValue()!=null &&o.getAttribute("phonetel").getValue()!=null && "2".equals(o.getAttribute("PhoneAuthFlag").getString()))
					throw new HandlerException("account.updated.error");
				if(o.getAttribute("username").getValue()==null){//��Ҫ����
					//����˺�
					checkUserName(sUserName,m);
					//o.setAttributeValue("username", sUserName);
					ModifyUserAccountHandler h= new ModifyUserAccountHandler();
					JSONObject joRequestParams = new JSONObject();
					joRequestParams.put("UserID",request.get("UserID").toString());
					joRequestParams.put("FieldName","USERNAME");
					joRequestParams.put("FieldType","BASIC");
					joRequestParams.put("FieldValue",sUserName);
					JSONObject jresult = (JSONObject)h.createResponse(joRequestParams, null);
					if("S".equals(jresult.get("SuccessFlag").toString())){
						
					}
					else{
						throw new HandlerException("�޸��û���ʧ�ܣ�"+ jresult.get("FailDesc").toString());
					}
				}
				if(o.getAttribute("phonetel").getValue()==null){
					//����ֻ�У����
					checkVeryCode(sVeryCode,m);
					//����ֻ���
					checkMobile(sMobilePhone,m);
					//o.setAttributeValue("phonetel", sMobilePhone);
					ModifyUserAccountHandler h= new ModifyUserAccountHandler();
					JSONObject joRequestParams = new JSONObject();
					joRequestParams.put("UserID",request.get("UserID").toString());
					joRequestParams.put("FieldName","PHONETEL");
					joRequestParams.put("FieldType","BASIC");
					joRequestParams.put("FieldValue",sMobilePhone);
					JSONObject jresult = (JSONObject)h.createResponse(joRequestParams, null);
					if("S".equals(jresult.get("SuccessFlag").toString())){
						o.setAttributeValue("PhoneAuthFlag", "2");
						m.saveObject(o);
					}
					else{
						throw new HandlerException("�޸��ֻ���ʧ�ܣ�"+ jresult.get("FailDesc").toString());
					}
					
				}	
				JSONObject result = new JSONObject();
				o = m.getBizObject(key);
				if(o!=null){
					result.put("UserName", o.getAttribute("username").getString());
					result.put("MobilePhone", o.getAttribute("phonetel").getString());
				}
				return result;
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("accountvalid.error");
		}
	}
	
	private void checkUserName(String requestUserName,BizObjectManager m)throws HandlerException,JBOException  {
		if(requestUserName.equals(""))throw new HandlerException("request.invalid");
		/*
		// ����û����Ƿ���ȷ���Ƿ�Ψһ
		BizObject obj = m.createQuery("select userid from o where username=:username").setParameter("username", requestUserName).getSingleResult(false);
		if(obj!=null){
			throw new HandlerException("username.unique.error");
		}
		*/
	}

	private void checkMobile(String requestMobilePhone,BizObjectManager m) throws HandlerException,JBOException{
		if(requestMobilePhone.equals(""))throw new HandlerException("request.invalid");
		/*
		// ����ֻ����Ƿ���ȷ���Ƿ�Ψһ
		BizObject obj = m.createQuery("select userid from o where phonetel=:phonetel").setParameter("phonetel", requestMobilePhone).getSingleResult(false);
		if(obj!=null){
			throw new HandlerException("mobile.unique.error");
		}
		*/
	}

	private void checkVeryCode(String requestVeryCode,BizObjectManager m)throws HandlerException,JBOException{
		if(requestVeryCode.equals(""))throw new HandlerException("request.invalid");
		//TODO ���У�����Ƿ���ȷ  �������׳�verycode.error
	}

}
