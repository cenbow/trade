package com.amarsoft.p2ptrade.personcenter;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;





import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ��ѯ��ͬģ�������Ϣ
 * ��������� UserID:�˻���� 
 *        SubContractNo:�˻���� 
 * ��������� 
 * 			
 */
public class GetContractPDFInfoHandler extends JSONHandler {

	static{
		Parser.registerFunction("getitemname");
	}
	public Object createResponse(JSONObject request, Properties arg1)throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		
		String sUserID = request.get("UserID").toString();
		
		if (request.get("SubContractNo") == null || "".equals(request.get("SubContractNo")))
			throw new HandlerException("common.nocontractno");
		
		String sSubContractNo = request.get("SubContractNo").toString();
		String ContractSavePath = ARE.getProperty("ContractSavePath");
		JSONObject result = new JSONObject();
		result.put("ContractSavePath", ContractSavePath);
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery query = m
					.createQuery("select ad1.realname as v.lendername,ua.username as v.lenderid,ad1.certid as v.lendercard, "+
						         "la.username as v.borrowname,la.customername as v.borrowid,la.Idcard as v.borrowcard,la.guarantorperson,la.guarantoridcard,la.mortgageperson,la.mortgageidcard,"+
						         "getitemname('LoanType',la.fundsource) as v.fundsource,uc.investsum,al.loanrate, "+
						         "al.loanterm,getitemname('PACBReturnMethod',al.RepaymentMethod) as v.RepaymentMethod, "+
						         "v.putoutdate,v.maturitydate,la.granantorname,al.putoutdate "+
								 "from jbo.trade.user_contract uc,jbo.trade.account_detail ad1, "+
								 "jbo.trade.account_detail ad2,jbo.trade.loan_apply la, "+
								 "jbo.trade.acct_loan al,jbo.trade.user_account ua "+
								 "where uc.userid=:userid "+
								 "and uc.SubContractNo=:SubContractNo "+
								 "and uc.contractid=al.contractserialno "+
								 "and uc.userid=ad1.userid "+
								 "and ua.userid=ad1.userid "+
								 "and al.customerid=ad2.userid "+
								 "and la.precontractno=al.contractserialno");
			query.setParameter("userid", sUserID);
			query.setParameter("SubContractNo", sSubContractNo);
			BizObject o = query.getSingleResult(false); 
			int sLoanTerm=0;
			if(o!=null){
				result.put("lendername", o.getAttribute("lendername").toString()==null?
						"":o.getAttribute("lendername").toString());//Ͷ��������
				result.put("lenderid", o.getAttribute("lenderid").toString()==null?
						"":o.getAttribute("lenderid").toString());//Ͷ�����˺�
				result.put("lendercard", o.getAttribute("lendercard").toString()==null?
						"":o.getAttribute("lendercard").toString());//Ͷ����֤��
				result.put("borrowname", o.getAttribute("borrowname").toString()==null?
						"":o.getAttribute("borrowname").toString());//���������
				result.put("borrowid", o.getAttribute("borrowid").toString()==null?
						"":o.getAttribute("borrowid").toString());//������˺�
				result.put("borrowcard", o.getAttribute("borrowcard").toString()==null?
						"":o.getAttribute("borrowcard").toString());//�����֤��
				result.put("guarantorperson", o.getAttribute("guarantorperson").toString()==null?
						"":o.getAttribute("guarantorperson").toString());//��֤������
				result.put("guarantoridcard", o.getAttribute("guarantoridcard").toString()==null?
						"":o.getAttribute("guarantoridcard").toString());//��֤�����֤��
				result.put("mortgageperson", o.getAttribute("mortgageperson").toString()==null?
						"":o.getAttribute("mortgageperson").toString());//����������
				result.put("mortgageidcard", o.getAttribute("mortgageidcard").toString()==null?
						"":o.getAttribute("mortgageidcard").toString());//���������֤��
				result.put("putoutdate", o.getAttribute("putoutdate").toString()==null?
						"":o.getAttribute("putoutdate").toString());//�ſ�����
				result.put("fundsource", o.getAttribute("fundsource").toString()==null?
						"":o.getAttribute("fundsource").toString());//�����;
				result.put("investsum", o.getAttribute("investsum").toString()==null?
						"":o.getAttribute("investsum").toString());//�����
				result.put("loanrate", o.getAttribute("loanrate").toString()==null?
						"":o.getAttribute("loanrate").toString());//���������
				result.put("loanterm", o.getAttribute("loanterm").toString()==null?
						"":o.getAttribute("loanterm").toString());//������ޡ���������
				result.put("RepaymentMethod", o.getAttribute("RepaymentMethod").toString()==null?
						"":o.getAttribute("RepaymentMethod").toString());//���ʽ
				result.put("putoutdate", o.getAttribute("putoutdate").toString()==null?
						"":o.getAttribute("putoutdate").toString());//��Ϣ��
				result.put("maturitydate", o.getAttribute("maturitydate").toString()==null?
						"":o.getAttribute("maturitydate").toString());//������
				result.put("granantorname", o.getAttribute("granantorname").toString()==null?
						"":o.getAttribute("granantorname").toString());//������
				
			}else{
				throw new HandlerException("getcontractpdfinfo.error");
			}
	           
			BizObjectManager m4 =jbo.getManager("jbo.trade.income_schedule");
			BizObjectQuery query4 = m4.createQuery(
							"select ps.seqid,ps.PayDate, " +
							" ps.PayCorpusAmt,ps.PayInteAmt,(ps.PayCorpusAmt+ps.PayInteAmt) as v.paysum " +
							" from jbo.trade.user_contract uc,jbo.trade.income_schedule ps " +
							" where uc.subcontractno=ps.subcontractno"+
							" and uc.userid=:userid "+
							" and uc.SubContractNo=:SubContractNo "+
							" order by ps.PayDate ");
			query4.setParameter("userid",sUserID);
			query4.setParameter("SubContractNo", sSubContractNo);
			int icount=0;
			List<BizObject> list2 = query4.getResultList(false);
			JSONArray array = new JSONArray();
			if (list2 != null) {
				for (int i = 0; i < list2.size(); i++) {
					icount++;
					BizObject o4 = list2.get(i);
					JSONObject obj = new JSONObject();
					String seqid = o4.getAttribute("seqid").getValue()==null?
							"":o4.getAttribute("seqid").getString();
					double sPayCorpusAmt = Double.parseDouble(o4.getAttribute("PayCorpusAmt").getValue()==null?
							"0":o4.getAttribute("PayCorpusAmt").toString());
					double sPayInteAmt = Double.parseDouble(o4.getAttribute("PayInteAmt").getValue()==null?
							"0":o4.getAttribute("PayInteAmt").toString());
					double sPayFineAmt = Double.parseDouble(o4.getAttribute("ACTUALFINEAMT").getValue()==null?
							"0":o4.getAttribute("ACTUALFINEAMT").toString());
					double sActualSum = sPayCorpusAmt + sPayInteAmt;
					String sPayDate = o4.getAttribute("PayDate").getValue()==null?
							"":o4.getAttribute("PayDate").getString();
					obj.put("SeqId", o4.getAttribute("SeqId").getValue()==null?
							0:o4.getAttribute("SeqId").getInt());//����
					obj.put("PayDate", sPayDate);//�����ֹ�� 
					obj.put("PayCorpusAmt", sPayCorpusAmt);//Ӧ������(Ԫ)
					obj.put("PayInteAmt", sPayInteAmt);//Ӧ����Ϣ(Ԫ)
					obj.put("ActualSum",GeneralTools.numberFormat(sActualSum, 0, 2));//Ӧ���ܶ�(Ԫ)
					array.add(obj);
				}
			}
			result.put("icount", icount);
			result.put("array", array);
			
			
			result.put("RootType", "030");
			return result;
		} catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getcontractpdfinfo.error");
		}
		
	}
	
}
