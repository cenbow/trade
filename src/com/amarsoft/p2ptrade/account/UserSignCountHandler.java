package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û�ǩ��ͳ��
 *
 */
public class UserSignCountHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		
		Parser.registerFunction("sum");
		Parser.registerFunction("count");
		//��ȡ����ֵ
		String userid = (String) request.get("UserID");
		JSONObject result = new JSONObject();
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_sign");
    
			String inputdate = StringFunction.getToday();
			//��ѯ�����Ƿ�ǩ��
			BizObjectQuery query = m.createQuery(" UserID=:UserID and inputdate=:inputdate");
			query.setParameter("UserID", userid).setParameter("inputdate", inputdate);
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				result.put("SignFlag", "S");
			}else{
				result.put("SignFlag", "F");
			}
			
			//��ѯ��ʷǩ��
			BizObjectManager m1 =jbo.getManager("jbo.trade.user_sign");
			BizObjectQuery query1 = m1.createQuery("select sum(tr.amount) as v.amount,count(tr.amount) as v.count from o,jbo.trade.transaction_record tr where tr.UserID=o.UserID and o.UserID=:UserID and tr.transtype='2050' and tr.direction='R' and o.inputdate=tr.transdate and tr.status='10'");
			query1.setParameter("UserID", userid);
			BizObject o1 = query1.getSingleResult(true);
			if(o1!=null){
				result.put("HistorySignSum", o1.getAttribute("amount").getDouble());//�ۻ�����
				System.out.println("HistorySignSumHistorySignSum=="+o1.getAttribute("amount").getDouble());
				result.put("HistorySignCount", o1.getAttribute("count").getInt());//�ۻ�ǩ������
				System.out.println("HistorySignCountHistorySignCountHistorySignCount=="+o1.getAttribute("count").getDouble());
			}else{
				result.put("HistorySignSum", "0.00");
				result.put("HistorySignCount", "0");
			}
			//��ѯ����ǩ������
//			BizObjectQuery query2 = m1.createQuery(" select tr.amount from o,jbo.trade.transaction_record tr where tr.UserID=o.UserID and o.UserID=:UserID and tr.transtype='2050' and tr.direction='R' and o.inputdate=:inputdate ");
			BizObjectQuery query2 = jbo.getManager("jbo.trade.transaction_record").createQuery(" SELECT amount FROM o where userid=:UserID and transdate =:inputdate and transtype='2050' and direction='R' and status='10'");
			query2.setParameter("UserID", userid).setParameter("inputdate", inputdate);
			BizObject o2 = query2.getSingleResult(true);
			if(o2!=null){
				result.put("TodaySignSum", o2.getAttribute("amount").getDouble());//����ǩ������
			}else
				result.put("TodaySignSum", "0.00");
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("register.error");
		}	
		return result;
	}
}
