package com.amarsoft.p2ptrade.contract;
/**
 * ��ֵ��ͬ 
 * ���������
 * 		Serialno ��ˮ��
 * 		UserID	�ͻ���
 * ���������
 * 		
 */
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.CodeManager;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class ContractRechargeHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		if(request.containsKey("UserID")==false)throw new HandlerException("ȱ���˺Ų���");
		try{
			if(request.containsKey("SerialNo")){
				return createExistContractInfo(request);
			}
			else{
				return createEmptyContractInfo(request);
			}
			
		}
		catch(JBOException je){
			je.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}
	
	private Object createExistContractInfo(JSONObject request)throws HandlerException,JBOException {
		BizObjectManager manager = JBOFactory.getBizObjectManager("jbo.trade.account_info");
		String sql = "select o.ACCOUNTBELONG,o.ACCOUNTNO,o.ACCOUNTNAME,u.username,d.realname,d.certid,d.certtype,tr.INPUTTIME,tr.AMOUNT"
			+ " from o,jbo.trade.transaction_record tr,jbo.trade.user_account u,jbo.trade.account_detail d"
			+ " where o.userid=tr.userid and o.userid=:userid and u.userid=o.userid and d.userid=o.userid and tr.status='10'"
			+ " and tr.serialno=:serialno";
		
		BizObjectQuery query = manager.createQuery(sql);
		query.setParameter("userid", request.get("UserID").toString());
		query.setParameter("serialno", request.get("SerialNo").toString());
		BizObject obj = query.getSingleResult(false);
		JSONObject result = new JSONObject();
		if(obj==null){
			throw new HandlerException("���޽��");
		}
		else{
			result.put("AccountBelong", CodeManager.getCodeManager().getItemName("BankNo",obj.getAttribute("AccountBelong").getString()));
			result.put("AccountNo", obj.getAttribute("AccountNo").getString());
			result.put("AccountName", obj.getAttribute("AccountName").getString());
			result.put("username", obj.getAttribute("UserName").getString());
			
			result.put("realname", obj.getAttribute("realname").getString());
			result.put("certid", obj.getAttribute("certid").getString());
			result.put("certtype", CodeManager.getCodeManager().getItemName("CertType", obj.getAttribute("certtype").getString()) );
			result.put("today", getCHDate(obj.getAttribute("INPUTTIME").getString().substring(0, 10)));
			result.put("transsum", getCHDate(obj.getAttribute("AMOUNT").getString()));
			
			
		}
		return result;
	}

	//�����հ׺�ͬ��Ϣ
	private Object createEmptyContractInfo(JSONObject request)throws HandlerException,JBOException{
		//��ѯͶ���˺ͽ������Ϣ��Ϣ
		BizObjectManager manager = JBOFactory.getBizObjectManager("jbo.trade.account_detail");
		String sql = "select o.realname,ua.username,o.certid,o.certtype,ai.AccountBelong,ai.AccountNo,ai.AccountName from "
				+" jbo.trade.user_account ua,o,jbo.trade.account_info ai"
				+ " where ua.userid=o.userid and o.userid=:userid and o.userid=ai.userid";
		
		BizObjectQuery query = manager.createQuery(sql);
		query.setParameter("userid", request.get("UserID").toString());
		BizObject bizObj = query.getSingleResult(false);
		if(bizObj!=null){
			JSONObject obju = new JSONObject();
			obju.put("realname", bizObj.getAttribute("realname").getString());
			obju.put("username", bizObj.getAttribute("username").getString());
			obju.put("certid", bizObj.getAttribute("certid").getString());
			obju.put("certtype", CodeManager.getCodeManager().getItemName("CertType", bizObj.getAttribute("certtype").getString()) );
			obju.put("today", getCHDate(StringFunction.getToday()));
			obju.put("AccountBelong", CodeManager.getCodeManager().getItemName("BankNo",bizObj.getAttribute("AccountBelong").getString()));
			obju.put("AccountNo", bizObj.getAttribute("AccountNo").getString());
			obju.put("AccountName", bizObj.getAttribute("AccountName").getString());
			
			return obju;
		}
		else{
			throw new HandlerException("��Ч�Ŀͻ���");
		}
	}
	private String getCHDate(String date){
		if(date==null)return "";
		if(date.length()!=10){
			return date;
		}
		else{
			return date.substring(0,4) + "��" + date.substring(5,7) + "��" + date.substring(8,10) + "��";
		}
	}
}
