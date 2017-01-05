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
 * 		UserId:�û� ��
 * ��������� 
 * ����б�
 *      PROJECTNAME|SERIALNO��Ŀ����|�����project
 *      ����ʱ��PUTOUTDATE loan
 *      ����ʱ��MATURITYDATE loan
 *      BUSINESSSUM����(Ԫ) loan 
 *      �����ܶ�(Ԫ)PAYCORPUSAMT+PAYINTEAMT+PAYFINEAMT+PAYFEEAMT1+PAYFEEAMT2 payment
 *      loan.serialno=payment.objectno
 *      acct_payment_schedule.objectno=ti_contract_info.loanno
 *      ti_contract_info.contractid=project.contractid
 *      
 * ��Ŀ������Ϣ��
 *      PROJECTNAME|SERIALNO��Ŀ����|�����
 *      ������PUTOUTDATE-MATURITYDATE
 *      ������max(MATURITYDATE)
 *      BUSINESSSUM����(Ԫ) loan
 *      ʵ�ʻ����ܶ�(Ԫ) ʵ
 *      LOANSTATUS״̬ loan
 * ������ϸ�� 
 *      ActualPayCorpusAmt�ѻ�����
 *      ActualPayInteAmt�ѻ���Ϣ
 *      ActualFineAmt�Ѹ����ڷ�Ϣ
 *      ActualPayFeeAmt1�Ѹ�������
 *      �Ѹ���ǰ����ΥԼ��
 *      �Ѹ����������
 *      �Ѹ�׷����
 * ����ƻ���
 *      SeqId����
 *      PAYDATE�����ֹ�� project
 *      PayCorpusAmt+PayInteAmt+PayFineAmt+PayFeeAmt1Ӧ���ܶ�(Ԫ)
 *      PayCorpusAmtӦ������(Ԫ)
 *      PayInteAmtӦ����Ϣ(Ԫ)
 *      PayFineAmtӦ�����ڷ�Ϣ(Ԫ)
 *      PayFeeAmt1Ӧ��������(Ԫ)
 *      Status״̬
 * ���Ǳ��������
 * 		PageSize��ÿҳ������;
 *		CurPage����ǰҳ;
 */
public class LoanListHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
	
		return getLoanList(request);
	}
	
	/**
	 * �����Ϣ��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getLoanList(JSONObject request)throws HandlerException {
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		if(request.containsKey("PageSize"))
			this.pageSize = Integer.parseInt(request.get("PageSize").toString());
		if(request.containsKey("CurPage"))
			this.curPage = Integer.parseInt(request.get("CurPage").toString());
		String sUserID = request.get("UserID").toString();
		
		try{ 
			JBOFactory jbo = JBOFactory.getFactory();
			JSONObject result = new JSONObject();
            BizObjectManager m0 =jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery query0 = m0.createQuery(
					"select ps.objectno,pi.projectname,pi.serialno,li.putoutdate,li.maturitydate," +
					" li.businesssum,sum(ps.paycorpusamt+ps.payinteamt+ps.payfineamt+" +
					" ps.payfeeamt1) as v.sum1,li.loanstatus " +
					" from jbo.trade.user_contract uc,jbo.trade.ti_contract_info ci," +
					" jbo.trade.project_info pi,jbo.trade.acct_loan li," +
					" jbo.trade.acct_payment_schedule ps " +
					" where uc.userid=:userid and uc.relativetype='001' " +
					" and uc.contractid=pi.contractid " +
					" and uc.contractid=ci.contractid and ci.loanno=li.serialno " +
					" and ps.objectno=ci.loanno " +
					" group by ps.objectno,pi.projectname,pi.serialno," +
					" li.putoutdate,li.maturitydate,li.businesssum,li.loanstatus"
					);
			query0.setParameter("userid",sUserID);
			
			int firstRow = curPage * pageSize;
			if(firstRow < 0){
				firstRow = 0;
			}
			int maxRow = pageSize;
			if(maxRow <= 0){
				maxRow = 10;
			}
			query0.setFirstResult(firstRow);
			if(request.containsKey("PageSize")){
				query0.setMaxResults(maxRow);
			}
			
			int totalAcount = query0.getTotalCount();
			int temp = totalAcount % pageSize;
			int pageCount = totalAcount / pageSize;
			if(temp != 0){
				pageCount += 1;
			}
			
			List<BizObject> list0 = query0.getResultList(false);
			if(list0 != null){
				JSONArray array = new JSONArray();
				for (int i = 0; i < list0.size(); i++) {
					BizObject o = list0.get(i);
					JSONObject obj = new JSONObject();
					double sPaySum = Double.parseDouble(
							o.getAttribute("SUM1").toString()==null?
									"0":o.getAttribute("SUM1").toString());
					obj.put("ObjectNo", o.getAttribute("OBJECTNO").getValue()==null?
							"":o.getAttribute("OBJECTNO").getString());//��ݺ�
					obj.put("ProjectName", o.getAttribute("PROJECTNAME").getValue()==null?
							"":o.getAttribute("PROJECTNAME").getString());//��Ŀ����|�����
					obj.put("SerialNo", o.getAttribute("SERIALNO").getValue()==null?
							"":o.getAttribute("SERIALNO").getString());//��Ŀ����|�����
					obj.put("PutOutDate", o.getAttribute("PUTOUTDATE").getValue()==null?
							"":o.getAttribute("PUTOUTDATE").getString());//����ʱ��
					obj.put("MaturityDate", o.getAttribute("MATURITYDATE").getValue()==null?
							"":o.getAttribute("MATURITYDATE").getString());//����ʱ��
					obj.put("BusinessSum", Double.parseDouble(
							o.getAttribute("BUSINESSSUM").toString()==null?
									"0":o.getAttribute("BUSINESSSUM").toString()));//����(Ԫ) 
					obj.put("PaySum", GeneralTools.numberFormat(sPaySum, 0, 2));//�����ܶ�(Ԫ)
					obj.put("LoanStatus", o.getAttribute("LOANSTATUS").getValue()==null?
							"":o.getAttribute("LOANSTATUS").getString());//״̬
					array.add(obj);
				}
				result.put("RootType", "020");
				result.put("TotalAcount", String.valueOf(totalAcount));// �������´�����
				result.put("PageCount", String.valueOf(pageCount));
				result.put("array", array);
			}
			return result;
		} 
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryloanlist.error");
		}
	}
	 
}
