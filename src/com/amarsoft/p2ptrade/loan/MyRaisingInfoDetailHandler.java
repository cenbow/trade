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
 * ���Ͷ���е���Ϣ
 * @author Mbmo
 *
 */
public class MyRaisingInfoDetailHandler extends JSONHandler {
	private final String INVEST = "002";// 1��������ˣ�2����Ͷ����
	static {
		Parser.registerFunction("getUserName");
		Parser.registerFunction("getitemname");
	}

	@Override
	public Object createResponse(JSONObject request, Properties arg1) throws HandlerException {
		if(isCurrentUser(request)){
			return getMyRaisingDetial(request);
		}
		//�������뵱ǰ�û��޹أ��򷵻ؿ�
		return new JSONObject();
	}

	@SuppressWarnings("unchecked")
	private JSONObject getMyRaisingDetial(JSONObject request) {
		JSONObject result = new JSONObject();
		String applyNo = (String) request.get("serialNo");
		
		JSONObject scheduleJ = new JSONObject();// ����
		JSONObject loanInfoJ = new JSONObject();// ������Ϣ
		JSONArray investBriefInfoArray = new JSONArray();// Ͷ���˼��
		
		BizObject loanInfoB = getLoanInfoResult(applyNo);
		String contractId ="";//��ͬ���
		double loanAmount=0;
		double investSum=0;
		
		if(loanInfoB!=null){
		try {
			loanInfoJ.put("loanAmount", null==loanInfoB.getAttribute("LOANAMOUNT")? "" : loanInfoB.getAttribute("LOANAMOUNT").toString());// ������
			loanInfoJ.put("loanRate", loanInfoB.getAttribute("LOANRATE") == null ? "" : loanInfoB.getAttribute("LOANRATE").toString());// ����������
			loanInfoJ.put("loanTerm", loanInfoB.getAttribute("LOANTERM") == null ? 0 : loanInfoB.getAttribute("LOANTERM").getInt());// ��������
			loanInfoJ.put("guarantee", loanInfoB.getAttribute("GRANANTEE") == null ? "" : loanInfoB.getAttribute("GRANANTEE").toString());// ������ʽ
			loanInfoJ.put("prov", loanInfoB.getAttribute("PROV") == null ? "" : loanInfoB.getAttribute("PROV").toString());// ʡ
			loanInfoJ.put("city", loanInfoB.getAttribute("CITY") == null ? "" : loanInfoB.getAttribute("CITY").toString());// ��
			loanInfoJ.put("paymentMethod", loanInfoB.getAttribute("PAYMENTMETHOD") == null ? "" : loanInfoB.getAttribute("PAYMENTMETHOD").toString());// ���ʽ
			loanInfoJ.put("fundSourceDesc", loanInfoB.getAttribute("fundsourcedesc") == null ? "" : loanInfoB.getAttribute("fundsourcedesc").toString());// ��������˵��
			
			loanAmount=loanInfoB.getAttribute("LOANAMOUNT")==null?-1:loanInfoB.getAttribute("LOANAMOUNT").getDouble();//Ϊ-1ʱȡֵ����
			contractId = loanInfoB.getAttribute("contractId") == null ?"" : loanInfoB.getAttribute("contractId").toString();
			
			List<BizObject> investBriefInfo = getInvestInfoResultList(contractId);
			for (BizObject bizO : investBriefInfo) {
				JSONObject investBriefInfoJ=new JSONObject();
				investBriefInfoJ.put("userName", handleName(bizO.getAttribute("USERNAME") == null ? "" : bizO.getAttribute("USERNAME").toString()));// �û���
				investBriefInfoJ.put("investSum", bizO.getAttribute("investsum") == null ? "" : bizO.getAttribute("investsum").toString());// Ͷ�ʽ��
				investBriefInfoJ.put("inputTime", bizO.getAttribute("inputtime") == null ? "" : bizO.getAttribute("inputtime").toString());// Ͷ��ʱ��
				investBriefInfoJ.put("status", bizO.getAttribute("status") == null ? "" : bizO.getAttribute("status").toString());// ״̬
				
				double invest=bizO.getAttribute("investsum")==null?-0.5:bizO.getAttribute("investsum").getDouble();
				investSum+=invest;
				investBriefInfoArray.add(investBriefInfoJ);
			}
			
			double ratio=-1;
			if(loanAmount!=0){
				ratio=investSum*100/loanAmount;
			}
			scheduleJ.put("loanAmount",loanAmount);// �ܶ�
			scheduleJ.put("investSum",investSum);// �ѳ��
			scheduleJ.put("difference",loanAmount-investSum);// ���
			scheduleJ.put("ratio",ratio);// ����
			
		} catch (JBOException e) {
			e.printStackTrace();
		}
		}
		result.put("scheduleJ", scheduleJ);
		result.put("loanInfoJ", loanInfoJ);
		result.put("investBriefInfoArray", investBriefInfoArray);
		return result;
	}

	/**
	 * ��ȡproject_info��loan_apply�ļ�����ѯ���
	 */
	private BizObject getLoanInfoResult(String applyNo) {
		BizObject r = null;
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		try {
			m = f.getManager("jbo.trade.project_info");
			String sql = "select" + 
						" o.contractid,o.LOANAMOUNT,o.LOANRATE,o.LOANTERM,o.GRANANTEE,o.PAYMENTMETHOD, getitemname('AreaCode', la.prov) AS V.PROV,getitemname('DistrictCode', la.city) AS V.CITY,la.fundsourcedesc "+ 
						"from jbo.trade.ti_contract_info ci,o,jbo.trade.loan_apply la "+ 
						"where la.loanno=ci.loanno and o.contractid=ci.contractid and o.SERIALNO=:applyNo";
			BizObjectQuery q = m.createQuery(sql);
			q.setParameter("applyNo", applyNo);
			r = q.getSingleResult(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return r;
	}

	/**
	 * ��ȡͶ���˵Ĳ�ѯ���
	 */
	@SuppressWarnings("unchecked")
	private List<BizObject> getInvestInfoResultList(String contractId) {
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		List<BizObject> r = null;
		try {
			m = f.getManager("jbo.trade.user_contract");
			String sql = "select " + 
									"getUserName(userid) AS V.USERNAME,inputtime,investsum,status " + 
									"from o "+ 
									"where contractId=:contractId And relativetype=:INVEST";
			BizObjectQuery q = m.createQuery(sql);
			q.setParameter("contractId", contractId);
			q.setParameter("INVEST", INVEST);
			r = q.getResultList(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return r;
	}
	/**
	 * �жϲ�ѯ����Ƿ��뵱ǰ�û�����
	 * @param request
	 * @return
	 */
	private boolean isCurrentUser(JSONObject request){
		boolean flag=false;
		String userId=(null==request.get("userId")?"":request.get("userId").toString());
		String serialNo=(null==request.get("serialNo")?"":request.get("serialNo").toString());
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		
		try {
			String table="jbo.trade.project_info";
			String sql = "select bc.CUSTOMERID from o,jbo.trade.business_contract bc where o.CONTRACTID=bc.SERIALNO and o.serialno=:serialNo";
			m = f.getManager(table);
			BizObjectQuery q = m.createQuery(sql);
			BizObject userInBaseB = q.setParameter("serialNo", serialNo).getSingleResult(false);
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
	/**
	 * ���û����м���*�Ŵ���
	 */
	private  String handleName(String userName){
		String result="";
		if(userName.length()>2){
			char endS=userName.charAt(userName.length()-1);
			result=userName.charAt(0)+"**"+endS;
		}
		return result;
	}
}
