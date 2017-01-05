package com.amarsoft.p2ptrade.loan;

import java.util.Properties;


import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tradeclient.RunTradeService;
/**
 * �����ύ
 * ���������
 *      City:���ڳ���
 *      RealName:����
 *      Title:��ν
 *      PhoneNo:�ֻ�����
 *      
 * ��������� 
 * 	    STATUS:�ɹ���ʶ
 *      �������
 */
public class LoanUserOnlineHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		 
		return saveUserOnline(request);
		 
	} 
	
	/**
	 * �����ύ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject saveUserOnline(JSONObject request)throws HandlerException {
		if(request.get("City")==null || "".equals(request.get("City"))){
			throw new HandlerException("city.error");
		}
		
		if(request.get("RealName")==null || "".equals(request.get("RealName"))){
			throw new HandlerException("realname.error");
		}
		
		if(request.get("Title")==null || "".equals(request.get("Title"))){
			throw new HandlerException("title.error");
		}
		
		if(request.get("PhoneNo")==null || "".equals(request.get("PhoneNo"))){
			throw new HandlerException("phoneno.error");
		}
		
		String sCityCode = request.get("City").toString();
		String sRealName= request.get("RealName").toString();
		String sTitle = request.get("Title").toString();
		String sPhoneNo = request.get("PhoneNo").toString();
		
		String sCity = "";
		if("110000".equals(sCityCode)){
			sCity = "������";
		}else if("310000".equals(sCityCode)){
			sCity = "�Ϻ���";
		}else if("440100".equals(sCityCode)){
			sCity = "������";
		}else if("440300".equals(sCityCode)){
			sCity = "������";
		}else if("999999".equals(sCityCode)){
			sCity = "����";
		}
		
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
        JBOTransaction tx = null;
		try{  
			tx = jbo.createTransaction();
            BizObjectManager m = jbo.getManager("jbo.trade.user_note",tx);
            BizObject bo = m.newObject();
            bo.setAttributeValue("City", sCity);//���ڳ���
            bo.setAttributeValue("RealName", sRealName);//����
            bo.setAttributeValue("Title", sTitle);//��ν
            bo.setAttributeValue("PhoneNo", sPhoneNo);//�ֻ�
            bo.setAttributeValue("HandleTime", GeneralTools.getDate());//����ʱ��
            
            //��ʼ�����Ͷ���
			RunTradeService rt = new RunTradeService();
			// ���Ŵ�ϵͳ���ͽ������󣬷��� ��Ӧ����
			request.put("Method", "TelCustomerConsult");
			JSONObject traderesponse = rt.requestTransaction(request, jbo) ;
			System.out.println("���ر��ģ�  "+traderesponse.toJSONString());
            
            m.saveObject(bo);
            tx.commit();
            result.put("param", "ok");
		    return result;
		}
		catch(Exception e){
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new HandlerException("saveuseronline.error");
		}
	}
}