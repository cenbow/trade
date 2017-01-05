package com.amarsoft.p2ptrade.personcenter;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.amarsoft.app.accounting.util.NumberTools;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.invest.P2pString;
/** 
 * ��������ͳ��
 * ���������
 * 		UserID:�˻����
 *
 */
public class UserIncomeHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		Parser.registerFunction("sum");
		Parser.registerFunction("count");
		return getAcount(request);		
	}
	  
	/**
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject getAcount(JSONObject request)throws HandlerException {
		
		JSONObject result = new JSONObject();
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}

		String sUserID = request.get("UserID").toString();
		
		JBOFactory jbo = JBOFactory.getFactory();
		try {
			
			//������Ŀ���
			BizObjectManager m = jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery q = m.createQuery(" select sum(investsum) as v.investsum from o where status='1' and UserID=:UserID");
			q.setParameter("UserID", sUserID);
			BizObject o = q.getSingleResult(false);
			double investsum = 0;

			if(o!=null){
				investsum = o.getAttribute("investsum").getDouble();
			}else
				investsum = 0;
			result.put("investsum", investsum);
			
			//Ԥ����
			/*
			BizObjectManager m1 = jbo.getManager("jbo.trade.income_schedule");
			BizObjectQuery q1 = m1.createQuery(" select sum(payinteamt) as v.payinteamt from o,jbo.trade.user_contract uc where uc.contractid = o.contractno and uc.status='1' and uc.UserID=:UserID");
			q1.setParameter("UserID", sUserID);
			BizObject o1 = q1.getSingleResult(false);
			double payinteamt = 0;

			if(o1!=null){
				payinteamt = o1.getAttribute("payinteamt").getDouble();
			}else
				payinteamt = 0;
				*/
			double payinteamt = getRuningInvestSum(sUserID);
			result.put("payinteamt", payinteamt);
						   
			//��������
			/*
			String updatetime = P2pString.addDateFormat(StringFunction.getTodayNow(), 3, -1,"yyyy/MM/dd");	
			BizObjectManager m2 = jbo.getManager("jbo.trade.transaction_record");
			BizObjectQuery q2 = m2.createQuery(" select sum(amount) as v.amount from o where status='10' and direction='R' and updatetime=:updatetime and UserID=:UserID");
			q2.setParameter("UserID", sUserID).setParameter("updatetime", updatetime);
			BizObject o2 = q2.getSingleResult(false);
			double amount = 0;

			if(o2!=null){
				amount = o2.getAttribute("amount").getDouble();
			}else
				amount = 0;
			result.put("lastamount", amount);
			*/
			//ƽ̨����
			double amount1 = getRewardIncome(sUserID,"");
			result.put("restoreamount", amount1);
			//�ۼ�����
			result.put("lastamount",payinteamt + getHistoryInvestSum(sUserID) + amount1);
			
			//��������
			long lYestoday = (new java.util.Date()).getTime()-3600*24*1000;
			Date dYestoday = new Date(lYestoday);
			SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
			String sYestoday = formatter.format(dYestoday);
			
			result.put("yestodayamount", getYestodayInvestIncome(sUserID)+ getRewardIncome(sUserID,sYestoday));
			//�����껯����
			result.put("yestodayreate",getYestodyRate(sUserID));
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return result;
	}
	
	private Object getYestodyRate(String sUserID)throws JBOException {
		
		double dLoanRate = 0d ;
		double dLoanAmount = 0d ;
		double dInvestSum = 0d ;
		double dSumRate = 0d ;
		
		String sSql = "Select o.LoanRate,o.LoanAmount,uc.InvestSum From jbo.trade.user_contract uc,o Where uc.ProjectID = o.SerialNo And uc.UserID = :userid";
		BizObjectQuery query = JBOFactory.createBizObjectQuery("jbo.trade.project_info", sSql).setParameter("userid", sUserID);
		List<BizObject> list = query.getResultList(false);
		for(BizObject obj : list){
			dLoanRate = obj.getAttribute("LoanRate").getDouble();//��Ŀ����
			dLoanAmount = obj.getAttribute("LoanAmount").getDouble();//��Ŀ���
			dInvestSum = obj.getAttribute("InvestSum").getDouble();//Ͷ�ʽ��
			dSumRate += NumberTools.round(dInvestSum/dLoanAmount*dLoanRate,2);//��������
		}
		ARE.getLog().info("�����껯������="+dSumRate+" %");
		return dSumRate ;
	}

	private String getPloanSetupDate()throws JBOException{
		return JBOFactory.getBizObjectManager("jbo.trade.ploan_setup").createQuery("").getSingleResult(false).getAttribute("curdeductdate").getString();
	}
	
	//��Ͷ����
	public  double getRuningInvestSum(String sUserID) throws JBOException{
		String sPloanSetupDate = getPloanSetupDate();
		BizObjectManager m3 = JBOFactory.getBizObjectManager("jbo.trade.income_detail");
		String sSql = "Select Sum(ActualPayInteAmt+ActualExpiationSum) as v.ActualAmount From o,jbo.trade.user_contract uc Where o.UserID = uc.UserID And uc.Status = '1' And uc.UserID = :userid";
		BizObject obj = m3.createQuery(sSql).setParameter("userid", sUserID).getSingleResult(false);
		
		//������Ͷ����
		double dInvestSum = 0;
		if(obj!=null)dInvestSum = obj.getAttribute("ActualAmount").getDouble();
		
		BizObjectManager m4 = JBOFactory.getBizObjectManager("jbo.trade.income_schedule");
		sSql = "Select Sum(PayInteAmt) as v.PayInteAmt,PayDate From o,jbo.trade.user_contract uc Where o.UserID = uc.UserID And PayDate like :month And uc.Status = '1' And uc.UserID = :userid ";
		obj = m4.createQuery(sSql).setParameter("userid", sUserID).setParameter("month", sPloanSetupDate.substring(0, 8)+"%").getSingleResult(false);
		//����Ӧ����Ϣ
		double dMonthInteAmt = 0;
		if(obj!=null)
			dMonthInteAmt = obj.getAttribute("PayInteAmt").getDouble();
		int iMonthDay = getCurrentMonthLastDay();//�����ж�����
		int itoday = Integer.parseInt(sPloanSetupDate.substring(8));//���µڼ���
		double dztSum = NumberTools.round(dInvestSum+(dMonthInteAmt/iMonthDay*itoday),2);
		return dztSum ;
	}
	
	//��ʷͶ������
	protected double getHistoryInvestSum(String sUserID) throws JBOException{
		BizObjectManager m3 = JBOFactory.getBizObjectManager("jbo.trade.income_detail");
		String sSql = "Select Sum(ActualPayInteAmt+ActualExpiationSum) as v.s From o,jbo.trade.user_contract uc Where o.UserID = :userid and uc.userid=o.userid and uc.status='3'";
		BizObject obj = m3.createQuery(sSql).setParameter("userid", sUserID).getSingleResult(false);
		if(obj==null)return 0d;
		return obj.getAttribute("s").getDouble();
	}
	
	//���ƽ̨����
	protected double getRewardIncome(String sUserID,String endDate)throws JBOException{
		//ƽ̨����
		BizObjectManager m3 = JBOFactory.getBizObjectManager("jbo.trade.transaction_record");
		String sql = " select sum(amount) as v.amount from o where status='10' and direction='R' and transtype in ('2030','2040','2050') and UserID=:UserID";
		if(endDate!=null && endDate.length()>0){
			sql += " and updatetime like :enddate";
		}
		BizObjectQuery q3 = m3.createQuery(sql);
		q3.setParameter("UserID", sUserID);
		if(endDate!=null && endDate.length()>0){
			q3.setParameter("enddate", endDate);
		}
		BizObject o3 = q3.getSingleResult(false);
		double amount1 = 0;

		if(o3!=null){
			amount1 = o3.getAttribute("amount").getDouble();
		}else
			amount1 = 0;
		return amount1;
	}
	
	//ȡ�õ�������(�������) 
	private int getYestodayMonthLastDay(){  
	    Calendar a = Calendar.getInstance();  
	    a.add(Calendar.DATE, -1);
	    a.set(Calendar.DATE, 1);//����������Ϊ���µ�һ��  
	    a.roll(Calendar.DATE, -1);//���ڻع�һ�죬Ҳ�������һ��  
	    int maxDate = a.get(Calendar.DATE);  
	    return maxDate;  
	}  
		
	//Ͷ���˵�ǰͶ�ʵ��������棨Ԥ�ƣ�������������ָ (������ͶͶ��)����Ӧ����Ϣ/��������
	protected double getYestodayInvestIncome(String sUserID)throws JBOException{
		BizObjectManager manager = JBOFactory.getBizObjectManager("jbo.trade.income_schedule");
		String sSql = "Select Sum(o.PayInteAmt) as v.PayInteAmt,PayDate From o,jbo.trade.user_contract uc Where o.UserID = uc.UserID And o.PayDate like :yesmonth And uc.Status = '1' And uc.UserID = :userid ";
		long lYestoday = (new java.util.Date()).getTime()-3600*24*1000;
		Date dYestoday = new Date(lYestoday);
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM");
		String sYesMonth = formatter.format(dYestoday);
		BizObject obj = manager.createQuery(sSql).setParameter("userid", sUserID).setParameter("yesmonth", sYesMonth + "%").getSingleResult(false);
		if(obj==null)return 0;
		//�����ж�����
		int iMonthDay = getYestodayMonthLastDay();
		return obj.getAttribute("PayInteAmt").getDouble()/iMonthDay;
	}
	
	//�����껯������
	/*
	public  double getLastLoanRate() throws JBOException{
		String sSql = "Select pj.LoanRate,pj.LoanAmount,uc.InvestSum From User_Contract uc,Project_Info pj Where uc.ProjectID = pj.SerialNo And uc.UserID = ?";
		
		PreparedStatement ps = null ;
		ResultSet rs = null ;
		double dLoanRate = 0d ;
		double dLoanAmount = 0d ;
		double dInvestSum = 0d ;
		double dSumRate = 0d ;
		try{
			ps = conn.prepareStatement(sSql);
			ps.setString(1, "2015020600000003");
			rs = ps.executeQuery();
			while(rs.next()){
				dLoanRate = rs.getDouble("LoanRate");//��Ŀ����
				dLoanAmount = rs.getDouble("LoanAmount");//��Ŀ���
				dInvestSum = rs.getDouble("InvestSum");//Ͷ�ʽ��
				dSumRate += NumberTools.round2(dInvestSum/dLoanAmount*dLoanRate);//��������
			}
			System.out.println("�����껯������="+dSumRate+" %");
		}finally{
			if(ps!=null)ps.close();
			if(rs!=null)rs.close();
		}
		
		return dSumRate ;
	}
	*/
	
	/** 
	 * ȡ�õ������� 
	 * */  
	public static int getCurrentMonthLastDay(){  
	    Calendar a = Calendar.getInstance();  
	    a.set(Calendar.DATE, 1);//����������Ϊ���µ�һ��  
	    a.roll(Calendar.DATE, -1);//���ڻع�һ�죬Ҳ�������һ��  
	    int maxDate = a.get(Calendar.DATE);  
	    return maxDate;  
	}  
}