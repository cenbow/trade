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
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.web.service.imp.transclass.JsptoTrans;
import com.amarsoft.web.service.imp.transclass.transformobject.OPayInfoRes;
import com.amarsoft.web.service.imp.util.TransTools;

/**
 * @ ��������ӿ� 2014-5-16 ���� Paytype �������ͣ�1һ�㻹��,3��ǰ���� ��ͬ�� ���
 * ������Ϣ����Ϣ��ƽ̨����ѡ������ѡ�ƽ̨����ѡ���������ѡ���ǰ����ΥԼ���˻����
 */
public class PaymentInfoConsultHandler extends TradeHandler {
	static {
		Parser.registerFunction("to_date");
		Parser.registerFunction("min");

	}

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
		// else if(!request.get("userId").equals(getUseId(request, jbo))){
		// throw new HandlerException("request.invalid");
		// }
		String paytypeString = (String) request.get("PayType");
		if ("3".equals(paytypeString)) {
			request.put("Method", "REPaymentConsult");
		} else if ("1".equals(paytypeString)) {
			request.put("Method", "PaymentConsult");
		}
		validateInfo(request, jbo);
		return request;
	}

	/**
	 * ���ýӿڱ������ݣ����ж��Ƿ���Ҫ��ֵ
	 * 
	 * @param request
	 * @param jbo
	 * @throws HandlerException 
	 */
	@SuppressWarnings("unchecked")
	private void validateInfo(JSONObject request, JBOFactory jbo) throws HandlerException {
		JSONObject result = new JSONObject();
		JsptoTrans jt = new JsptoTrans();
		try {
			jt.setConn(ARE.getDBConnection("als"));// ����
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
		String loanNo = "TEST20140916";
		int seqId=3;
//		String loanNo = request.get("loanNo").toString();
//		int seqId = getCurrentSeq(request, jbo);// ���Գ�ʼ��ֵ������Ϊ0
		String serialNo = loanNo + seqId;
		jt.setMethod(request.get("Method").toString());// ���׷�ʽ
		jt.setObjectNo(loanNo);// �����˺� ��ݺ�
		jt.setAmt("1000");// ������ ����ʱ�������
		jt.setObjectType("");
		jt.setSerialNo(serialNo);
		OPayInfoRes opir = (OPayInfoRes) jt.runPayment();
		String stype = opir.getReturnType();
		//����Ƿ��д���
		if (null!=stype&&!"0000".equals(stype)) {
			System.out.println(TransTools.getErrorMss(stype));
		} else {
			String actualPayCorpusAmt = opir.getActualPayCorpusAmt()==null?"0":opir.getActualPayCorpusAmt().toString();// ����
			String actualPayFineAmt = opir.getActualPayFineAmt()==null?"0":opir.getActualPayFineAmt().toString();// ��Ϣ
			String actualPayInteAmt = opir.getActualPayInteAmt()==null?"0":opir.getActualPayInteAmt().toString();// ��Ϣ
			String plantMange = opir.getPlantmange()==null?"0":opir.getPlantmange().toString();// ƽ̨�����
			String plantfee = opir.getPlantfee()==null?"0":opir.getPlantfee().toString();// ƽ̨�����
			String managefee = opir.getManagefee()==null?"0":opir.getManagefee().toString();// ������
			String insureManagementFee = opir.getInsuremanagement_fee()==null?"0":opir.getInsuremanagement_fee().toString();// ���������
			String penalValue = opir.getPenal_value()==null?"0":opir.getPenal_value().toString();// ΥԼ��
			String payAmt = opir.getPayamt()==null?"0":opir.getPayamt().toString();// �ܽ��
			
			double total = Double.parseDouble(actualPayCorpusAmt) + Double.parseDouble(actualPayFineAmt) + Double.parseDouble(actualPayInteAmt)
					+ Double.parseDouble(managefee) + Double.parseDouble(plantfee) + Double.parseDouble(plantMange) + Double.parseDouble(insureManagementFee)
					+ Double.parseDouble(penalValue);
			double accountBalance = 0;
			try {
				accountBalance = getAccountBalance(request, jbo);
			} catch (HandlerException e) {
				e.printStackTrace();
			}
			if (total > accountBalance) {
				result.put("needCharge", "t");
			} else {
				result.put("needCharge", "f");
			}
			result.put("actualPayCorpusAmt", actualPayCorpusAmt);// ����
			result.put("actualPayInteAmt", actualPayInteAmt);// ��Ϣ
			result.put("actualPayFineAmt", actualPayFineAmt);// ��Ϣ
			result.put("plantfee", plantfee);// ƽ̨�����
			result.put("plantMange", plantMange);// ƽ̨�����
			result.put("managefee", managefee);// ������
			result.put("insureManagementFee", insureManagementFee);// ���������
			result.put("penalValue", penalValue);// ΥԼ��
			result.put("payAmt", payAmt);// �ܽ��
			result.put("total", total);// �뻹�ܽ��
			result.put("accountBalance", accountBalance);// �˻��ܽ��
			request.put("paymentInfo", result);
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

	/**
	 * ��ȡ��ǰ�ڴ�
	 * 
	 * @param request
	 * @param jbo
	 * @return
	 * @throws HandlerException
	 */
	private int getCurrentSeq(JSONObject request, JBOFactory jbo) throws HandlerException {
		BizObjectManager m;
		String loanNo = request.get("loanNo").toString();// ��ݺ�
		int currentSeq = 0;// ��ǰ�ڴ�
		String table = "jbo.trade.acct_payment_schedule";
		String sql = "select o.seqid,o.papdate from o where o.OBJECTNO=:loanNo and to_date(o.paydate,'yyyy/MM/dd')=(select min(to_date(o.paydate,'yyyy/MM/dd')) from o where to_date(o.paydate,'yyyy/MM/dd')>sysdate)";
		try {
			m = jbo.getManager(table);
			BizObjectQuery query = m.createQuery(sql);
			query.setParameter("loanNo", loanNo);
			BizObject o = query.getSingleResult(false);
			// �˻����
			currentSeq = o.getAttribute("USABLEBALANCE").getString() == null ? 0 : o.getAttribute("USABLEBALANCE").getInt();
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return currentSeq;

	}

	/*
	 * ������Ϣ
	 */
	@Override
	protected Object responseObject(JSONObject request, JSONObject response, String logid, String transserialno, JBOFactory jbo) throws HandlerException {

		return response;
	}

}
