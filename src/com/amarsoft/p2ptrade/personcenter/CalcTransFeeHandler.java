package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ���������Ѽ��㽻�� 
 * ��������� 
 * 		Amount:���ֽ�� 
 * 		AccountBelong�������� 
 * ���������
 * 		Amount:ʵ�ʵ��ʽ��  
 * 		HandlCharge��������
 */
public class CalcTransFeeHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getCalcTransFee(request);

	}

	@SuppressWarnings("unchecked")
	private JSONObject getCalcTransFee(JSONObject request) throws HandlerException {
		if (request.get("Amount") == null || "".equals(request.get("Amount"))) {
			throw new HandlerException("withdraw.amount.error");
		}
		if (request.get("AccountBelong") == null
				|| "".equals(request.get("AccountBelong"))) {
			throw new HandlerException("accountbelong.error");
		} 

		double amount = Double.parseDouble(request.get("Amount").toString());// ���ֽ��
		
		JBOFactory jbo = JBOFactory.getFactory();
		
		double handlCharge = GeneralTools.getCalTransFee(jbo,"0010",amount);//������
		
		String sAccountBelong = request.get("AccountBelong").toString();// ��������

		try {
			JSONObject obj = new JSONObject();
			obj.put("RootType", "010");
			obj.put("Amount", String.valueOf(GeneralTools.numberFormat(amount - handlCharge)));
			obj.put("HandlCharge", String.valueOf(GeneralTools.numberFormat(handlCharge)));
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("calctransfee.error");
		}
	}
}
