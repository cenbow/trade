package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.account.QueryUserAccountInfoHandler;
import com.amarsoft.p2ptrade.account.UserSignCountHandler;
import com.amarsoft.p2ptrade.front.TopRecordProHandler;
/**
 * ����������ҳ�Ľ���
 * ���������
 * 		UserID:�˻����
 *
 */
public class MemberIndexHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getAccount(request);		
	}
	  
	/**
	 * ����ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getAccount(JSONObject request)throws HandlerException {
		
		JSONObject result = new JSONObject();
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
	
		//�û���Ϣ
		QueryUserAccountInfoHandler ua = new QueryUserAccountInfoHandler();
		JSONObject user = (JSONObject)ua.createResponse(request, null);
		result.put("userinfo", user);
		//ǩ��
		UserSignCountHandler sign = new UserSignCountHandler();
		JSONObject signo = (JSONObject)sign.createResponse(request, null);
		result.put("sign", signo);
		//�Ƽ���Ͷ��
		TopRecordProHandler invest = new TopRecordProHandler();
		request.put("z", "5");
		JSONObject ob = (JSONObject) invest.createResponse(request, null);
		
		result.put("prolist", ob.get("proList"));
		//��Ͷ��ͳ��
		UserIncomeHandler total = new UserIncomeHandler();
		JSONObject userIncome = (JSONObject)total.createResponse(request, null);
		result.put("userIncome", userIncome);
		//����Ͷ��
		InvestmentListHandler pro = new InvestmentListHandler();
		JSONObject invPro = (JSONObject)pro.createResponse(request, null);
		result.put("invPro", invPro.get("array"));
		
		return result;
	}
}