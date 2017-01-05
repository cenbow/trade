package com.amarsoft.p2ptrade.personcenter;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ��ѯ�����п�����
 * ��������� UserID:�˻���� 
 * ������� AccountNo:���п��� AccountName:�˻��� AccountBelong:������
 * LimitAmount:�޶�
 */
public class BindingBankCardStatusHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return getBindingBankCard(request);
	}

	/**
	 * ��ѯ�����п�
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	public JSONObject getBindingBankCard(JSONObject request)
			throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			JSONObject result = new JSONObject();
			BizObjectManager m = jbo.getManager("jbo.trade.account_info");

			BizObjectQuery query = m
					.createQuery("userid=:userid and status=:status order by accounttype desc,inputtime desc");
			query.setParameter("userid", sUserID).setParameter("status", "2").setParameter("isreturncard", "0");
			List<BizObject> list = query.getResultList(true);

			if (list != null && list.size() != 0) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();
					String sAccountNo = o.getAttribute("ACCOUNTNO").toString();
					String sSerialNo = o.getAttribute("SerialNo").toString();
					String sHideAccountNo="";
					if(sAccountNo.length()>8){
						 sHideAccountNo =  sAccountNo.substring(0,4)+"**** ****"+sAccountNo.substring(sAccountNo.length()-4,sAccountNo.length());//���ؿ����м�Ĳ���
					}else{
						 sHideAccountNo =  sAccountNo.substring(0,4)+"**** ****"+sAccountNo.substring(sAccountNo.length()-2,sAccountNo.length());//���ؿ����м�Ĳ���
					}
					String sStatus = o.getAttribute("STATUS").toString();
					String sAccountType = o.getAttribute("accounttype").toString();
					String sAccountBelong = o.getAttribute("ACCOUNTBELONG").getString();
					obj.put("Status", sStatus);// �Ƿ���֤
					obj.put("SerialNo", sSerialNo);// �˻���
					obj.put("AccountType", sAccountType);// �˻�����
					obj.put("HideAccountNo", sHideAccountNo);// ���غ���˻���
					obj.put("AccountName", o.getAttribute("ACCOUNTNAME").toString());// �˻���
					obj.put("IsReturnCard", o.getAttribute("ISRETURNCARD").toString());// �Ƿ񻹿
					obj.put("AccountBelong", sAccountBelong);// ��������
					obj.put("LimitAmount", String.valueOf(GeneralTools.numberFormat(o.getAttribute("LIMITAMOUNT").getDouble())));// �޶�
					array.add(obj);	
					
				}
				result.put("bankcard", array);
			}
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("querybindingbankcard.error");
		}
	}
}