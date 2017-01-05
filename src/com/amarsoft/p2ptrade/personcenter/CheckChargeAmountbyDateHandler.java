package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
/**
 * У���û����ճ�ֵ�Ƿ񳬹��޶�
 * ���������
 * 		UserID:�˻����
 * 		Amount:��ֵ���
 * ���������
 * 		�ɹ���־
 *
 */
public class CheckChargeAmountbyDateHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return CheckChargeAmountbyDate(request);
		
	}
	  
	/**
	 * ����ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject CheckChargeAmountbyDate(JSONObject request)throws HandlerException {
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
		double amount = 0d;
		try {
			amount = Double.parseDouble(request.get("Amount").toString());
		} catch (NumberFormatException e2) {
			throw new HandlerException("chargeapply.amount.error");
		}
		if (amount <= 0) {
			throw new HandlerException("chargeapply.amount.error");
		}
		
		String sUserID = request.get("UserID").toString();//�û����
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			double chargeAllAmount = getChargeCountByDate(jbo, sUserID, StringFunction.getToday());
			
			if (chargeAllAmount + amount >= 1000000) {
				throw new HandlerException("charge.limitallamount.error");
			}else{
				return null;
			}
		}catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			throw new HandlerException("queryaccountinfo.error");
		}
	}
	
	
	/**
	 * ��ȡ�����ѽ������ֽ��׵Ĵ���
	 * 
	 * @param jbo
	 *            JBOFactory
	 * @param sUserID
	 *            �û����
	 * @param transdate
	 *            ��ǰ����
	 * @return �ѽ��г�ֵ���׵��ܽ��
	 * @throws HandlerException
	 */
	private double getChargeCountByDate(JBOFactory jbo, String sUserID,
			String transdate) throws HandlerException {
		double amount = 0;
		try {
			BizObjectManager manager;
			manager = jbo.getManager("jbo.trade.transaction_record");
			BizObjectQuery query = manager
					.createQuery("select sum(o.AMOUNT) as v.amount from o where userid=:userid and transdate=:transdate and transtype in (:transtype1,:transtype2,:transtype3) and status not in (:status1,:status2,:status3)");
			query.setParameter("userid", sUserID);
			query.setParameter("transdate", transdate);
			query.setParameter("transtype1", "1010")
				 .setParameter("transtype2", "1011")
				 .setParameter("transtype3", "1012");
			query.setParameter("status1", "04")
					.setParameter("status2", "20")
					.setParameter("status3", "00");
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				amount = Double
						.parseDouble(o.getAttribute("amount").toString() == null ? "0.0"
								: o.getAttribute("amount").toString());
			} else {
				amount = 0;
			}
			return amount;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getchargeallamount.error");
		}
	}

	
	
}
