package com.amarsoft.p2ptrade.personcenter;

import java.util.List;
import java.util.Properties;


import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
/**
 * �տ���ϸ��ѯ
 * ���������
 * 		UserId:�û� ��
 * ��������� 
 *      ȫ�����ձ�ϢPAYCORPUSAMT + PAYINTEAMT ps    MATURITYDATE li
 *      δ��һ���� PAYCORPUSAMT + PAYINTEAMT * 30
 *      δ�������� PAYCORPUSAMT + PAYINTEAMT * 90
 *      δ��һ�� PAYCORPUSAMT + PAYINTEAMT * 360
 */
public class PaymentScheduleReceiveInfoHandler extends JSONHandler {

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
		
		String sUserID = request.get("UserID").toString();
		
		try{ 
			JBOFactory jbo = JBOFactory.getFactory();
			JSONObject result = new JSONObject();
			double sPaySum = 0;
			BizObjectManager m0 =jbo.getManager("jbo.trade.income_schedule");
			//ȫ�����ձ�Ϣ
			BizObjectQuery query0 = m0.createQuery(
				    "select sum(ps.paycorpusamt+ps.payinteamt) as v.PaySum  " +
				    " from jbo.trade.user_contract uc,jbo.trade.business_contract ci," +
				    " jbo.trade.project_info pi,jbo.trade.income_schedule ps," +
				    " jbo.trade.acct_loan li " +
				    " where uc.userid=:userid and uc.relativetype='002' " +
				    " and uc.contractid=ci.serialno " +
					" and uc.contractid=pi.contractid and ci.BASERIALNO=ps.LOANSERIALNO " +
					" and ci.BASERIALNO=li.serialno and li.loanstatus in ('0','1')" 
					);
			query0.setParameter("userid", sUserID);
			BizObject o0 = query0.getSingleResult(false);
			if(o0!=null){
				sPaySum =  Double.parseDouble(o0.getAttribute("PAYSUM").getValue()==null?
						  "0":o0.getAttribute("PAYSUM").toString());
			}else{
				sPaySum = 0;
			}
			
			double sPaySum1 = 0;
			double sPaySum3 = 0;
			double sPaySum12 = 0;
			BizObjectManager m =jbo.getManager("jbo.trade.income_schedule");
			//δ��һ���� δ��������  δ��һ��
			BizObjectQuery query = m.createQuery(
				    "select sum(ps.paycorpusamt+ps.payinteamt) as v.PaySum,ps.paydate " +
				    " from jbo.trade.user_contract uc,jbo.trade.business_contract ci," +
				    " jbo.trade.project_info pi,jbo.trade.income_schedule ps," +
				    " jbo.trade.acct_loan li " +
				    " where uc.userid=:userid and uc.relativetype='002' " +
				    " and uc.contractid=ci.serialno " +
					" and uc.contractid=pi.contractid and ci.BASERIALNO=ps.LOANSERIALNO " +
					" and ci.baserialno=li.serialno and li.loanstatus in ('0','1')"  +
				    " group by ps.paydate"
					);
			query.setParameter("userid", sUserID);
			List<BizObject> list = query.getResultList(false);
			if (list != null) {
				 for(int i = 0;i< list.size();i++){
					  BizObject o = list.get(i);
					  String sPayDate = o.getAttribute("PAYDATE").getValue()==null?
							  "":o.getAttribute("PAYDATE").getString();
					  //δ��һ���� 
					  if(GeneralTools.getdiffDateInt(sPayDate, GeneralTools.getDate())<30){
						  double sAmount = Double.parseDouble(o.getAttribute("PAYSUM").toString()==null?
								  "0":o.getAttribute("PAYSUM").toString());
						  sPaySum1 += sAmount;
					  }
					  //δ��������
					  if(GeneralTools.getdiffDateInt(sPayDate, GeneralTools.getDate())<90){
						  double sAmount = Double.parseDouble(o.getAttribute("PAYSUM").toString()==null?
								  "0":o.getAttribute("PAYSUM").toString());
						  sPaySum3 += sAmount;
					  }
					  //δ��һ��
					  if(GeneralTools.getdiffDateInt(sPayDate, GeneralTools.getDate())<360){
						  double sAmount = Double.parseDouble(o.getAttribute("PAYSUM").toString()==null?
								  "0":o.getAttribute("PAYSUM").toString());
						  sPaySum12 += sAmount;
					  }
				  }
					result.put("PaySum", String.format("%.2f",sPaySum));//ȫ�����ձ�Ϣ
					result.put("PaySum1", String.format("%.2f",sPaySum1));//δ��һ����
					result.put("PaySum3", String.format("%.2f",sPaySum3));//δ��������
					result.put("PaySum12", String.format("%.2f",sPaySum12));//δ��һ��
				    result.put("RootType", "010");
			}
			return result;
		} 
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("paymentschedulereceiveinfo.error");
		}
	}
}
