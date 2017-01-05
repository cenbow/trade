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
 * �����Ϣ��ѯ
 * ���������
 * 		ProjectSerialNo:�����
 * ��������� 
 * ����ƻ���
 *      SeqId����
 *      PAYDATE�����ֹ�� project
 *      PayCorpusAmt+PayInteAmt+PayFineAmt+PayFeeAmt1Ӧ���ܶ�(Ԫ)
 *      PayCorpusAmtӦ������(Ԫ)
 *      PayInteAmtӦ����Ϣ(Ԫ)
 *      PayFineAmtӦ�����ڷ�Ϣ(Ԫ)
 *      Status״̬
 */
public class InvestmentListSimpHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
	
		return getLoanInfo(request);
	}
	
	/**
	 * �����Ϣ��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getLoanInfo(JSONObject request)throws HandlerException {
		
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		
		if(request.get("ContractId")==null || "".equals(request.get("ContractId"))){
			throw new HandlerException("contractid.error");
		}
		 
		if(request.containsKey("PageSize"))
			this.pageSize = Integer.parseInt(request.get("PageSize").toString());
		if(request.containsKey("CurPage"))
			this.curPage = Integer.parseInt(request.get("CurPage").toString());
		
		String sUserID = request.get("UserID").toString();
		String sContractId = request.get("ContractId").toString();
		try{ 
			    JBOFactory jbo = JBOFactory.getFactory();
			    JSONObject sInvestmentStatusItems = GeneralTools.getItemName(jbo, "PaymentStatus");
			    JSONObject result = new JSONObject();
				BizObjectManager m =jbo.getManager("jbo.trade.acct_payment_schedule");
				BizObjectQuery query = m.createQuery(
						"select ps.objectno,ps.seqid,ps.paydate,ps.paycorpusamt," +
						" ps.payinteamt,ps.payfineamt,ps.status " +
						" from  jbo.trade.user_contract uc,jbo.trade.ti_contract_info ci," +
						" jbo.trade.acct_payment_schedule ps " +
						" where uc.userid=:userid and uc.relativetype='002' " +
						" and uc.contractid=:contractid and " +
						" uc.contractid=ci.contractid and ci.loanno=ps.objectno" +
						" order by ps.seqid");
				query.setParameter("userid",sUserID);
				query.setParameter("contractid",sContractId);
				int firstRow = curPage * pageSize;
				if(firstRow < 0){
					firstRow = 0;
				}
				int maxRow = pageSize;
				if(maxRow <= 0){
					maxRow = 10;
				}
				query.setFirstResult(firstRow);
				if(request.containsKey("PageSize"))
					query.setMaxResults(maxRow);
				
				int totalAcount = query.getTotalCount();
				int temp = totalAcount % pageSize;
				int pageCount = totalAcount / pageSize;
				if(temp != 0){
					pageCount += 1;
				}
				List<BizObject> list = query.getResultList(false);
				if (list != null) {
					JSONArray array = new JSONArray();
					for (int i = 0; i < list.size(); i++) {
						BizObject o = list.get(i);
						JSONObject obj = new JSONObject();
						String sStatusCode = o.getAttribute("STATUS").getValue()==null?
								"":o.getAttribute("STATUS").getString();
						double sPayCorpusAmt = Double.parseDouble(o.getAttribute("PAYCORPUSAMT").getValue()==null?
								"0":o.getAttribute("PAYCORPUSAMT").toString());
						double sPayInteAmt = Double.parseDouble(o.getAttribute("PAYINTEAMT").getValue()==null?
								"0":o.getAttribute("PAYINTEAMT").toString());
						double sPayFineAmt = Double.parseDouble(o.getAttribute("PAYFINEAMT").getValue()==null?
								"0":o.getAttribute("PAYFINEAMT").toString());
						double sActualSum = sPayCorpusAmt + sPayInteAmt + sPayFineAmt;
						String sPayDate = o.getAttribute("PAYDATE").getValue()==null?
								"":o.getAttribute("PAYDATE").getString();
						obj.put("ObjectNo", o.getAttribute("OBJECTNO").getValue()==null?
								"":o.getAttribute("OBJECTNO").getString());//��ݺ�
						
						obj.put("SeqId", o.getAttribute("SeqId").getValue()==null?
								0:o.getAttribute("SeqId").getInt());//����
						
						obj.put("PayDate", sPayDate);//�����ֹ�� 
						obj.put("ActualSum",GeneralTools.numberFormat(sActualSum, 0, 2));//Ӧ���ܶ�(Ԫ)
						obj.put("PayCorpusAmt", sPayCorpusAmt);//Ӧ������(Ԫ)
						obj.put("PayInteAmt", sPayInteAmt);//Ӧ����Ϣ(Ԫ)
						obj.put("PayFineAmt", sPayFineAmt);//Ӧ�����ڷ�Ϣ(Ԫ)
						obj.put("StatusCode", sStatusCode);//״̬ 
						JSONObject sInvestmentStatusInfo = getInvestmentStatusBelong(sStatusCode, sInvestmentStatusItems);//״̬  
						String sLoanStatusName = sInvestmentStatusInfo.get("InvestmentStatusName").toString();
						obj.put("StatusName", sLoanStatusName);//״̬ ����
						array.add(obj);
			}
					result.put("array", array);
				}
				result.put("RootType", "030");
				result.put("TotalAcount", String.valueOf(totalAcount));// �������´�����
				result.put("PageCount", String.valueOf(pageCount));
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryinvestmentsimplist.error");
		}
	}
	
	 /**
	  * ״ֵ̬ת��code_library
	  * @param jbo
	  * @param sSerialNo
	  * @param bankItems
	  * @return
	  * @throws HandlerException
	  */
	private JSONObject getInvestmentStatusBelong(String sInvestmentStatusItemNo, JSONObject sInvestmentStatusItems)
			throws HandlerException {
		try {
				String sInvestmentStatusName = sInvestmentStatusItems.containsKey(sInvestmentStatusItemNo) ? sInvestmentStatusItems
						.get(sInvestmentStatusItemNo).toString() : sInvestmentStatusItemNo;
				JSONObject obj = new JSONObject();
//				obj.put("AccountBelongCode", sLoanStatus);
				obj.put("InvestmentStatusName", sInvestmentStatusName);
				return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("queryinvestmentsimpstatusinfo.error");
		}
	}
}
