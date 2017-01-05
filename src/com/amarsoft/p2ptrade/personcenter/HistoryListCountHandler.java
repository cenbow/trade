package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ������ʷ��¼����
 * ���������  
 * 			UserID:�˻����
 * �������������
 */
public class HistoryListCountHandler extends JSONHandler {
	
	JSONObject result = new JSONObject();

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.transaction_record");
			String sQuerySql = "userid=:userid";
			int iCount = m.createQuery(sQuerySql)
					.setParameter("userid", request.get("UserID").toString())
					.getTotalCount();
			return iCount;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("queryhistory.error");
		}

	}
}

