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
 * �տ���ϸ��ѯ
 * ���������
 * 		UserId:�û� ��
 * ��������� 
 * ���տ���ϸ�б�
 *      ContractId:��ͬ��
 *      PayDate:Ԥ�Ƶ�������ps
 *      ROJECTNAME|SERIALNO:��Ŀ���ƨO��� pi
 *      PayCorpusAmt+PayInteAmt:Ԥ���տ�Ԫ��ps
 *      PayCorpusAmt:Ԥ�ձ���ps
 *      PayInteAmt:Ԥ����Ϣ ps
 *���Ǳ��������
 * 		PageSize��ÿҳ������;
 *		CurPage����ǰҳ;
 */
public class PaymentScheduleReceiveListHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	private String sStartDate;
	private String sEndDate;
	private String sQuerySql;
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
	
		return getPaymentScheduleReceiveList(request);
	}
	
	/**
	 * �տ���ϸ��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getPaymentScheduleReceiveList(JSONObject request)throws HandlerException {
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		
		if(request.containsKey("StartDate"))
			this.sStartDate =  request.get("StartDate").toString();
		if(request.containsKey("EndDate"))
			this.sEndDate =  request.get("EndDate").toString();
		//��ȡpageSizeÿҳ��������curPage��ǰ����ҳ
		if(request.containsKey("pageSize"))
			this.pageSize = Integer.parseInt(request.get("pageSize").toString());
		if(request.containsKey("pageNo"))
			this.curPage = Integer.parseInt(request.get("pageNo").toString());
		String sUserID = request.get("UserID").toString();
		
		try{ 
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.project_info");
			
			sQuerySql= " uc.userid=:userid and " +
					   " uc.relativetype='002' and " +
					   " uc.contractid=pi.contractid and " +
					   " uc.contractid=bc.serialno and " +
			           " bc.serialno=ps.contractno and " +
			           " bc.serialno=li.contractserialno and li.loanstatus in ('0','1') and uc.subcontractno=ps.subcontractno" ;
			
			if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
				sQuerySql = sQuerySql + " and ps.paydate between :startdate and :enddate ";
			} 
			
			BizObjectQuery query = m.createQuery(
					"select pi.projectname,pi.contractid,pi.serialno,uc.SUBCONTRACTNO, " +
					"ps.PayDate,ps.PayCorpusAmt,ps.PayInteAmt " +
					" from jbo.trade.user_contract uc, jbo.trade.business_contract bc," +
					" jbo.trade.project_info pi,jbo.trade.income_schedule ps," +
					" jbo.trade.acct_loan li  " +
					" where " + sQuerySql +
					" order by ps.PayDate " 
					);
			query.setParameter("userid", sUserID);
			
			if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
				query.setParameter("startdate", sStartDate);
				query.setParameter("enddate", sEndDate);
			} 
			 //��ҳ
            int totalAcount = query.getTotalCount();
    		int pageCount = (totalAcount + pageSize - 1) / pageSize;
    		if (curPage > pageCount)
    			curPage = pageCount;
    		if (curPage < 1)
    			curPage = 1;
    		query.setFirstResult((curPage - 1) * pageSize);
    		query.setMaxResults(pageSize);
			
			List<BizObject> list = query.getResultList(false);
			JSONObject result = new JSONObject();
			if (list != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					String sPojectName = o.getAttribute("PROJECTNAME").toString()==null?
							"":o.getAttribute("PROJECTNAME").toString();
					String sSerialNo1 = o.getAttribute("SERIALNO").toString()==null?
							"":o.getAttribute("SERIALNO").toString();
					String sProjectNameSerialNo = sPojectName + sSerialNo1;
					double sPayCorpusAmt = Double.parseDouble(o.getAttribute("PAYCORPUSAMT").toString()==null?
							"0":o.getAttribute("PAYCORPUSAMT").toString());
					double sPayInteAmt = Double.parseDouble(o.getAttribute("PAYINTEAMT").toString()==null?
							"0":o.getAttribute("PAYINTEAMT").toString());
					double sPaySum = sPayCorpusAmt + sPayInteAmt;
					
					JSONObject obj = new JSONObject();
					obj.put("ContractId", o.getAttribute("SUBCONTRACTNO").getValue()==null?
							"":o.getAttribute("SUBCONTRACTNO").getString());//��ͬ��
					obj.put("ProjectNameSerialNo", sProjectNameSerialNo);//��Ŀ���ƨO���
					obj.put("PayDate", o.getAttribute("PAYDATE").getValue()==null?
							"":o.getAttribute("PAYDATE").getString());//Ԥ�Ƶ�������
					obj.put("PaySum", GeneralTools.numberFormat(sPaySum, 0, 2));//Ԥ���տ�Ԫ��
					obj.put("PayCorpusAmt", GeneralTools.numberFormat(sPayCorpusAmt, 0, 2));//Ԥ�ձ���
					obj.put("PayInteAmt", GeneralTools.numberFormat(sPayInteAmt, 0, 2));//Ԥ����Ϣ
					array.add(obj);
				}
				result.put("RootType", "030");
				result.put("TotalAcount", String.valueOf(totalAcount));
				result.put("curPage", String.valueOf(curPage));
				result.put("pagesize", String.valueOf(pageSize));
				result.put("array", array);
			}
			
			//�����ٱ�
            BizObjectManager m1 =jbo.getManager("jbo.trade.acct_loan");
            BizObjectQuery query1 = m1.createQuery(
					"select count(1) as v.Cnt " +
					" from jbo.trade.user_contract uc, jbo.trade.business_contract ci," +
					" jbo.trade.project_info pi,jbo.trade.income_schedule ps," +
					" jbo.trade.acct_loan li " +
					" where uc.userid=:userid and uc.relativetype='002' " +
					 " and uc.contractid=pi.contractid and " +
					   " uc.contractid=ci.serialno and " +
			           " ci.serialno=ps.contractno AND ci.serialno=li.contractserialno " +
					" and li.loanstatus in ('0','1') and uc.subcontractno=ps.subcontractno" 
					);
			query1.setParameter("userid", sUserID);
			BizObject o1 = query1.getSingleResult(false);
			if(o1!=null){
				result.put("Cnt", o1.getAttribute("Cnt").getValue()==null?
			    		 0:o1.getAttribute("Cnt").getInt());
			}else{
				result.put("Cnt", 0);
			}
			     
			
			//Ԥ���տ��  Ԥ�ձ���   Ԥ����Ϣ     
		    BizObjectManager m2 =jbo.getManager("jbo.trade.acct_payment_schedule");
			BizObjectQuery query2 = m2.createQuery(
					 "select sum(ps.paycorpusamt) as v.paycorpusamt,sum(ps.payinteamt) as v.payinteamt," +
					 "sum(ps.paycorpusamt+ps.payinteamt) as v.PaySum " +
					 " from jbo.trade.user_contract uc, jbo.trade.business_contract ci," +
					 " jbo.trade.project_info pi,jbo.trade.income_schedule ps," +
					 " jbo.trade.acct_loan li " +
					 " where uc.userid=:userid and uc.relativetype='002' " +
					 " and uc.contractid=pi.contractid and " +
					   " uc.contractid=ci.serialno and " +
			           " ci.serialno=ps.contractno AND ci.serialno=li.contractserialno " +
					" and li.loanstatus in ('0','1') and uc.subcontractno=ps.subcontractno" 
						);
			query2.setParameter("userid", sUserID);
			BizObject o2 = query2.getSingleResult(false);
			if(o2!=null){
				double sPayCorpusAmt = Double.parseDouble(o2.getAttribute("PAYCORPUSAMT").toString()==null?
						"0":o2.getAttribute("PAYCORPUSAMT").toString());
				double sPayInteAmt = Double.parseDouble(o2.getAttribute("PAYINTEAMT").toString()==null?
						"0":o2.getAttribute("PAYINTEAMT").toString());
				double sPaySum = Double.parseDouble(o2.getAttribute("PAYSUM").toString()==null?
						"0":o2.getAttribute("PAYSUM").toString());
			     result.put("PaySum", GeneralTools.numberFormat(sPaySum, 0, 2));
			     result.put("PayCorpusAmt", GeneralTools.numberFormat(sPayCorpusAmt, 0, 2));
			     result.put("PayInteAmt",GeneralTools.numberFormat(sPayInteAmt, 0, 2));
			}else{
				 result.put("PaySum", 0);
			     result.put("PayCorpusAmt", 0);
			     result.put("PayInteAmt",0);
			}
			
			return result;
		} 
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("paymentschedulereceivelist.error");
		}
	}
}
