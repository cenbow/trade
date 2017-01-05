package com.amarsoft.p2ptrade.tools;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * 2.2.8 ����֪ͨ
 * ���������
 * 		TempletID���������� 
 * 		UserID:�ͻ���� 
 * 		PhoneTel:�ֻ�����(���ֻ�ʱ����)
 * 		��������������TempletID��SMSConfig.xml�ļ���ƥ��
 * 
 * ���������
 * 		SendMsgFlag:�ɹ���־
 */
public class SendSMSHandler extends JSONHandler {
	@Override

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return sendOTPMessage(request);

	}

	@SuppressWarnings("unchecked")
	private JSONObject sendOTPMessage(JSONObject request)
			throws HandlerException {
		String sPhoneTel = null;
		if (request.get("TempletID") == null
				|| "".equals(request.get("TempletID"))) {
			throw new HandlerException("sms.templetid.error");
		}
		if (request.get("UserID") == null
				|| "".equals(request.get("UserID"))) {
			throw new HandlerException("userid.error");
		}
		if(request.containsKey("PhoneTel") && !request.get("PhoneTel").toString().equals("")){
			sPhoneTel = request.get("PhoneTel").toString();
		}
		
		String sUserID = request.get("UserID").toString();
		String sTempletID = request.get("TempletID").toString();
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		try {
			Iterator it = request.keySet().iterator();
			while (it.hasNext()) {
				String key = (String) it.next();
				if (!(key.equals("TempletID") || key.equals("UserID") || key.equals("PhoneTel"))) {
					if(key.contains("Amount") || key.contains("Balance")){
						parameters.put(key, GeneralTools.numberFormat(Double.parseDouble(request.get(key) == null?"0.0":request.get(key).toString())));
					}else{
						parameters.put(key, request.get(key).toString());
					}
				}
			}
			
			String sDate = new TimeTool().getsChDate();
			parameters.put("Date", sDate);
			
			if(sPhoneTel == null){
				JBOFactory jbo = JBOFactory.getFactory();
				sPhoneTel = getUserTel(jbo,sUserID);
			}
			
			boolean sSendResult = GeneralTools.sendSMS(sTempletID,
					sPhoneTel, parameters);

			JSONObject result = new JSONObject();
			result.put("SendMsgFlag", String.valueOf(sSendResult));
			return result;
		}catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("smsreminder.error");
		}
	}
	
	/**
	 * ��ȡ�û��ֻ�����
	 * @param jbo  JBOFactory
	 * @param sUserID  �û����
	 * @return
	 * @throws HandlerException
	 */
	private String getUserTel(JBOFactory jbo, String sUserID)
			throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = m
					.createQuery("select PHONETEL from o where userid=:userid");
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				return o.getAttribute("PHONETEL").toString();
			} else {
				throw new HandlerException("quaryphonetel.nodata.error");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("quaryphonetel.error");
		}
	}
}
