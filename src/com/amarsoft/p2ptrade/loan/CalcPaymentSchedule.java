package com.amarsoft.p2ptrade.loan;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.app.accounting.entity.BusinessObject;
import com.amarsoft.app.accounting.entity.BusinessObjectPK;
import com.amarsoft.app.accounting.util.ACCOUNT_CONSTANTS;
import com.amarsoft.app.accounting.util.DateTools;
import com.amarsoft.app.accounting.util.LoanTools;
import com.amarsoft.app.accounting.util.NumberTools;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/**
 * ��������ƻ���ȡ��ԭ�еĴ���ʽ
 * */
public class CalcPaymentSchedule extends JSONHandler{

	@Override
	public Object createResponse(JSONObject json, Properties arg1)
			throws HandlerException {
		JSONObject result = new JSONObject();
		// ��ȡ������
		String amount = json.get("amount").toString();
		// ��ȡ��������
		String sLoanTerm = json.get("year").toString();
		// ��ȡ���ʽ
		String paymethod = json.get("ways").toString();
		// ��ȡ��������
		String businessRate = json.get("businessRate").toString();
		// ��ȡ�¹����
		String monthfee = json.get("monthfee").toString();
		// ��ȡһ�����շ�
		String lonelyfee = json.get("lonelyfee").toString();

		NumberFormat formatter = new DecimalFormat("#0.00");
		//��ʼ����Ϣ
		try {
			if(amount==null||"".equals(amount)) amount = "0";
			if(sLoanTerm==null||"".equals(sLoanTerm)) amount = "0";
			if(businessRate==null||"".equals(businessRate)) businessRate = "0.000001";
			double businessSum = Double.parseDouble(amount);
			double loanRate = Double.parseDouble(businessRate);
			int loanTerm = Integer.parseInt(sLoanTerm);
			
			String beginDate = StringFunction.getToday("/");
			String endDate = DateTools.getRelativeDate(beginDate, ACCOUNT_CONSTANTS.TERM_UNIT_MONTH, loanTerm);
			
			BusinessObject loan = new BusinessObject();
			
			getRepaySchedule(loan,
					businessSum,
					beginDate,
					endDate,
					loanRate,
					paymethod
			);
			//���㻹��ƻ�
			ArrayList<BusinessObject> rps = LoanTools.getPaymentSchedules(loan, endDate);
			//��Ϣ�ϼ�
			double dInteSum = 0.0;
			
			JSONArray calclist = new JSONArray();
			for(int i = 0 ; i < rps.size() ; i++)
			{
				
				BusinessObject ps = rps.get(i);
				HashMap<String,Object> rpAttribute = ps.getAllAttributes();
				
				double dCorpus =  Double.parseDouble(String.valueOf(rpAttribute.get("PAYCORPUSAMT")));
				double dInte =  Double.parseDouble(String.valueOf(rpAttribute.get("PAYINTEAMT"))); 
				double dBalance =  Double.parseDouble(String.valueOf(rpAttribute.get("CORPUSBALANCE"))); 
				String seqId =  String.valueOf(rpAttribute.get("SEQID")); 
				
				dInteSum = NumberTools.round(dInteSum+dInte, 2);
				
				JSONObject calc = new JSONObject();
				calc.put("order", seqId);// �ڴ�
				calc.put("MonthInterest", DataConvert.toMoney(dInte));// ÿ����Ϣ
				calc.put("MonthSum", DataConvert.toMoney(dCorpus));// ÿ�±���
				calc.put("MonthPay", DataConvert.toMoney(NumberTools.round(dInte+dCorpus, 2)));// ÿ�»����ܶ�
				calc.put("overplus", DataConvert.toMoney(dBalance));// ʣ�౾��
				calclist.add(calc);
			}
			
			
			String repay_total_string = formatter.format(NumberTools.round(dInteSum+businessSum, 2));
			result.put("total_loan", DataConvert.toMoney(businessSum));
			result.put("total_month", loanTerm);
			result.put("repay_month", loanTerm);
			result.put("repay", DataConvert.toMoney(dInteSum));
			result.put("repay_total", DataConvert.toMoney(NumberTools.round(dInteSum+businessSum, 2)));
			
			result.put("repay_total_string", repay_total_string);

			result.put("tot_int", 0);
			result.put("manageefee", 0.0);
			result.put("onepayfee", 0.0);
			result.put("repay_first", 0.0);
			result.put("month_intrest", 0.0);
			result.put("currway", 0.0);
			result.put("currcal", 0.0);
			
			result.put("calclist", calclist);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	
	/**
	 * ���㻹��ƻ�
	 * */
	private static final void getRepaySchedule(BusinessObject loan,
			double businessSum,
			String beginDate,
			String endDate,
			double loanRate,
			String payMethod
			) throws Exception
	{
		loan.setId(new BusinessObjectPK("Loan"));
		//���ý��
		loan.setAttribute("NormalBalance", businessSum);
		//��������
		loan.setAttribute("PUTOUTDATE", beginDate);
		loan.setAttribute("MATURITYDATE", endDate);
		
		//��������
		loan.setAttribute("LOANRATE", loanRate);
		loan.setAttribute("LOANRATEMODE", "2");
		loan.setAttribute("LOANRATECODE", "01");
		loan.setAttribute("BASEDAYS", 360);
		
		//TODO 
		if("RPT000040".equals(payMethod)||"RPT000050".equals(payMethod))
			loan.setAttribute("INTEBEARINGTYPE", "0");
		else if("RPT000010".equals(payMethod)||"RPT000020".equals(payMethod))
			loan.setAttribute("INTEBEARINGTYPE", "2");
		
		loan.setAttribute("Lastintedate", loan.getAttributeValue("PUTOUTDATE"));
		loan.setAttribute("PMTMaturity", loan.getAttributeValue("MATURITYDATE"));
		//loan.setAttribute("DEFAULTPAYDATE", "20");
		
//		loan.setAttribute("LOANRATEFLOATTYPE", "2");
		//���û��ʽ
		loan.setAttribute("RepaymentMethod",  payMethod);
		loan.setAttribute("ISPAYCURRENTMONTH", ACCOUNT_CONSTANTS.NO);
		loan.setAttribute("REPAYMENTCYCLE", "1");
		
		loan.setAttribute("INOFFFLAG", "1");
		
		
	}
	
}
