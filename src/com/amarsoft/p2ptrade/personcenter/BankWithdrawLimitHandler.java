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

/**
 * ���������޶��ѯ���� 
 * ��������� ��
 * ������������б� 
 * 		ItemNo:���д���
 * 		ItemName����������
 * 		TimeLimit�����ʽ����޶�
 */
public class BankWithdrawLimitHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getBankWithdrawLimit(request);

	}

	@SuppressWarnings("unchecked")
	private JSONObject getBankWithdrawLimit(JSONObject request)
			throws HandlerException {

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo
					.getManager("jbo.trade.code_library");

			BizObjectQuery query = m.createQuery("codeno=:codeno and isinuse=:isinuse");
			query.setParameter("codeno", "BankNo").setParameter("isinuse", "1");

			List<BizObject> list = query.getResultList(false);

			if (list != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();
					String sItemNo = o.getAttribute("ITEMNO").toString();//���д���
					String sItemName = o.getAttribute("ITEMNAME").toString();//��������
					String sTimeLimit = o.getAttribute("ATTRIBUTE3").toString();//���ʽ����޶�
//					String sDayLimit = o.getAttribute("ATTRIBUTE4").toString();//���ս����޶�

					obj.put("ItemNo", sItemNo);
					obj.put("ItemName", sItemName);
					obj.put("TimeLimit", sTimeLimit);
					obj.put("DayLimit", 1000000);
					
					array.add(obj);
				}
				
				JSONObject result = new JSONObject();
				result.put("RootType", "020");
				result.put("array", array);
				
				return result;
			} else{
				throw new HandlerException("bankwithdrawlimit.error");
			}
		} catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("bankwithdrawlimit.error");
		}
	}
}
