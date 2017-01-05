package com.amarsoft.p2ptrade.loan;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/**
 * ��û���ҳ����Ϣ
 */
public class MyLoanDetailHandler extends JSONHandler {
static{
	Parser.registerFunction("add_months");
	Parser.registerFunction("to_date");
	Parser.registerFunction("nvl");
}
	@Override
	public Object createResponse(JSONObject request, Properties params) throws HandlerException {
		if(isCurrentUser(request)){
			return getMyRepayingInfo(request);
		}
		//�������뵱ǰ�û��޹أ��򷵻ؿ�
		return new JSONObject();
	}

	/**
	 * �жϲ�ѯ����Ƿ��뵱ǰ�û�����
	 * @param request
	 * @return
	 */
	private boolean isCurrentUser(JSONObject request) {
		boolean flag=false;
		String userId=(null==request.get("userId")?"":request.get("userId").toString());
		String loanNo=(null==request.get("loanNo")?"":request.get("loanNo").toString());
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		try {
			String table="jbo.trade.acct_loan";
			String sql = "SELECT CUSTOMERID FROM O WHERE SERIALNO=:loanNo";
			m = f.getManager(table);
			BizObjectQuery q = m.createQuery(sql);
			BizObject userInBaseB = q.setParameter("loanNo", loanNo).getSingleResult(false);
			if(userInBaseB==null){return false;}
			else if(!userId.equals(userInBaseB.getAttribute("CUSTOMERID").toString())){
				return false;
			}else{
				flag=true;
			}
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return flag;
	}

	@SuppressWarnings("unchecked")
	private JSONObject getMyRepayingInfo(JSONObject request) {
		JSONObject result = new JSONObject();// �ҵĴ�����Ϣ�Ľ��
		JSONObject loanInfo = new JSONObject();// ������Ϣ
		JSONObject repayTotalInfo = new JSONObject();// �ܻ�������
		JSONArray repayPerTermArray = new JSONArray();// ÿ�ڻ�������
		String loanNo = (String) request.get("loanNo");
		float payCorpusamtTotal = 0;// ʵ���ܻ�����
		float payingTeamtTotal = 0;// ʵ���ܻ���Ϣ
		float fineamtTotal = 0;// ʵ���ܷ�Ϣ
		int curSeqId = 0;// ��ǰ��������
		try {
			BizObject projectInfoResult = getProjectInfoResult(loanNo);// ��Ŀ��Ϣsql���
			
			List<BizObject> payDetailResult = getPayDetailResultList(loanNo);// ������ϸsql���
			for (BizObject bizO : payDetailResult) {
				JSONObject repayPerTerm = new JSONObject();
				float actualPayCorpusamt = Float.parseFloat(bizO.getAttribute("ACTUALPAYCORPUSAMT").toString());
				float actualPayinTeamt = Float.parseFloat(bizO.getAttribute("ACTUALPAYINTEAMT").toString());
				float actualFineamt = Float.parseFloat(bizO.getAttribute("ACTUALFINEAMT").toString());// ʵ�ʷ�Ϣ
				float payCorpusamt = Float.parseFloat(bizO.getAttribute("PAYCORPUSAMT").toString());
				float payinTeamt = Float.parseFloat(bizO.getAttribute("PAYINTEAMT").toString());
				float payFineamt = Float.parseFloat(bizO.getAttribute("PAYFINEAMT").toString());// Ӧ����Ϣ
				payCorpusamtTotal += actualPayCorpusamt;
				payingTeamtTotal += actualPayinTeamt;
				fineamtTotal += actualFineamt;
				
				boolean isPay = (actualPayCorpusamt + actualPayinTeamt + actualFineamt == payCorpusamt + payinTeamt + payFineamt);
//				curSeqId = bizO.getAttribute("SEQID") == null ? 0: bizO.getAttribute("SEQID").getInt();
				curSeqId++;
				// ����ڻ����������������
				repayPerTerm.put("seqId", bizO.getAttribute("SEQID") == null ? 0 : bizO.getAttribute("SEQID").getInt());// ����
				repayPerTerm.put("payDate", bizO.getAttribute("PAYDATE") == null ? "" : bizO.getAttribute("PAYDATE").toString());// �����ֹ����
				repayPerTerm.put("payTotal", payCorpusamt + payinTeamt + payFineamt);// Ӧ���ܶ����+��Ϣ
				repayPerTerm.put("payCorpusamt", bizO.getAttribute("PAYCORPUSAMT") == null ? "" : bizO.getAttribute("PAYCORPUSAMT").toString());// Ӧ������
				repayPerTerm.put("payinTeamt", bizO.getAttribute("PAYINTEAMT") == null ? "" : bizO.getAttribute("PAYINTEAMT").toString());// Ӧ����Ϣ
				repayPerTerm.put("payFineamt", bizO.getAttribute("PAYFINEAMT") == null ? "" : bizO.getAttribute("PAYFINEAMT").toString());// Ӧ�����ڷ�Ϣ
				if (isPay) {// �жϻ���״̬
					repayPerTerm.put("status", "�Ѹ���");// ״̬
				} else {
					repayPerTerm.put("status", "δ����");// ״̬
				}
				repayPerTermArray.add(repayPerTerm);
			}
			// ���ܻ����������������
			repayTotalInfo.put("payCorpusamtTotal", payCorpusamtTotal);// �ܱ���
			repayTotalInfo.put("payingTeamtTotal", payingTeamtTotal);// ����Ϣ
			repayTotalInfo.put("fineamtTotal", fineamtTotal);// �ܷ�Ϣ
			repayTotalInfo.put("paymentTotal", payCorpusamtTotal + payingTeamtTotal + fineamtTotal);// �ܻ���
			// ����Ŀ�������������
			if(null!=projectInfoResult){
			loanInfo.put("serialNo", projectInfoResult.getAttribute("SERIALNO") == null ? "" : projectInfoResult.getAttribute("SERIALNO").toString());// ��Ŀ���
			loanInfo.put("projectName", projectInfoResult.getAttribute("PROJECTNAME") == null ? "" : projectInfoResult.getAttribute("PROJECTNAME").toString());// ��Ŀ����
			loanInfo.put("rateDate", projectInfoResult.getAttribute("RATEDATE") == null ? "" : projectInfoResult.getAttribute("RATEDATE").toString());// ��ʼ���ڣ���Ϣ��
			//add_months(to_date(o.applytime,'yyyy/mm/dd'����������
			loanInfo.put("endDate", projectInfoResult.getAttribute("ENDDATE") == null ? "" : projectInfoResult.getAttribute("ENDDATE").toString());// ��������
			int loanTerm=projectInfoResult.getAttribute("LOANTERM") == null ? 0 : projectInfoResult.getAttribute("LOANTERM").getInt();
			loanInfo.put("loanTerm", loanTerm);// Ͷ������
			loanInfo.put("remianSeq", loanTerm-curSeqId);// ��ǰ����
			loanInfo.put("loanMount", projectInfoResult.getAttribute("LOANAMOUNT") == null ? "" : projectInfoResult.getAttribute("LOANAMOUNT").toString());// ����
			loanInfo.put("capitalRemain", Float.parseFloat(projectInfoResult.getAttribute("LOANAMOUNT").toString()) - payCorpusamtTotal);// ʣ�౾��
			loanInfo.put("status", projectInfoResult.getAttribute("STATUS") == null ? "" : projectInfoResult.getAttribute("STATUS").toString());// ״̬
			}
		} catch (JBOException e) {
			e.printStackTrace();
		}
		result.put("loanInfo", loanInfo);
		result.put("repayTotalInfo", repayTotalInfo);
		result.put("repayPerTermArray", repayPerTermArray);
		return result;
	}

/*
 * ��ȡ��Ŀ��Ľ����
 */
	private BizObject getProjectInfoResult(String param) {
		BizObject result = null;
		JBOFactory factory = JBOFactory.getFactory();
		BizObjectManager manager;
		try {
			manager = factory.getManager("jbo.trade.project_info");
			String sql="select  o.SERIALNO,o.PROJECTNAME,o.RATEDATE,o.LOANTERM,o.LOANAMOUNT,o.STATUS,add_months(to_date(nvl(o.RATEDATE,'1990/01/01 00:00:00'),'yyyy/mm/dd hh24:mi:ss'),nvl(o.LOANTERM,0)) AS V.ENDDATE from o,jbo.trade.business_contract ci where o.CONTRACTID = ci.serialno and ci.RELATIVESERIALNO=:loanNo";
			BizObjectQuery query = manager.createQuery(sql);
			query.setParameter("loanNo", param);
			result = query.getSingleResult(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return result;
	}
	/*
	 * ����ƻ��ͻ�����ϸ������
	 */
	@SuppressWarnings("unchecked")
	private List<BizObject> getPayDetailResultList(String param) {
		List<BizObject> result = null;
		JBOFactory factory = JBOFactory.getFactory();
		BizObjectManager manager;
		try {
			manager = factory.getManager("jbo.trade.acct_back_detail");
			BizObjectQuery query = manager
					.createQuery("select  o.ACTUALPAYCORPUSAMT,o.ACTUALPAYINTEAMT,o.ACTUALFINEAMT,ps.PAYCORPUSAMT,ps.PAYINTEAMT,ps.PAYFINEAMT,ps.SEQID,ps.PAYDATE from o , jbo.trade.acct_payment_schedule ps where o.PSSERIALNO=ps.SERIALNO and o.LOANSERIALNO=:loanSerialNo");
			query.setParameter("loanSerialNo", param);
			result=query.getResultList(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return result;
	}
}
