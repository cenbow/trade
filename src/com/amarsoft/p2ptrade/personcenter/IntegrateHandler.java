package com.amarsoft.p2ptrade.personcenter;

/*
 * ���ϵ�Handler����һ��ҳ���е��õĶ��Handler�ϲ�����һ��Handler��ִ��
 * 
 * @author by cyliu at 2014.8.25
 * */

import java.util.Properties;

import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class IntegrateHandler extends JSONHandler {

	// ���ڽ��ܱ�ʾ������Щ����Handler�Ĳ���
	private int runType = 0;

	String rootTypeStat = "";
	String rootTypeList = "";

	// ��ŷ��صĽ��
	JSONObject result = new JSONObject();

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		runType = Integer.valueOf((String) request.get("RunType"));
		switch (runType) {
		// ������-�ҵ�����P2P���
		case 1:
			return getLoanInfo(request, arg1);
		// Ͷ�ʹ���-�ҵ�Ͷ��
		case 2:
			return getInvestInfo(request, arg1);
		default:
			return "";
		}
	}

	private Object getLoanInfo(JSONObject request, Properties arg)
			throws HandlerException {
		LoanInfoSumHandler loanInfoSumHandler = new LoanInfoSumHandler();
		LoanListHandler listHandler = new LoanListHandler();

		// ���ͳ��
		JSONObject sResponseLoanStat = (JSONObject) loanInfoSumHandler
				.createResponse(request, arg);
		rootTypeStat = (String) sResponseLoanStat.get("RootType");
		if (rootTypeStat.equals("010")) {
			result.put("responseLoanStat", sResponseLoanStat); // ���ͳ����Ϣ
		}

		// ����б���Ϣ��ѯ
		JSONObject sResponseLoanList = (JSONObject) listHandler.createResponse(
				request, arg);
		rootTypeList = (String) sResponseLoanList.get("RootType");
		if (rootTypeList.equals("020")) {
			result.put("responseLoanList", sResponseLoanList); // ����б���Ϣ
		}

		return result;
	}

	private Object getInvestInfo(JSONObject request, Properties arg)
			throws HandlerException {

		InvestmentStatHandler investmentStatHandler = new InvestmentStatHandler();
		InvestmentListHandler investmentListHandler = new InvestmentListHandler();

		// Ͷ��ͳ��
		JSONObject sResponseInvestStat = (JSONObject) investmentStatHandler
				.createResponse(request, arg);
		rootTypeStat = (String) sResponseInvestStat.get("RootType");
		if (rootTypeStat.equals("010")) {
			result.put("responseInvestStat", sResponseInvestStat);// Ͷ��ͳ����Ϣ
		}

		// Ͷ���б���Ϣ
		JSONObject sResponseInvestList = (JSONObject) investmentListHandler
				.createResponse(request, arg);
		rootTypeList = (String) sResponseInvestList.get("RootType");
		if (rootTypeList.equals("030")) {
			result.put("responseInvestList", sResponseInvestList); // Ͷ���б���Ϣ
		}

		return result;
	}
}
