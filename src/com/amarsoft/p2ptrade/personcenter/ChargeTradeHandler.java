package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class ChargeTradeHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return chargetrade(request);
		
	}

	
	private JSONObject chargetrade(JSONObject request)throws HandlerException {
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
		JSONObject result = new JSONObject();
		
		try{
			//��ѯ�û��˻���Ϣ
			AccountBalanceHandler ab = new AccountBalanceHandler();
			JSONObject obj = (JSONObject) ab.createResponse(request, null);
			result.put("accountBalance",obj);
			
			//��ѯ���س�ֵ����������
			request.put("codeNo", "BankNo");
			request.put("Remark", "A");
			getCodeLibraryHandler code = new getCodeLibraryHandler();
			JSONObject codelist = (JSONObject) code.createResponse(request, null);
			result.put("BankList", codelist.get("codelist"));
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryaccountbalance.error");
		}
	}
}
