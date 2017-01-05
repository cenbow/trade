package com.amarsoft.p2ptrade.personcenter;

import java.util.List;
import java.util.Properties;


import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
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
 * ����б�
 *      PROJECTNAME|SERIALNO��Ŀ����|�����project
 *      ����ʱ��PUTOUTDATE loan
 *      ����ʱ��MATURITYDATE loan
 *      BUSINESSSUM����(Ԫ) loan 
 *      �����ܶ�(Ԫ)PAYCORPUSAMT+PAYINTEAMT+PAYFINEAMT+PAYFEEAMT1+PAYFEEAMT2 payment
 *      
 * ��Ŀ������Ϣ��
 *      CONTRACTID��ͬ��
 *      PROJECTNAME|SERIALNO��Ŀ����|�����  project
 *      ������PUTOUTDATE-MATURITYDATE loan 
 *      ������LOANTERM loan
 *      BUSINESSSUM����(Ԫ) loan
 *      �������� NORMALBALANCE
 *      ���ڱ���+������Ϣ+��Ϣ+����
 *      OVERDUEBALANCE+INTERESTBALANCE+FINEINTEBALANCE+COMPINTEBALANCE
 *      ʵ�ʻ����ܶ�(Ԫ) ʵ ACTUALPAYCORPUSAMT+ACTUALPAYINTEAMT+ACTUALFINEAMT payment
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
 *      
 *      FINISHDATEʵ�ʻ�����
 *      ACTUALPAYCORPUSAMT+ACTUALPAYINTEAMT+ACTUALFINEAMTʵ���ܶ�(Ԫ)
 *      ACTUALPAYCORPUSAMT����(Ԫ)
 *      ACTUALPAYINTEAMT��Ϣ(Ԫ)
 *      ACTUALFINEAMT���ڷ�Ϣ(Ԫ)
 *      ACTUALPAYFEEAMT1������(Ԫ)
 * 
 */
public class LoanInfo1Handler extends JSONHandler {

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
		if(request.get("SerialNo")==null || "".equals(request.get("SerialNo"))){
			throw new HandlerException("SerialNo.error");
		}
		
		if(request.get("SerialNo")==null || "".equals(request.get("SerialNo"))){
			throw new HandlerException("SerialNo.error");
		}
		 
		String sSerialNo = request.get("SerialNo").toString();
		String sPaymentFlag = paymentFlag(sSerialNo);
		String[] sPaymentFlagSplit = sPaymentFlag.split("@");
		try{ 
			JBOFactory jbo = JBOFactory.getFactory();
			JSONObject result = new JSONObject();
            BizObjectManager m =jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery query = m.createQuery(
					 "select uc.contractid,ps.objectno,pi.projectname,pi.serialno,li.putoutdate,li.maturitydate," +
					 " li.loanterm,li.businesssum,sum(ps.actualpaycorpusamt+ps.actualpayinteamt+ps.actualfineamt+ps.actualpayfeeamt1)  as v.sum1,li.loanstatus " +
					 " from jbo.trade.project_info pi,jbo.trade.acct_payment_schedule ps," +
					 " jbo.trade.acct_loan li,jbo.trade.user_contract uc,jbo.trade.ti_contract_info ci " +
					 " where pi.serialno=:serialno and uc.contractid=pi.contractid " +
					 " and uc.contractid=ci.contractid and ci.loanno=li.serialno " +
					 " and ps.objectno=ci.loanno " +
					 " group by uc.contractid,ps.objectno,pi.projectname,pi.serialno,li.putoutdate,li.putoutdate," +
					 " li.maturitydate,li.loanterm,li.businesssum,li.loanstatus"
					);
			query.setParameter("serialno",sSerialNo);
			
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				String sProjectName = o.getAttribute("PROJECTNAME").toString()==null?
						"":o.getAttribute("PROJECTNAME").toString();
				String sSerialNo1 = o.getAttribute("SERIALNO").toString()==null?
						"":o.getAttribute("SERIALNO").toString();
				String sPutoutDate = o.getAttribute("PUTOUTDATE").toString()==null?
						"":o.getAttribute("PUTOUTDATE").toString();
				String sMaturityDate = o.getAttribute("MATURITYDATE").toString()==null?
						"":o.getAttribute("MATURITYDATE").toString();
//     			double sOverdueBalance = Double.parseDouble(o.getAttribute("OVERDUEBALANCE").toString()==null?
//     					"0":o.getAttribute("OVERDUEBALANCE").toString());//��������
//				double sInterestBalance = Double.parseDouble(o.getAttribute("INTERESTBALANCE").toString()==null?
//						"0":o.getAttribute("INTERESTBALANCE").toString());//���ڱ���
//				double sFineInteBalance= Double.parseDouble(o.getAttribute("FINEINTEBALANCE").toString()==null?
//						"0":o.getAttribute("FINEINTEBALANCE").toString());//��Ϣ
//				double sCompInteBalance = Double.parseDouble(o.getAttribute("COMPINTEBALANCE").toString()==null?
//						"0":o.getAttribute("COMPINTEBALANCE").toString());//����
//				double sOverBalance = sOverdueBalance + sInterestBalance + sFineInteBalance + sCompInteBalance;//�����ܶ�
//				double sActualPayCorpusAmt = Double.parseDouble(o.getAttribute("ACTUALPAYCORPUSAMT").toString()==null?
//						"":o.getAttribute("ACTUALPAYCORPUSAMT").toString());//�ѻ�����
//				double sActualPayInteAmt = Double.parseDouble(o.getAttribute("ACTUALPAYINTEAMT").toString()==null?
//						"":o.getAttribute("ACTUALPAYINTEAMT").toString());//�ѻ���Ϣ
//				double sActualFineAmt = Double.parseDouble(o.getAttribute("ACTUALFINEAMT").toString()==null?
//						"":o.getAttribute("ACTUALFINEAMT").toString());//�Ѹ����ڷ�Ϣ
				String sProjectSerialNo = sProjectName + "|" + sSerialNo1;
				String sPutMatDate = sPutoutDate + "-" + sMaturityDate;
//				double sPaySum = sActualPayCorpusAmt + sActualPayInteAmt + sActualFineAmt; 
				String sLoanStatusCode = o.getAttribute("LOANSTATUS").toString()==null?
						"":o.getAttribute("LOANSTATUS").toString();
				JSONObject sLoanStatusName = GeneralTools.getItemName(jbo, "LoanStatus");
				
				result.put("RootType", "030");
				result.put("ContractId", o.getAttribute("CONTRACTID").toString()==null?
						"":o.getAttribute("CONTRACTID").toString());//��ͬ��
				result.put("PayFlag", sPaymentFlagSplit[0]);//
				result.put("OverFlag", sPaymentFlagSplit[1]);//
				result.put("ProjectSerialNo", sProjectSerialNo);//��Ŀ����|�����
				result.put("PayDate", sPutMatDate);//������
				result.put("LoanTerm", o.getAttribute("LOANTERM").toString()==null?
						"":o.getAttribute("LOANTERM").toString());//������
				result.put("BusinessSum", o.getAttribute("BUSINESSSUM").toString()==null?
						"":o.getAttribute("BUSINESSSUM").toString());//����(Ԫ)
				result.put("PaySum", o.getAttribute("SUM1").toString()==null?
						"":o.getAttribute("SUM1").toString());//ʵ�ʻ����ܶ�(Ԫ)
				result.put("LoanStatusCode", sLoanStatusCode);//״̬ 
				result.put("LoanStatusName", sLoanStatusName);//״̬����
				
				
				BizObjectManager m1 =jbo.getManager("jbo.trade.acct_payment_schedule");
				BizObjectQuery query1 = m1.createQuery(
						"select ps.objectno,ps.seqid,ps.paydate,ps.paycorpusamt," +
						"ps.payinteamt,ps.payfineamt,ps.payfeeamt1,ps.status " +
						"from jbo.trade.project_info pi,jbo.trade.acct_payment_schedule ps," +
						"jbo.trade.ti_contract_info ci " +
						"where pi.serialno=:serialno and pi.contractid=ci.contractid " +
						"and ci.loanno=ps.objectno ");
				query1.setParameter("serialno",sSerialNo);
				
				List<BizObject> list = query1.getResultList(false);
				if (list != null) {
					JSONArray array = new JSONArray();
					for (int i = 0; i < list.size(); i++) {
						BizObject o1 = list.get(i);
						JSONObject obj = new JSONObject();
						String sStatusCode = o1.getAttribute("STATUS").toString()==null?
								"":o1.getAttribute("STATUS").toString();
						JSONObject sStatusName = GeneralTools.getItemName(jbo, "PaymentStatus");
						double sPayCorpusAmt = Double.parseDouble(o1.getAttribute("PAYCORPUSAMT").toString()==null?
								"":o1.getAttribute("PAYCORPUSAMT").toString());
						double sPayInteAmt = Double.parseDouble(o1.getAttribute("PAYINTEAMT").toString()==null?
								"":o1.getAttribute("PAYINTEAMT").toString());
						double sPayFineAmt = Double.parseDouble(o1.getAttribute("PAYFINEAMT").toString()==null?
								"":o1.getAttribute("PAYFINEAMT").toString());
						double sPayFeeAmt1 = Double.parseDouble(o1.getAttribute("PAYFEEAMT1").toString()==null?
								"":o1.getAttribute("PAYFEEAMT1").toString());
						double sActualSum = sPayCorpusAmt + sPayInteAmt + sPayFineAmt + sPayFeeAmt1;
						String sPayDate = o1.getAttribute("PAYDATE").toString()==null?
								"":o1.getAttribute("PAYDATE").toString();
						String sCur = GeneralTools.getdiffDate(sPayDate, GeneralTools.getDate());
						obj.put("ObjectNo", o1.getAttribute("OBJECTNO").toString()==null?
								"":o1.getAttribute("OBJECTNO").toString());//��ݺ�
						obj.put("SeqId", o1.getAttribute("SeqId").toString()==null?
								"":o1.getAttribute("SeqId").toString());//����
						
						obj.put("PayDate", sPayDate);//�����ֹ�� 
						obj.put("Cur", sCur);// 
						obj.put("ActualSum",GeneralTools.numberFormat(sActualSum, 0, 2));//Ӧ���ܶ�(Ԫ)
						obj.put("PayCorpusAmt", o1.getAttribute("PAYCORPUSAMT").toString()==null?
								"":o1.getAttribute("PAYCORPUSAMT").toString());//Ӧ������(Ԫ)
						obj.put("PayInteAmt", o1.getAttribute("PAYINTEAMT").toString()==null?
								"":o1.getAttribute("PAYINTEAMT").toString());//Ӧ����Ϣ(Ԫ)
						obj.put("PayFineAmt", o1.getAttribute("PAYFINEAMT").toString()==null?
								"":o1.getAttribute("PAYFINEAMT").toString());//Ӧ�����ڷ�Ϣ(Ԫ)
						obj.put("PayFeeAmt1", o1.getAttribute("PAYFEEAMT1").toString()==null?
								"":o1.getAttribute("PAYFEEAMT1").toString());//Ӧ��������(Ԫ)
						obj.put("StatusCode", sStatusCode);//״̬ 
						obj.put("StatusName", sStatusName);//״̬ ����
						array.add(obj);
			}
					result.put("array", array);
				}
			}
			
            BizObjectManager m1 =jbo.getManager("jbo.trade.ti_contract_info");
            
			BizObjectQuery query1 = m1.createQuery(
					 "select ps.actualpaycorpusamt,ps.actualpayinteamt," +
					 "ps.actualfineamt,ps.actualpayfeeamt1 " +
					 "from jbo.trade.ti_contract_info ci," +
					 "jbo.trade.acct_payment_schedule ps,jbo.trade.project_info pi " +
					 "where pi.serialno=:serialno and pi.contractid=ci.contractid " +
					 "and ci.loanno=ps.objectno"
					);
			  query1.setParameter("serialno",sSerialNo);
			  BizObject o1 = query1.getSingleResult(false);
			  
				result.put("ActualPayCorpusAmt", o1.getAttribute("ACTUALPAYCORPUSAMT").toString()==null?
				        "":o1.getAttribute("ACTUALPAYCORPUSAMT").toString());//�ѻ�����
				result.put("ActualPayInteAmt", o1.getAttribute("ACTUALPAYINTEAMT").toString()==null?
						"":o1.getAttribute("ACTUALPAYINTEAMT").toString());//�ѻ���Ϣ
				result.put("ActualFineAmt", o1.getAttribute("ACTUALFINEAMT").toString()==null?
						"":o1.getAttribute("ACTUALFINEAMT").toString());//�Ѹ����ڷ�Ϣ
				result.put("ActualPayFeeAmt1", o1.getAttribute("ACTUALPAYFEEAMT1").toString()==null?
						"":o1.getAttribute("ACTUALPAYFEEAMT1").toString());//�Ѹ�������
//			  result.put("NormalBalance", o1.getAttribute("NORMALBALANCE").toString()==null?
//						"":o1.getAttribute("NORMALBALANCE").toString());//��������
//				result.put("OverBalance", sOverBalance);//�����ܶ�
			  
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryloanlist.error");
		}
	}
	
	 
	/**
	 * @return
	 * @throws HandlerException 
	 */
	private String  paymentFlag(String  serialnoString) throws HandlerException {
		 String payflag = "";  // paytype 1 һ�㻹��  3 ��ǰ����  1
		 String overflag = "";//���ڱ�־  0 ���� 1 ����  0 
		 String loansserialnoString = "";
		 String loanstatusString = "";
		 String paydateString = "";
		 String subpaydate = GeneralTools.getDate().substring(0, 7);
		 //����  ��Ϣ��  / ����
		 JBOFactory jbo=JBOFactory.getFactory();
		 BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.acct_loan");
			BizObjectQuery query =manager.createQuery(
					"select  SERIALNO, LOANSTATUS " +
					" from o,jbo.trade.project_info pi,jbo.trade.ti_contract_info ci " +
					" where pi.contractid =ci.contractno and ci.loanno = serialno " +
					" and pi.serialno = :serialno" );
			query.setParameter("serialno",serialnoString);
			BizObject bizObject = query.getSingleResult(false);
			loansserialnoString = bizObject.getAttribute("SERIALNO").toString()==null?
					"":bizObject.getAttribute("SERIALNO").toString();
			loanstatusString = bizObject.getAttribute("LOANSTATUS").toString()==null?
					"":bizObject.getAttribute("LOANSTATUS").toString();
			
			manager = jbo.getManager("jbo.trade.acct_payment_schedule");
			BizObjectQuery queryObjectQuery =manager.createQuery(
					"select PAYDATE " +
					" from o ,jbo.trade.acct_loan li  " +
					" where  objectno = li.serialno and  paydate " +
					" like :subpaydate and  li.serialno = :serialno" );
			queryObjectQuery.setParameter("serialno",loansserialnoString);
			queryObjectQuery.setParameter("subpaydate",subpaydate+"%");
 			BizObject bizObject1 = queryObjectQuery.getSingleResult(false);	
 			if(bizObject1!=null){
 			paydateString = bizObject1.getAttribute("PAYDATE").toString()==null?
 					"":bizObject1.getAttribute("PAYDATE").toString();
 			}
			//����
			if ("1".equals(loanstatusString)) {
				payflag = "1"; 
				overflag = "1";
			//����
			}else if("0".equals(loanstatusString)){
				   if (paydateString.equals(GeneralTools.getDate())) {
					   payflag = "1"; 
				    }else {
				    	payflag = "3"; 
					}
				   overflag = "0";
			}else {
				throw new HandlerException("request.invalid");
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return payflag+"@"+overflag;
	}
}
