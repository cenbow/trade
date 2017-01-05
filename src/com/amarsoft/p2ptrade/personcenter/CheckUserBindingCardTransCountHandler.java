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
import com.amarsoft.p2ptrade.tools.TimeTool;

/**
 * ����֤�ѵ��մ�����ѯ����(��ѯ��ǰ�û����ճɹ����а���֤�Ĵ����Ƿ񳬹����ƴ���)
 * 		��������� UserID:�˻���� 
 * ���������
 * 		�ɹ���־
 * 
 */
public class CheckUserBindingCardTransCountHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return getBindingBankCardTime(request);
	}

	/**
	 * ��ѯ�����п�
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getBindingBankCardTime(JSONObject request)
			throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			
			BizObjectManager m = jbo.getManager("jbo.trade.transaction_record"); 

			BizObjectQuery query = m.createQuery("select count(*) as v.count from o where userid=:userid and transtype=:transtype and transdate=:transdate and status=:status");
			query.setParameter("userid", sUserID).setParameter("transtype", "1070").setParameter("transdate", new TimeTool().getsCurrentDate()).setParameter("status", "10");

			BizObject o = query.getSingleResult(false);
			if (o != null) {
				double count = Double.parseDouble(o.getAttribute("count").toString());
				if(count >= 5){
					throw new HandlerException("bindcardcountuserlimit.error");
				}
			}
			return null;
		} catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getbindingbankcardtime.error");
		}
	}
}
