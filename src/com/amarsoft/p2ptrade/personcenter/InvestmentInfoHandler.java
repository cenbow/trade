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
 * Ͷ����ϸ��ѯ
 * ���������
 * 		ContractId��ͬ��  
 * ��������� 
 * ��Ŀ��Ϣ��
 *      SerialNo���
 * 		ProjectName��Ŀ����
 *      PUTOUTDATE MATURITYDATE�տ���
 *      LOANTERMFLAGʣ������
 *      LoanTerm|������
 *      LoanAmount��Ŀ����(Ԫ)
 *      CorpusBalanceʣ�౾��(Ԫ)
 *      Status״̬
 * �տ����飺
 *      ActualPayCorpusAmt+ActualPayInteAmt+ActualFineAmt�տ��ܶ�
 *      ActualPayCorpusAmt����
 *      ActualPayInteAmt��Ϣ
 *      ActualFineAmt���ڷ�Ϣ
 * ������ʷ��
 *      Created_Dateʱ��
 *      OPERATE����
 *      REMARK��ע
 * ���Ǳ��������
 * 		PageSize��ÿҳ������;
 *		CurPage����ǰҳ;
 */
public class InvestmentInfoHandler extends JSONHandler {
	 
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		 
		return getInvestmentInfo(request);
		 
	} 
	
	/**
	 * Ͷ����ϸ��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getInvestmentInfo(JSONObject request)throws HandlerException {
		if(request.get("SubContractNo")==null || "".equals(request.get("SubContractNo"))){
			throw new HandlerException("contractid.error");
		}
		
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
 
		String sSubContractNo = request.get("SubContractNo").toString();
		String sUserID = request.get("UserID").toString();
		
		try{
            JBOFactory jbo = JBOFactory.getFactory();
            //JSONObject sLoanStatusItems = GeneralTools.getItemName(jbo, "LoanStatus");
            JSONObject result = new JSONObject();
            //��Ŀ��Ϣ  
            BizObjectManager m = jbo.getManager("jbo.trade.project_info");
            BizObjectQuery query = m.createQuery(
            		"select pi.serialno,pi.projectname,li.PUTOUTDATE,li.MATURITYDATE,pi.contractid," +
            		" li.LoanTerm,uc.investsum,"+
            		" li.LOANSTATUS,li.STERM,li.finishdate " +
            		" from jbo.trade.project_info pi,jbo.trade.acct_loan li,jbo.trade.user_contract uc" +
            		" where uc.userid=:userid and uc.relativetype='002' " +
            		" and uc.SubContractNo=:SubContractNo " +
            		" and uc.contractid=pi.contractid and pi.contractid=li.contractserialno" 
            		);
			query.setParameter("SubContractNo", sSubContractNo);
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false); 
			int sLoanTerm = 0;
			int sTerm = 0;
			int smaxTerm = 0;
			String sSerialNo = "";
			String sPutOutDate = "";//��һ��������
			String sFinishdate = "";
			if(o!=null){
			    sLoanTerm =  o.getAttribute("LOANTERM").getValue()==null?
						    0:o.getAttribute("LOANTERM").getInt();
			    
			    
			    sTerm =  o.getAttribute("STERM").getValue()==null?
					    0:o.getAttribute("STERM").getInt();
			    
			    sSerialNo = o.getAttribute("contractid").toString()==null?
						"":o.getAttribute("contractid").toString();
			    
			    sFinishdate = o.getAttribute("Finishdate").toString()==null?
						"":o.getAttribute("Finishdate").toString();
			    
			    //��ʼ��Ϊ��һ��������
			    BizObjectManager mx = jbo.getManager("jbo.trade.acct_payment_schedule");
	            BizObjectQuery queryx = mx.createQuery(
	            		" select " +
	            		"  PAYDATE " +
	            		" from o where o.ObjectNo =:ObjectNo  and o.seqid = '1' ");
				queryx.setParameter("ObjectNo", sSerialNo);
				BizObject ox = queryx.getSingleResult(false); 
				if(ox!=null){
					sPutOutDate = ox.getAttribute("PAYDATE").toString()==null?
							"":ox.getAttribute("PAYDATE").toString();
				}
			    
				//ȡ�ѻ�����
			    BizObjectManager mx1 = jbo.getManager("jbo.trade.income_detail");
	            BizObjectQuery queryx1 = mx1.createQuery(
	            		" select max(seqid) as v.maxseqid," +
	            		"  sum(ACTUALPAYCORPUSAMT) as v.NORMALOVERDUE" +
	            		" from o where o.SUBCONTRACTNO =:SubContractNo ");
				queryx1.setParameter("SubContractNo", sSubContractNo);
				BizObject ox1 = queryx1.getSingleResult(false); 
				double amt =0 ;
				if(ox1!=null){
					amt = ox1.getAttribute("NORMALOVERDUE").getValue()==null?
							0:ox1.getAttribute("NORMALOVERDUE").getDouble();
					
					smaxTerm = ox1.getAttribute("maxseqid").getValue()==null?
							0:ox1.getAttribute("maxseqid").getInt();
				}
			    
				double investsum = o.getAttribute("investsum").getValue()==null?
					    0:o.getAttribute("investsum").getDouble();
				
				if("".equals(sPutOutDate)){
					sPutOutDate = o.getAttribute("PUTOUTDATE").toString()==null?
							"":o.getAttribute("PUTOUTDATE").toString();
				}
			    
				result.put("SerialNo", o.getAttribute("SerialNo").toString()==null?
						"":o.getAttribute("SerialNo").toString());//���
				result.put("ProjectName", o.getAttribute("ProjectName").toString()==null?
						"":o.getAttribute("ProjectName").toString());//��Ŀ���� 
				result.put("PutOutDate", sPutOutDate);//�տ���
				result.put("MaturityDate", o.getAttribute("MATURITYDATE").toString()==null?
						"":o.getAttribute("MATURITYDATE").toString());//�տ���
				result.put("LoanTerm",  sLoanTerm);//������
				result.put("BusinessSum", o.getAttribute("investsum").getValue()==null?
						"0":o.getAttribute("investsum").getDouble());//��Ŀ����(Ԫ) ��
				result.put("NormalOverDue", investsum-amt);//ʣ�౾��(Ԫ) ��
				
				if(!"".equals(sFinishdate)&& null != sFinishdate ){
					result.put("ProductStatus","20");
				}else{
					result.put("ProductStatus","10");
				}
			}else{
				throw new HandlerException("queryinvestmentinfo.error");
			}
			result.put("RootType", "030");
			
			//�տ����� 
			BizObjectManager m1 = jbo.getManager("jbo.trade.income_detail");
            BizObjectQuery query1 = m1.createQuery(
            		"select sum(bb.ACTUALPAYCORPUSAMT) as v.ACTUALPAYCORPUSAMT," +
            		"sum(bb.ACTUALPAYINTEAMT) as v.ACTUALPAYINTEAMT," +
            		"sum(bb.ACTUALEXPIATIONSUM) as v.ACTUALEXPIATIONSUM," +
            		"sum(bb.ACTUALPAYCORPUSAMT+bb.ACTUALPAYINTEAMT+bb.ACTUALEXPIATIONSUM) as v.PaySum " +
            		" from jbo.trade.business_contract ci,jbo.trade.user_contract uc  "+
            		" left join jbo.trade.income_detail bb on uc.contractid=bb.ContractNo " +
            		" where uc.userid=:userid and uc.relativetype='002' " +
            		" and uc.SUBCONTRACTNO = bb.SUBCONTRACTNO" +
            		" and uc.SubContractNo=:SubContractNo " +
            		" and uc.contractid=ci.serialno  " );
            query1.setParameter("userid", sUserID);
            query1.setParameter("SubContractNo", sSubContractNo);
			BizObject o1 = query1.getSingleResult(false); 
			if(o1!=null){
				double corpusamt = o1.getAttribute("ACTUALPAYCORPUSAMT").getValue()==null?
 						0:o1.getAttribute("ACTUALPAYCORPUSAMT").getDouble();
				
				double inteamt = o1.getAttribute("ACTUALPAYINTEAMT").getValue()==null?
 						0:o1.getAttribute("ACTUALPAYINTEAMT").getDouble();
				
				double ywjamt = o1.getAttribute("ACTUALEXPIATIONSUM").getValue()==null?
						0:o1.getAttribute("ACTUALEXPIATIONSUM").getDouble();
				
				result.put("PaySum", GeneralTools.numberFormat(corpusamt+inteamt, 0, 2));//�տ��ܶ�
 				result.put("ActualPayCorpusAmt", GeneralTools.numberFormat(corpusamt, 0, 2));//����
 				result.put("ActualPayInteAmt", GeneralTools.numberFormat(inteamt, 0, 2));//��Ϣ
 				result.put("ActualExpiationSum", GeneralTools.numberFormat(ywjamt, 0, 2));//ΥԼ��
			}
			
					int plusseqid = 0;
			        if("".equals(sFinishdate)||null==sFinishdate){
			        	plusseqid = (sLoanTerm - sTerm) > 0 ? (sLoanTerm - sTerm) : 0;
			        }
			
					result.put("PlusSeqId", plusseqid);//ʣ������
		   
				if("".equals(sFinishdate)||null==sFinishdate){
					smaxTerm = sLoanTerm;
				}	
				
				BizObjectManager m4 =jbo.getManager("jbo.trade.income_detail");
				BizObjectQuery query4 = m4.createQuery(
						"select ps.LOANSERIALNO,ps.seqid,ps.PAYDATE,ps.PAYCORPUSAMT,ps.PAYINTEAMT " +
						" from  jbo.trade.user_contract uc,jbo.trade.business_contract ci," +
						" jbo.trade.income_schedule ps " +
						" where uc.userid=:userid and uc.relativetype='002' " +
						" and uc.SubContractNo=:SubContractNo " +
						" and uc.contractid=ci.serialno and uc.contractid=ps.ContractNo" +
						" and uc.SUBCONTRACTNO = ps.SUBCONTRACTNO and ps.seqid <=:Seqid" +
						" order by ps.seqid");
				query4.setParameter("userid",sUserID);
				query4.setParameter("SubContractNo", sSubContractNo);
				query4.setParameter("Seqid", smaxTerm);
				 
				List<BizObject> list2 = query4.getResultList(false);
				JSONArray array1 = new JSONArray();
				if (list2 != null) {
					for (int i = 0; i < list2.size(); i++) {
						BizObject o4 = list2.get(i);
						JSONObject obj = new JSONObject();
						double sPayCorpusAmt = o4.getAttribute("PAYCORPUSAMT").getValue()==null?
		 						0:o4.getAttribute("PAYCORPUSAMT").getDouble();
						double sPayInteAmt = o4.getAttribute("PAYINTEAMT").getValue()==null?
		 						0:o4.getAttribute("PAYINTEAMT").getDouble();
						
						double sPaySum = sPayCorpusAmt + sPayInteAmt ;//δ���ܶ�
						
						String ObjectNo = o4.getAttribute("LOANSERIALNO").getValue()==null?
								"":o4.getAttribute("LOANSERIALNO").getString();
						
						int iSeq = o4.getAttribute("SeqId").getValue()==null?
								0:o4.getAttribute("SeqId").getInt();
						
						String sPayDate = o4.getAttribute("PAYDATE").getValue()==null?
								"":o4.getAttribute("PAYDATE").getString();
						obj.put("ObjectNo", ObjectNo);//��ݺ�
						obj.put("SeqId", iSeq );//����
						
						
						//��ѯ������ϸ
						BizObjectManager m5 = jbo.getManager("jbo.trade.income_detail");
			            BizObjectQuery query5 = m5.createQuery(
			            		" select " +
			            		" sum(o.ACTUALPAYCORPUSAMT) as v.ACTUALPAYCORPUSAMT," +
			            		" sum(o.ACTUALPAYINTEAMT) as v.ACTUALPAYINTEAMT " +
			            		" from o where o.SubContractNo =:SubContractNo and o.seqid =:SeqId " );
			            query5.setParameter("SubContractNo", sSubContractNo);
			            query5.setParameter("SeqId", iSeq);
						BizObject o5 = query5.getSingleResult(false); 
						double dActualCorpusAmt = 0;
						double dActualInteAmt = 0;
						if(o5!=null){
							 dActualCorpusAmt = o5.getAttribute("ACTUALPAYCORPUSAMT").getValue()==null?
			 						0:o5.getAttribute("ACTUALPAYCORPUSAMT").getDouble();//�ѻ�����
							 dActualInteAmt = o5.getAttribute("ACTUALPAYINTEAMT").getValue()==null?
			 						0:o5.getAttribute("ACTUALPAYINTEAMT").getDouble();//�ѻ���Ϣ
							
						}
						//�⸶״̬
						BizObjectManager m6 = jbo.getManager("jbo.trade.acct_payment_schedule");
			            BizObjectQuery query6 = m6.createQuery(
			            		" select FinishDate from o where o.ObjectNo =:ObjectNo  and o.SeqId =:SeqId " );
			            query6.setParameter("ObjectNo", ObjectNo);
			            query6.setParameter("SeqId", iSeq);
						BizObject o6 = query6.getSingleResult(false); 
						String sFinishDate = "xx";
						if(o6!=null){
							sFinishDate =  o6.getAttribute("FinishDate").getValue()==null?
									"xx":o6.getAttribute("FinishDate").getString();
						}
						
						sPaySum = sPaySum - dActualCorpusAmt - dActualInteAmt;//δ�ս��
						
						String sStatus = "δ��";
						
						if(sPaySum < 0 ){
							sPaySum = 0d;
						}
						
						if(!"xx".equals(sFinishDate)&&sFinishDate.length()==10){//�ѽ���
							sPaySum = 0d;
							sStatus = "����";
						}
						
						
						obj.put("PayDate", sPayDate);//�����ֹ�� 
						obj.put("ActualCorpusAmt",GeneralTools.numberFormat(dActualCorpusAmt, 0, 2));//���ձ���(Ԫ)
						obj.put("ActualInteAmt", GeneralTools.numberFormat(dActualInteAmt, 0, 2));//������Ϣ(Ԫ)
						obj.put("ActualSum", GeneralTools.numberFormat(dActualCorpusAmt + dActualInteAmt, 0, 2));//ʵ���ܶ�(Ԫ)
						obj.put("UnReSum", GeneralTools.numberFormat(sPaySum, 0, 2));//δ�ս��(Ԫ)
						obj.put("StatusName", sStatus);//״̬ ����
						array1.add(obj);
					}
				}
		 
//			result.put("array", array);
			result.put("array1", array1);
			return result;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryinvestmentinfo.error");
		}
	}
	
 
	 /**
	  * ״ֵ̬ת��code_library����״̬
	  * @param jbo
	  * @param sSerialNo
	  * @param bankItems
	  * @return
	  * @throws HandlerException
	  */
	private JSONObject getLoanStatusBelong(String sLoanStatusItemNo, JSONObject sLoanStatusItems)
			throws HandlerException {
		try {
				String sLoanStatusName = sLoanStatusItems.containsKey(sLoanStatusItemNo) ? sLoanStatusItems
						.get(sLoanStatusItemNo).toString() : sLoanStatusItemNo;
				JSONObject obj = new JSONObject();
				obj.put("LoanStatusName", sLoanStatusName);
				return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("queryloanstatusinfo.error");
		}
	}
	
	 /**
	  * ״ֵ̬ת��code_libraryͶ��״̬
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