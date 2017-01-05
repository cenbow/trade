package com.amarsoft.p2ptrade.account;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.amarsoft.account.common.ObjectBalanceUtils;
import com.amarsoft.account.common.ObjectConstants;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.personcenter.BindingBankCardStatusHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ��ѯ�û������Ϣ
 * ���������
 * 		UserID					�û�ID
 * ���������
 * 		UsableBalance			�˻����ý��
 * 		FrozenBalance			������
 * @author flian
 *
 */
public class QueryUserBalanceHandler extends JSONHandler {

	
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userID = (String) request.get("UserID");
		if(userID == null || userID.length() == 0){
			throw new HandlerException("queryuseracctinfo.emptyuserid");
		}
		
		JSONObject result = new JSONObject();
		try{
			//�޸�����ȡ����
			double usableBalance = 0.0;
			double frozenBalance = 0.0;

			HashMap<String,Double> balances = ObjectBalanceUtils.ObjectBalanceUtils(userID, ObjectConstants.OBJECT_TYPE_001);
			if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_001))
				usableBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_001); //��ѯ�������
			
			if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_002))
				frozenBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_002); //��ѯ�������
			
			result.put("UsableBalance", String.valueOf(usableBalance));
			result.put("FrozenBalance", String.valueOf(frozenBalance));
			
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryuseracctinfo.error");
		}

		return result;
	}
}
