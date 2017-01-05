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
 * Ͷ�ʼ�¼��ѯ
 * ���������
 * 		UserID:�û� ��
 * ��������� 
 *      ContractID:��ͬ��
 * 		ProjectName:��Ŀ����
 * 		RateDate:��Ϣ��
 * 		MaturityDate:������
 *      LoanAmount:Ͷ�ʽ��  
 *  ���Ǳ��������
 * 		PageSize��ÿҳ������;
 *		CurPage����ǰҳ;
 */
public class InvestmentListHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
	
		return getInvestmentList(request);
	}
	
	/**
	 * Ͷ�ʼ�¼��ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getInvestmentList(JSONObject request)throws HandlerException {
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		
		String sUserID = request.get("UserID").toString();
		String sStatus = null;
		if(request.containsKey("Status"))
			sStatus = request.get("Status").toString();
		String spType = null;
		if(request.containsKey("pType"))
			spType = request.get("pType").toString();
		
		//��ȡpageSizeÿҳ��������curPage��ǰ����ҳ
		if(request.containsKey("pageSize"))
			this.pageSize = Integer.parseInt(request.get("pageSize").toString());
		if(request.containsKey("pageNo"))
			this.curPage = Integer.parseInt(request.get("pageNo").toString());
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			String sQuerySql = " uc.userid=:userid and uc.contractid=bc.serialno " +
					           " and uc.contractid=pi.contractid " +
					           " and uc.relativetype='002'" ;//2 ��������
			
/*			if(sStatus.equals("10")){
				sQuerySql+=" and (li.loanstatus in ('0','1') or li.loanstatus is null) ";
			}else if(sStatus.equals("20")){
				sQuerySql+=" and li.loanstatus not in ('0','1') and li.loanstatus is not null ";
			}*/
			if("00".equals(sStatus)){//������
				sQuerySql +=" and uc.status='0'";
			}else if("10".equals(sStatus)){//������
				sQuerySql +=" and uc.status='1'";
			}else if("20".equals(sStatus)){//�ѽ���
				sQuerySql +=" and uc.status='3'";
			}else if("30".equals(sStatus)){//��ʧ��
				sQuerySql +=" and uc.status='2'";
			}
			if(spType!=null){
				if(spType.equals("all")){
					
				}else{
					sQuerySql+=" and bc.businesstype='"+spType+"' ";
				}
			}
			
			
			BizObjectManager m =jbo.getManager("jbo.trade.user_contract");

			BizObjectQuery query = m.createQuery("" +
					"select pi.serialno,bc.serialno as v.baserialno,uc.contractid,pi.projectname,"
					+ "pi.begintime,pi.endtime,al.Putoutdate,al.MATURITYDATE,uc.investsum,"
					+ "uc.SUBCONTRACTNO,al.loanstatus,uc.status " +
					" from jbo.trade.user_contract uc,jbo.trade.project_info pi, " +
					" jbo.trade.business_contract bc left join jbo.trade.acct_loan al on bc.serialno=al.contractserialno " +
					" where " + sQuerySql+
					" order by uc.updatetime desc");
			query.setParameter("userid", sUserID);
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
			JSONArray array = new JSONArray();
			double dinvestsumcount=0.0;
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();
					obj.put("SerialNo", o.getAttribute("SerialNo").getValue()==null?
							"":o.getAttribute("SerialNo").getString());//��Ŀ���
//					obj.put("baserialno", o.getAttribute("baserialno").getValue()==null?
//							"":o.getAttribute("baserialno").getString());//��ݺ�
					obj.put("ContractID", o.getAttribute("contractid").getValue()==null?
							"":o.getAttribute("contractid").getString());//��ͬ��
					obj.put("ProjectName", o.getAttribute("PROJECTNAME").getValue()==null?
							"":o.getAttribute("PROJECTNAME").getString());//��Ŀ����
					obj.put("RateDate", o.getAttribute("PUTOUTDATE").getValue()==null?
							"":o.getAttribute("PUTOUTDATE").getString());//��Ϣ��
					obj.put("EndDate", o.getAttribute("MATURITYDATE").getValue()==null?
							"":o.getAttribute("MATURITYDATE").getString());//������
					obj.put("LoanAmount", o.getAttribute("investsum").getValue()==null?
							"0":o.getAttribute("investsum").getString());//Ͷ�ʽ��   ��
					obj.put("status", o.getAttribute("status").getValue()==null?
							"0":o.getAttribute("status").getString());//Ͷ�ʽ��   ��
					double investsum=o.getAttribute("investsum").getDouble();
					dinvestsumcount=dinvestsumcount+investsum;
					String sSubContractNo=o.getAttribute("SUBCONTRACTNO").getValue()==null?
							"":o.getAttribute("SUBCONTRACTNO").getString();//Ͷ�ʺ�ͬ��
					String sloanstatus=o.getAttribute("loanstatus").getValue()==null?
							"":o.getAttribute("loanstatus").getString();//���״̬
					if(sloanstatus.equals("0")||sloanstatus.equals("1")||sloanstatus.equals("")){
						obj.put("LoanStatus","10");
					}else{
						obj.put("LoanStatus","20");
					}
					String MonthIncome=getMonthIncome(jbo,sSubContractNo);
					
					obj.put("MonthIncome",MonthIncome);
					obj.put("SubContractNo",sSubContractNo);
					
					array.add(obj);
				}
			}
			
			result.put("investsumcount",dinvestsumcount);//���ܽ��
			result.put("TotalAcount", String.valueOf(totalAcount));
			result.put("curPage", String.valueOf(curPage));
			result.put("pagesize", String.valueOf(pageSize));
			result.put("array", array);
			return result;
		} 
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryinvestmentlist.error");
		}
	}
	
	/**
	 * ��ȡ���ձ�Ϣ
	 * 
	 * @param accountBo
	 * @throws HandlerException
	 */
	private String getMonthIncome(JBOFactory jbo, String sSubContractNo)
			throws HandlerException {
			BizObjectManager m;
			try {
				m = jbo.getManager("jbo.trade.income_detail");
				BizObjectQuery query = m.createQuery("select (o.PAYCORPUSAMT+o.PAYINTEAMT) as MonthIncome from o where  SubContractNo=:SubContractNo");
				query.setParameter("SubContractNo", sSubContractNo);
				BizObject o = query.getSingleResult(false);
				if (o != null) {
					return o.getAttribute("MonthIncome").getString();
				} else {
					return "";
				}
			} catch (Exception e) {
				return "";
			}
		}
	
}
