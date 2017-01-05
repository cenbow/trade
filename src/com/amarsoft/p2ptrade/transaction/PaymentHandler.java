/**
 * 
 */
package com.amarsoft.p2ptrade.transaction;

import java.sql.SQLException;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.web.service.imp.transclass.JsptoTrans;
import com.amarsoft.web.service.imp.transclass.transformobject.OPayInfoRes;
import com.amarsoft.web.service.imp.util.TransTools;

/**
 * @ ���𻹿� 2014-5-16 ���� Paytype �������ͣ�1һ�㻹��,3��ǰ���� ��ͬ�� ���
 * ������Ϣ����Ϣ��ƽ̨����ѡ������ѡ�ƽ̨����ѡ���������ѡ���ǰ����ΥԼ���˻����
 */
public class PaymentHandler extends TradeHandler {
	/*
	 * ��������
	 * 
	 * REPaymentConsult ��ǰ���� PaymentConsult �ֶ����� ���׷�ʽ Payment һ�㻹�� REPayment
	 * ��ǰ���� PaymentConsult һ�㻹������ REPaymentConsult ��ǰ��������
	 */

	@SuppressWarnings("unchecked")
	@Override
	protected Object requestObject(JSONObject request, JBOFactory jbo) throws HandlerException {

		if (request.get("PayType") == null || request.get("loanNo") == null || null == request.get("userId")) {
			throw new HandlerException("request.invalid");
		}
		/**
		 * ����Ϊ����ʱ��ע�ͣ���ʽʹ��ʱӦ��ȡ��
		 */
		// else if(!request.get("userId").equals(getUseId(request, jbo))){
		// throw new HandlerException("request.invalid");
		// }
		
		if ("1".equals(request.get("PayType").toString())) {
			request.put("Method", "Payment");
		}else if ("3".equals(request.get("PayType").toString())) {
			request.put("Method", "REPayment");
		}
		validateInfo(request, jbo);
		
		return request;
	}

	/**
	 * ���ýӿڱ������ݣ����ж��Ƿ���Ҫ��ֵ
	 * 
	 * @param request
	 * @param jbo
	 */
	@SuppressWarnings("unchecked")
	private void validateInfo(JSONObject request, JBOFactory jbo) {
		
		
		String sPayAmt=request.get("payAmt")==null?"0":request.get("payAmt").toString();
		JsptoTrans jt = new JsptoTrans();
		try {
			jt.setConn(ARE.getDBConnection("als"));// ����
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String loanNo = "TEST20140916";
		int seqId = 3;// ���Գ�ʼ��ֵ������Ϊ0
//		try {
//			seqId = getCurrentSeq(request, jbo);
//		} catch (HandlerException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		String serialNo = loanNo + seqId;
		jt.setMethod(request.get("Method").toString());// ���׷�ʽ
		jt.setObjectNo(loanNo);// �����˺� ��ݺ�
		jt.setAmt(sPayAmt);// ������ ����ʱ�������
		jt.setObjectType("");
		jt.setSerialNo(serialNo);
	
			OPayInfoRes opir = (OPayInfoRes) jt.runPayment();
			String stype = opir.getReturnType();
		
		//����Ƿ��д���
		if (null!=stype&&!"0000".equals(stype)) {
			
			System.out.println("���𻹿����=======================��"+TransTools.getErrorMss(stype));
		} else {
			System.out.println("����ɹ���=================��");
			request.put("flag", "success");
		}
	}

	/**
	 * ����û�id
	 * 
	 * @param request
	 * @param jbo
	 * @return
	 * @throws HandlerException
	 */
	private String getUseId(JSONObject request, JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		String customerId = "";
		String loanNo = (String) request.get("loanNo");
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.acct_loan");
			BizObjectQuery query = m.createQuery("select CUSTOMERID from o where SERIALNO=:SERIALNO");
			query.setParameter("SERIALNO", loanNo);
			BizObject o = query.getSingleResult(false);
			customerId = o.getAttribute("CUSTOMERID").getValue() == null ? "" : o.getAttribute("CUSTOMERID").getString();
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return customerId;
	}

	/**
	 * ����˻����
	 * 
	 * @param request
	 * @param jbo
	 * @throws HandlerException
	 */
	private double getAccountBalance(JSONObject request, JBOFactory jbo) throws HandlerException {
		BizObjectManager m;
		double balance = 0;
		String userId = request.get("userId").toString();
		try {
			m = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = m.createQuery(" USERID=:USERID");
			query.setParameter("USERID", userId);
			BizObject o = query.getSingleResult(false);
			// �˻����
			balance = o.getAttribute("USABLEBALANCE").getString() == null ? 0 : o.getAttribute("USABLEBALANCE").getDouble();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return balance;
	}

	/*
	 * ������Ϣ
	 */
	@Override
	protected Object responseObject(JSONObject request, JSONObject response, String logid, String transserialno, JBOFactory jbo) throws HandlerException {

		return response;
	}

}
