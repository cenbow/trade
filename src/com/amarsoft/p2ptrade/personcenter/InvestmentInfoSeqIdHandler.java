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
import com.amarsoft.p2ptrade.tools.GeneralTools;
/**
 * Ͷ����Ϣ��ѯ
 * ���������
 * 		ProjectSerialNo:�����
 * ��������� 
 *      FINISHDATEʵ���տ���
 *      ACTUALPAYCORPUSAMT+ACTUALPAYINTEAMT+ACTUALFINEAMTʵ���ܶԪ��
 *      ACTUALPAYCORPUSAMT����(Ԫ)
 *      ACTUALPAYINTEAMT��Ϣ(Ԫ)
 *      ACTUALFINEAMT���ڷ�Ϣ(Ԫ)
 */
public class InvestmentInfoSeqIdHandler extends JSONHandler {
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
	
		return getLoanInfoSeqId(request);
	}
	
	/**
	 * �����Ϣ��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getLoanInfoSeqId(JSONObject request)throws HandlerException {
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		
		if(request.get("SeqId")==null || "".equals(request.get("SeqId"))){
			throw new HandlerException("seqid.error");
		}
		
		if(request.get("LoanNo")==null || "".equals(request.get("LoanNo"))){
			throw new HandlerException("loanno.error");
		}
		
		String sUserID = request.get("UserID").toString();
		String sLoanNo = request.get("LoanNo").toString();
		String sSeqId = request.get("SeqId").toString();
		
		try{ 
			JBOFactory jbo = JBOFactory.getFactory();
			JSONObject result = new JSONObject();
            BizObjectManager m =jbo.getManager("jbo.trade.user_contract");
            
            BizObjectQuery query = m.createQuery(
					"select bd.seqid,bd.loanserialno,bd.actualpaydate,bd.actualpaycorpusamt," +
					" bd.actualpayinteamt,bd.actualfineamt  " +
					" from jbo.trade.user_contract uc,jbo.trade.ti_contract_info ci," +
					" jbo.trade.acct_back_detail bd " +
					" where uc.userid=:userid and uc.relativetype='002' " +
					" and bd.loanserialno=:objectno and bd.seqid=:seqid" +
					" and uc.contractid=ci.contractid and ci.loanno=bd.loanserialno ");
			
            query.setParameter("userid",sUserID);
			query.setParameter("objectno",sLoanNo);
			query.setParameter("seqid",sSeqId);
			 
			List<BizObject> list = query.getResultList(false);
			if (list != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();
					double sActualPayCorpusAmt = Double.parseDouble(
							o.getAttribute("ACTUALPAYCORPUSAMT").toString()==null?
									"0":o.getAttribute("ACTUALPAYCORPUSAMT").toString());
					double sActualPayInteAmt = Double.parseDouble(
							o.getAttribute("ACTUALPAYINTEAMT").toString()==null?
									"0":o.getAttribute("ACTUALPAYINTEAMT").toString());
					double sActualFineAmt = Double.parseDouble(
							o.getAttribute("ACTUALFINEAMT").toString()==null?
									"0":o.getAttribute("ACTUALFINEAMT").toString()); 
					double sActualPaySum = sActualPayCorpusAmt + sActualPayInteAmt + sActualFineAmt ;
					obj.put("FinishDate", o.getAttribute("ACTUALPAYDATE").getValue()==null?
							"":o.getAttribute("ACTUALPAYDATE").getString());//ʵ���տ���
					obj.put("ActualSum",GeneralTools.numberFormat(sActualPaySum, 0, 2));//ʵ���ܶԪ��
					obj.put("ActualPayCorpusAmt", sActualPayCorpusAmt);//����(Ԫ)
					obj.put("ActualPayInteAmt", sActualPayInteAmt);//��Ϣ(Ԫ)
					obj.put("ActualFineAmt", sActualFineAmt);//���ڷ�Ϣ(Ԫ)
					array.add(obj); 
		}
				result.put("RootType", "020");
				result.put("array", array);
			}
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryinvestmentinfoseqid.error");
		}
	}
}
