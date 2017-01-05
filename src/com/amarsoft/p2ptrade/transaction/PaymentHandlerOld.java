package com.amarsoft.p2ptrade.transaction;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/*
 *  *@ ����
 * 2014-5-16
 * ����
 *     Paytype�� ��������   1һ�㻹�� Payment  ,3��ǰ����REPayment    ��ͬ�� 
 * ���    
 *     ����ƻ��б��ڴΡ�Ӧ�����ա�������Ϣ��ƽ̨����ѡ������ѡ�ƽ̨����ѡ���������ѡ���ǰ����ΥԼ��
 * ����¼
 */
public class PaymentHandlerOld extends TradeHandler{
	
	//�����Ĳ���
	private double amtDouble;//���󻹿��
	private String loanSerialnoString;// ��ݺ� �Ŵ�
	private String contractnoString;// ��ͬ�� �Ŵ�
	private String paytypeString; //��������
	private Map<String, Object> map = new HashMap<String, Object>();
	private String sContractID = "";
	
	//�Ŵ����ر��Ĳ���
	private String payamt;//�ܽ��
	private double insuremanagement_fee;//	���������
	private double penal_value;//	����ΥԼ��
	private double thaw_amount;//�ⶳ���
	private double plantmange;//ƽ̨�����
	private double managefee;//������
	private double plantfee;//ƽ̨�����
	private double actualPayCorpusAmt;//����
	private double actualPayInteAmt;//��Ϣ
	private double actualPayFineAmt;//��Ϣ
	private double actualPayCompoundInte;//����
	private String userPhoneNo;
	private String tuserPhoneNo;
	private String proName;
	private String billSerialno;
	//nn
	@Override
	protected Object requestObject(JSONObject request, JBOFactory jbo) throws HandlerException {
			if (request.get("PayType")==null||request.get("ContractId")==null||request.get("Amt")==null) {
				throw new HandlerException("request.invalid");
			}
		
			try{
				sContractID = (String)request.get("ContractId")==null?"":(String)request.get("ContractId");//��ͬ��
				if("".equals(sContractID)||null==sContractID) throw new HandlerException("common.contractnotexist");
				
				paytypeString = (String)request.get("PayType")==null?"":(String)request.get("PayType");//��������
				if("".equals(paytypeString)||null==paytypeString) throw new HandlerException("transrun.err");
				
				amtDouble = Double.parseDouble((String)request.get("Amt")==null?"0":(String)request.get("Amt"));//���
				if(amtDouble<=0) throw new HandlerException("transrun.err");
				map = getMapName(jbo);//�÷������˻�
			    
			    //�û�״̬�ж�  account_freeze
			    GeneralTools.userAccountStatus((String)map.get("userid"),"") ;
			
			    double usbblance = tUsablebalance(jbo,(String)map.get("userid"));// Ͷ�����˻����ý��  
				if (usbblance < amtDouble||amtDouble<=0) {
					throw new HandlerException("tusaamtnoenough.error");
				}
			    
				String[] strings = getloanserialno(jbo).split("@",-1);
				loanSerialnoString = strings[0];
				contractnoString = strings[1];
				if ("3".equals(paytypeString)) {
					request.put("Method", "REPayment");
				}else if ("1".equals(paytypeString)) {
					request.put("Method", "Payment");
				}else{
					request.put("Method", "");
				}
				
				//�ֻ���
				userPhoneNo = getPhoneTel((String)map.get("userid"), jbo);
				tuserPhoneNo =  getPhoneTel((String)map.get("tuserid"), jbo);
				if(userPhoneNo.length()==0||tuserPhoneNo.length()==0){
				  throw new HandlerException("common.emptymobile");//δ�󶨿�
				}
				
				proName = getProName(jbo);
				if("".equals(proName)||null==proName) throw new HandlerException("writeaccount.error");//δ�󶨿�
				
				//�� �˻�     �����   ������˾  ƽ̨
				String frozenstatus = "1";
				frozenaccount(map,frozenstatus ,jbo);
	 			//�������˽��
				frozensigler(map,frozenstatus,request, jbo);
				//�˻�����
				request.put("Amt", amtDouble);
				request.put("LoanSerialNo", loanSerialnoString);
				request.put("ContractSerialNo", contractnoString);
				
//				try{
//					tx.commit();
//				}catch(Exception e){
//					tx.rollback();
//					e.printStackTrace();
//					throw new HandlerException("transrun.err");
//				}
//				
				
			}catch(Exception e){
				e.printStackTrace();
				throw new HandlerException("transrun.err");
			}
			
			return request;
	}

	@Override
	protected Object responseObject(JSONObject request, JSONObject response, String logid, String transserialno, 	JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		payamt =GeneralTools.numberFormat(Double.parseDouble((String)response.get("payamt")), 0, 2) ;//�ܽ��
		billSerialno = (String)response.get("sbillSerialNo")==null?"":(String)response.get("sbillSerialNo");
		try {
			
			writeaccount(map ,response, jbo);
			// ���� �˻�
			frozenaccount(map,"2" ,jbo);
			//���뽻�׼�¼
			//TODO ���뻹��ף�״̬Ϊ03-�����ؽ��
			initTransactionRecord(request,jbo);
			insertTraction(map,payamt,request, jbo);
			
			try{
				senmsg();//��������
			}catch(Exception e){
                ARE.getLog().info("���ŷ���ʧ�ܽ���ˣ���ͬ�ţ�"+sContractID);
			}
			
			try{
				tsenmsg();//Ͷ����
			}catch(Exception e){
                ARE.getLog().info("���ŷ���ʧ��Ͷ���ˣ���ͬ�ţ�"+sContractID);
			}
			try{
				//ȡ����ʱ��
				String sFinishDate = (String)response.get("sFinishDate")==null?"":(String)response.get("sFinishDate");
				if(!("".equals(sFinishDate)||null==sFinishDate)){
				//��Ϊ�շ��ͽ������
					try{
						senmsgfinish();//�����
					}catch(Exception e){
		                ARE.getLog().info("���ŷ���ʧ�ܽ���ˣ���ͬ�ţ�"+sContractID);
					}
					
					try{
						sentmsgfinish();//Ͷ����
					}catch(Exception e){
		                ARE.getLog().info("���ŷ���ʧ��Ͷ���ˣ���ͬ�ţ�"+sContractID);
					}
					
				}
			}catch(Exception e){
                ARE.getLog().info("���ŷ���ʧ��Ͷ���ˣ���ͬ�ţ�"+sContractID);
			}
				
				
			
			
		}catch (HandlerException e) {
			// TODO: handle exception
			e.printStackTrace();
			try {
				tx.rollback();
			} catch (Exception e1) {
				e1.printStackTrace();
				throw new HandlerException("transrun.err");
			}
			throw new HandlerException("transrun.err");
		}
		return response;
	}
	

	/**
	 * @param request
	 * @param jbo
	 * @return
	 * @throws HandlerException 
	 */
	private String getloanserialno(JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		String loanseriaString ="";
		String contractnoString ="";
		BizObjectManager manager = null;
		try {
			manager = jbo.getManager("jbo.trade.ti_contract_info");
			BizObjectQuery query = manager.createQuery("select LOANNO ,CONTRACTNO from o where  CONTRACTID  =:CONTRACTID");
			query.setParameter("CONTRACTID",sContractID);
			BizObject bObject = query.getSingleResult(false);
			if (bObject!=null) {
				loanseriaString = bObject.getAttribute("LOANNO").getValue()==null?"":bObject.getAttribute("LOANNO").getString();
				contractnoString = bObject.getAttribute("CONTRACTNO").getValue()==null?"":bObject.getAttribute("CONTRACTNO").getString();
			}else {
				throw new HandlerException("common.contractidnotexist");
			}
		}catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return loanseriaString+"@"+contractnoString;
	}
	
	/**
	 * @throws HandlerException 
	 * 
	 */
	private void tsenmsg() throws HandlerException {
		// TODO Auto-generated method stub
		// ���Ͷ������� Ͷ����
		 GeneralTools.sendSMS("P2P_SKCG", tuserPhoneNo, setP2P_SKCG());
	}
	
	private void senmsg() throws HandlerException {
		// TODO Auto-generated method stub
		// ���Ͷ������� �����
		 GeneralTools.sendSMS("P2P_HKCG", userPhoneNo, setP2P_HKCG());
	}
	
	private void sentmsgfinish() throws HandlerException {
		// TODO Auto-generated method stub
		// ���Ͷ������ѻ����,Ͷ���˴����
		 GeneralTools.sendSMS("P2P_SKCGLAST", tuserPhoneNo, setP2P_SKCGLAST());
	}
	
	private void senmsgfinish() throws HandlerException {
		// TODO Auto-generated method stub
		// ���Ͷ������ѻ����,����˴����
		 GeneralTools.sendSMS("P2P_DKHQ", userPhoneNo, setP2P_DKHQ());
	}
	
	private HashMap<String, Object> setP2P_SKCGLAST() {
		// TODO Auto-generated method stub
		HashMap<String , Object> map   = new HashMap<String, Object>();
		map.put("ProjectName", proName);
		return map;
	}

	private HashMap<String, Object> setP2P_DKHQ() {
		// TODO Auto-generated method stub
		HashMap<String , Object> map   = new HashMap<String, Object>();
		map.put("ContractNo", contractnoString);
		return map;
	}
	
	private HashMap<String, Object> setP2P_HKCG()  {
		// TODO Auto-generated method stub
		HashMap<String , Object> map   = new HashMap<String, Object>();
		map.put("ContractNo", contractnoString); 
		map.put("PayAmount", GeneralTools.numberFormat(GeneralTools.round(amtDouble,2),0,2));
		return map;
	}
	
	private HashMap<String, Object> setP2P_SKCG()  {
		// TODO Auto-generated method stub
		HashMap<String , Object> map   = new HashMap<String, Object>();
		map.put("Amount", GeneralTools.numberFormat(GeneralTools.round(penal_value+actualPayCorpusAmt+actualPayInteAmt+actualPayFineAmt+actualPayCompoundInte,2), 0, 2));
		map.put("Date", StringFunction.getToday());
		map.put("ProjectName", proName);
		return map;
	}
	
	/**
	 * @return
	 * @throws HandlerException 
	 */

	private String getProName(JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		BizObjectManager manager;
		String projectname = "";
		try {
			manager = jbo.getManager("jbo.trade.project_info");
			BizObjectQuery query = manager.createQuery("  CONTRACTID = :CONTRACTID");
			query.setParameter("CONTRACTID",contractnoString);
			BizObject boBizObject = query.getSingleResult(false);
			projectname = boBizObject.getAttribute("PROJECTNAME").getValue()==null?"":boBizObject.getAttribute("PROJECTNAME").getString()
					+"|"+boBizObject.getAttribute("SERIALNO").getValue()==null?"":boBizObject.getAttribute("SERIALNO").getString();
		} catch (Exception e) {
			e.printStackTrace();
			// TODO Auto-generated catch block
			//e.printStackTrace();
			//throw new HandlerException("sms.templetid.error");
		}
		return projectname;
	}
	
		/**
	 *    ���������¼�����
		 * @throws HandlerException 
	 */
	private void insertbackdetail( JSONObject response ,JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		BizObjectManager manager;
		List<JSONObject> payment_detail=  (List<JSONObject>) response.get("payment_detail");
		try {
		manager = jbo.getManager("jbo.trade.acct_back_detail",tx);
		for (int i = 0; i < payment_detail.size(); i++) {
		    BizObject o =manager.newObject();
			JSONObject jObjectpaymentdetail = payment_detail.get(i);
			if ("3".equals(paytypeString)) {
				o.setAttributeValue("SEQID", jObjectpaymentdetail.get("ahead_rpterm"));//�ڴ�
				o.setAttributeValue("ACTUALPAYCORPUSAMT", jObjectpaymentdetail.get("ahead_capital"));//�����
				o.setAttributeValue("ACTUALPAYINTEAMT", jObjectpaymentdetail.get("ahead_int"));//������Ϣ
				o.setAttributeValue("ACTUALFINEAMT", jObjectpaymentdetail.get("ahead_oint"));//���Ϣ
				o.setAttributeValue("ACTUALPAYFEEAMT1", jObjectpaymentdetail.get("ahead_insureamount"));//ʵ��������
				o.setAttributeValue("ACTUALPAYFEEAMT2", jObjectpaymentdetail.get("ahead_plantservicefee"));//ʵ��ƽ̨�����
				o.setAttributeValue("ACTUALEXPIATIONSUM", response.get("penal_value"));//����ΥԼ��
				o.setAttributeValue("PAYTYPE", paytypeString);//��������   1һ�㻹�� Payment  ,3��ǰ����
			}else if ("1".equals(paytypeString)) {
				o.setAttributeValue("SEQID", jObjectpaymentdetail.get("manual_rpterm"));//�ڴ�
				o.setAttributeValue("ACTUALPAYCORPUSAMT", jObjectpaymentdetail.get("manual_capital"));//�����
				o.setAttributeValue("ACTUALPAYINTEAMT", jObjectpaymentdetail.get("manual_int"));//������Ϣ
				o.setAttributeValue("ACTUALFINEAMT", jObjectpaymentdetail.get("manual_oint"));//���Ϣ
				o.setAttributeValue("ACTUALPAYFEEAMT1", jObjectpaymentdetail.get("manual_insureamount"));//ʵ��������
				o.setAttributeValue("ACTUALPAYFEEAMT2", jObjectpaymentdetail.get("plantservicefee"));//ʵ��ƽ̨�����
			}
				o.setAttributeValue("ACTUALPLANTMANAGE", response.get("plantmange"));//ʵ��ƽ̨�����
				o.setAttributeValue("ACTUALGUARANTEEDMANAGE", response.get("insuremanagement_fee"));//ʵ�����������
				o.setAttributeValue("LOANSERIALNO",loanSerialnoString);//��ݺ�
			    manager.saveObject(o);
		}
		
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}

		/**
	 * @param map
	 * @param response
	 * @param jbo
		 * @throws HandlerException 
		 * 
	 */
	private void writeaccount(Map<String, Object> map ,JSONObject response, JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		BizObjectManager manager= null;
		
		insuremanagement_fee = GeneralTools.round(Double.parseDouble((String) response.get("insuremanagement_fee")), 2);//	���������
		penal_value = GeneralTools.round(Double.parseDouble((String) response.get("penal_value")), 2);//	����ΥԼ��
		thaw_amount = GeneralTools.round(Double.parseDouble((String) response.get("thaw_amount")), 2);//�ⶳ���(��۽��  ��Ϊ��ֵ)
		plantmange = GeneralTools.round(Double.parseDouble((String) response.get("plantmange")), 2);//ƽ̨�����
		managefee = GeneralTools.round(Double.parseDouble((String) response.get("managefee")), 2);//������
		plantfee = GeneralTools.round(Double.parseDouble((String) response.get("plantfee")), 2);//ƽ̨�����
		actualPayCorpusAmt = GeneralTools.round(Double.parseDouble((String) response.get("actualPayCorpusAmt")), 2);//����
		actualPayInteAmt = GeneralTools.round(Double.parseDouble((String) response.get("actualPayInteAmt")), 2);//��Ϣ
		actualPayFineAmt = GeneralTools.round(Double.parseDouble((String) response.get("actualPayFineAmt")), 2);//��Ϣ
		actualPayCompoundInte = GeneralTools.round(Double.parseDouble((String) response.get("actualPayCompoundInte")), 2);//����
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid  in ( :tuserid, :userid)");
			query.setParameter("tuserid",(String) map.get("tuserid"));//Ͷ�����˻�
			query.setParameter("userid",(String) map.get("userid"));//������˻�
			List<BizObject> list = query.getResultList(true);
			if (list != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					//double a =0d;
					double usablebalance = Double.parseDouble(o.getAttribute("USABLEBALANCE").getValue()==null?"0":o.getAttribute("USABLEBALANCE").getString());
					String userid = o.getAttribute("USERID").getValue()==null?"":o.getAttribute("USERID").getString();
					//double frozenbalance =Double.parseDouble(o .getAttribute("frozenbalance").getValue()==null?"0":o.getAttribute("frozenbalance").getString());//�����˻��������
					 //�˻����� 
					if (((String) map.get("tuserid")).equals(userid)) {
						//Ͷ���˼���    �� �� ��
						usablebalance = usablebalance + actualPayCorpusAmt+actualPayInteAmt+actualPayFineAmt+actualPayCompoundInte+penal_value;//�� �� �� �� ΥԼ��
					}else if (((String) map.get("userid")).equals(userid)) {
					//	a = amtDouble;
						// �����Ϊ��ֵ�Ľ��
						if (thaw_amount>0) {
							usablebalance=usablebalance+thaw_amount;
						}
					} 
					o.setAttributeValue("USABLEBALANCE", GeneralTools.round(usablebalance, 2));
					//o.setAttributeValue("frozenbalance", GeneralTools.round(frozenbalance-a,2));
					manager.saveObject(o);
				}
			}else {
				throw new HandlerException("writeaccount.error");
			}

//			//�����˻�
			manager = jbo.getManager("jbo.trade.org_account_info", tx);
			BizObjectQuery bObjectQuery = manager.createQuery(" serialno in (:puseridserialno,:duseridserialno)");
			bObjectQuery.setParameter("puseridserialno",(String) map.get("puseridserialno"));//ƽ̨�˻�orgid
			bObjectQuery.setParameter("duseridserialno",(String) map.get("duseridserialno"));//������˾�˻�orgid
			List<BizObject> listorgid = bObjectQuery.getResultList(true);

			if (listorgid != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < listorgid.size(); i++) {
					BizObject oo = listorgid.get(i);
					double usablebalance = Double.parseDouble(oo.getAttribute("USABLEBALANCE").getValue()==null?"0":oo.getAttribute("USABLEBALANCE").getString());
					String sAccountType = oo.getAttribute("AccountType").getValue()==null?"":oo.getAttribute("AccountType").getString();
					if ("0103".equals(sAccountType)) {
						//ƽ̨�˻�   ƽ̨�����
						usablebalance =usablebalance+plantmange+plantfee;// ƽ̨�����   ƽ̨�����
					}else if ("0202".equals(sAccountType)) {
						//������˾�˻�   ������
						usablebalance = usablebalance +insuremanagement_fee+managefee; //���������    ������
					}				
					oo.setAttributeValue("USABLEBALANCE", GeneralTools.round(usablebalance, 2));
					manager.saveObject(oo);
				}
			}else {
				throw new HandlerException("writeaccount.error");
			}
		} catch (HandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("common.usernotexist");
		} catch (JBOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}

	/**
	 * @param request
	 * @param jbo
	 * @return 
	 * @throws HandlerException 
	 */
	private void frozensigler(Map<String, Object> map,String lockflag,JSONObject request, JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		   BizObjectManager manager = null;
		   Double amt=Double.parseDouble((String) request.get("Amt"));
		try {   
			String  userid= (String) map.get("userid");
			manager = jbo.getManager("jbo.trade.user_account",tx);
			BizObjectQuery query = manager.createQuery("userid=:userid ");
			query.setParameter("userid", userid);
			BizObject bizObject = query.getSingleResult(true);
			double usablebalance =Double.parseDouble(bizObject .getAttribute("USABLEBALANCE").getValue()==null?"0":bizObject.getAttribute("USABLEBALANCE").getString());//�����˻��������
			//double frozenbalance =Double.parseDouble(bizObject .getAttribute("frozenbalance").getValue()==null?"0":bizObject.getAttribute("frozenbalance").getString());//�����˻��������
			if (usablebalance - amt>=0) {
				usablebalance = usablebalance - amt;
				// ���      ������     
				bizObject.setAttributeValue("USABLEBALANCE",GeneralTools.round(usablebalance, 2));
			//	bizObject.setAttributeValue("frozenbalance",GeneralTools.round(frozenbalance+amt, 2));
			}else {
				//�˻����� ���ȳ�ֵ
				throw new HandlerException("usablebalance.notenough");
			}
			manager.saveObject(bizObject);
		} catch (HandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("usablebalance.notenough");
			
		} catch (JBOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}
	
 	/*
 	 * 
 	 * 	 ���� Ͷ���˽���� ���� ƽ̨�˻�
 	 * 
 	 * */
		private void frozenaccount(Map<String, Object> map,String frozenstatus ,JBOFactory jbo) throws HandlerException {
			BizObjectManager manager;
			try { 
				//�����˻�
				manager = jbo.getManager("jbo.trade.user_account", tx);
				BizObjectQuery query = manager.createQuery("userid  in (:tuserid, :userid)");
				query.setParameter("tuserid",(String) map.get("tuserid"));//Ͷ�����˻�
				query.setParameter("userid",(String) map.get("userid"));//������˻�
				List<BizObject> list = query.getResultList(true);
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						BizObject o = list.get(i);
						o.setAttributeValue("LOCKFLAG", frozenstatus);
						manager.saveObject(o);
					}
				}
				//�����˻�
				manager = jbo.getManager("jbo.trade.org_account_info", tx);
				BizObjectQuery bObjectQuery = manager.createQuery(" serialno in (:puseridserialno,:duseridserialno)");
				bObjectQuery.setParameter("puseridserialno",(String) map.get("puseridserialno"));//ƽ̨�˻�orgid
				bObjectQuery.setParameter("duseridserialno",(String) map.get("duseridserialno"));//������˾�˻�orgid
				List<BizObject> listorg = bObjectQuery.getResultList(true);
				if (listorg != null) {
					JSONArray array = new JSONArray();
					for (int i = 0; i < listorg.size(); i++) {
						BizObject oo = listorg.get(i);
						oo.setAttributeValue("LOCKFLAG", frozenstatus);
						manager.saveObject(oo);
					}
				}
			} catch (JBOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw new HandlerException("makeamountfrozen.error");
			}
		}

		/***
		 * ��ȡͶ����tuserid�������userid��������˾������ˮ��duseridserialno��ƽ̨�����˻���ˮ��puseridserialno
		 * 
		 * ***/
		
		private Map<String, Object> getMapName(JBOFactory jbo) throws HandlerException {
		Map<String, Object> map = new HashMap<String, Object>();
		 
		BizObjectManager manager = null;
		try {
			manager = jbo.getManager("jbo.trade.user_contract",tx);
			BizObjectQuery query = manager.createQuery("CONTRACTID=:CONTRACTID ");
			query.setParameter("CONTRACTID", sContractID);
			List<BizObject> list = query.getResultList(false);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					String userrelativetype = o.getAttribute("RELATIVETYPE").getValue()==null?"":o.getAttribute("RELATIVETYPE").getString();
					if ("001".equals(userrelativetype)) {
						map.put("userid", o.getAttribute("USERID").getValue()==null?"":o.getAttribute("USERID").getString()); // ������˻�
					} else if ("002".equals(userrelativetype)) {
						map.put("tuserid", o.getAttribute("USERID").getValue()==null?"":o.getAttribute("USERID").getString());// Ͷ�����˻�
					}else {
						throw new HandlerException("common.usernotexist");
					}
				}
			}
			manager = jbo.getManager("jbo.trade.org_account_info",tx);
			BizObjectQuery qObjectQuery = manager.createQuery(
					" SELECT toa.serialno,toa.AccountType FROM jbo.trade.org_account_info toa WHERE " +
					"toa.accounttype in ('0202','0103') " +
					"AND toa.status='1' ");
			qObjectQuery.setParameter("CONTRACTID", sContractID);
			qObjectQuery.setParameter("CONTRACTSERIALNO", sContractID);
			List<BizObject> listtype = qObjectQuery.getResultList(false);
			if (listtype != null) {
				int m = 0;
				int n = 0;
				for (int i = 0; i < listtype.size(); i++) {
					BizObject oo = listtype.get(i);
					if (oo!=null) {
					
						String sAccountType = oo.getAttribute("AccountType").getValue()==null?"":oo.getAttribute("AccountType").getString();
						if ("0202".equals(sAccountType)) {
							// ������˾
							map.put("duseridserialno", oo.getAttribute("serialno").getValue()==null?"":oo.getAttribute("serialno").getString());// ������˾�����������˻�
							m++;
						} else if ("0103".equals(sAccountType)) {
							// ƽ̨�˻�
							//TODO �߼�����
							map.put("puseridserialno", oo.getAttribute("serialno").getValue()==null?"":oo.getAttribute("serialno").getString());// ����ƽ̨���뻧
							n++;
						}
						
						if(i==listtype.size()-1){
							if(m<=0) {
								ARE.getLog().debug("δ���õ�����˾�����������˻����ݣ�");
								throw new HandlerException("transrun.err");
							}
							if(n<=0) {
								ARE.getLog().debug("δ��������ƽ̨�����ʻ����ݣ�");
								throw new HandlerException("transrun.err");
							}
						}
						
						
					}else {
						ARE.getLog().debug("δ���õ�����˾��ƽ̨�ʻ����ݣ�");
						throw new HandlerException("transrun.err");
					}
				}
			}else {
				throw new HandlerException("transrun.err");
			}
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return map;
	}
		
	/*
	 * 
	 * ���뽻�׼�¼
	 * */
	private  void  insertTraction(Map<String, Object> map,String payamt,JSONObject request,JBOFactory jbo) throws HandlerException {
		String sTUserID = (String) map.get("tuserid");
		String sUserID = (String) map.get("userid");
		Map<String, Object> tuserMap = new HashMap<String, Object>();
		Map<String, Object> userMap = new HashMap<String, Object>();
		Map<String, Object> porgMap = new HashMap<String, Object>();
		Map<String, Object> dorgMap = new HashMap<String, Object>();
		Map<String ,Object> smap = getmap(jbo,map);
		tuserMap = (Map<String, Object>) smap.get("tuserid");
		userMap = (Map<String, Object>) smap.get("userid");
		porgMap = (Map<String, Object>) smap.get("porgMap");//ƽ̨��˾
		dorgMap = (Map<String, Object>) smap.get("dorgMap");//������˾
		BizObjectManager m =null;
		String sInputDate =GeneralTools.getDate();
		String sInputTime =GeneralTools.getTime();
		try {
			m = jbo.getManager("jbo.trade.transaction_record",tx);
			double balance = Double.parseDouble(tuserMap.get("BALANCE").toString());
			balance = GeneralTools.round(balance-penal_value-actualPayCorpusAmt-actualPayInteAmt-actualPayFineAmt-actualPayCompoundInte, 2);
			if(penal_value+actualPayCorpusAmt+actualPayInteAmt+actualPayFineAmt+actualPayCompoundInte>0){
				if(penal_value>0){ 
					BizObject o = m.newObject();
					o.setAttributeValue("USERID", sTUserID);//�û���� Ͷ����
					o.setAttributeValue("AMOUNT", penal_value);//���׽��
					o.setAttributeValue("ACTUALAMOUNT", penal_value);//���׽��
					o.setAttributeValue("TRANSTYPE", "1090");//��������
					o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					o.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					o.setAttributeValue("BALANCE", balance+penal_value);// ���
					o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
					o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
					o.setAttributeValue("REMARK","Ͷ������:����ΥԼ��");//��ע
					o.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 1000));//����ʱ��
					o.setAttributeValue("TRANSDATE", sInputDate);//��������
					o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					o.setAttributeValue("STATUS", "10");//����״̬
					o.setAttributeValue("HANDLCHARGE", "0");//������
					o.setAttributeValue("ReBillNo",billSerialno);
					o.setAttributeValue("ACTUALEXPIATIONSUM",penal_value);//����ΥԼ��
					m.saveObject(o);
				}
				if(actualPayCorpusAmt>0){
					BizObject o = m.newObject();
					o.setAttributeValue("USERID", sTUserID);//�û���� Ͷ����
					o.setAttributeValue("AMOUNT", actualPayCorpusAmt);//���׽��
					o.setAttributeValue("ACTUALAMOUNT", actualPayCorpusAmt);//���׽��
					o.setAttributeValue("TRANSTYPE", "1090");//��������
					o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					o.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					o.setAttributeValue("BALANCE",balance+penal_value+actualPayCorpusAmt);// ���
					o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
					o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
					o.setAttributeValue("REMARK","Ͷ������:����");//��ע
					o.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 2000));//����ʱ��
					o.setAttributeValue("TRANSDATE", sInputDate);//��������
					o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					o.setAttributeValue("STATUS", "10");//����״̬
					o.setAttributeValue("HANDLCHARGE", "0");//������
					o.setAttributeValue("ReBillNo",billSerialno);
					o.setAttributeValue("ACTUALPAYCORPUSAMT",actualPayCorpusAmt);//����
					m.saveObject(o);
				}
				if(actualPayInteAmt>0){
					BizObject o = m.newObject();
					o.setAttributeValue("USERID", sTUserID);//�û���� Ͷ����
					o.setAttributeValue("AMOUNT", actualPayInteAmt);//���׽��
					o.setAttributeValue("ACTUALAMOUNT", actualPayInteAmt);//���׽��
					o.setAttributeValue("TRANSTYPE", "1090");//��������
					o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					o.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					o.setAttributeValue("BALANCE",balance+penal_value+actualPayCorpusAmt+actualPayInteAmt);// ���
					o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
					o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
					o.setAttributeValue("REMARK","Ͷ������:��Ϣ");//��ע
					o.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 3000));//����ʱ��
					o.setAttributeValue("TRANSDATE", sInputDate);//��������
					o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					o.setAttributeValue("STATUS", "10");//����״̬
					o.setAttributeValue("HANDLCHARGE", "0");//������
					o.setAttributeValue("ReBillNo",billSerialno);
					o.setAttributeValue("ACTUALPAYINTEAMT",actualPayInteAmt);//��Ϣ
					m.saveObject(o);
				}
				if(actualPayFineAmt>0){
					BizObject o = m.newObject();
					o.setAttributeValue("USERID", sTUserID);//�û���� Ͷ����
					o.setAttributeValue("AMOUNT", actualPayFineAmt);//���׽��
					o.setAttributeValue("ACTUALAMOUNT", actualPayFineAmt);//���׽��
					o.setAttributeValue("TRANSTYPE", "1090");//��������
					o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					o.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					o.setAttributeValue("BALANCE",balance+penal_value+actualPayCorpusAmt+actualPayInteAmt+actualPayFineAmt);// ���
					o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
					o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
					o.setAttributeValue("REMARK","Ͷ������:��Ϣ");//��ע
					o.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 4000));//����ʱ��
					o.setAttributeValue("TRANSDATE", sInputDate);//��������
					o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					o.setAttributeValue("STATUS", "10");//����״̬
					o.setAttributeValue("HANDLCHARGE", "0");//������
					o.setAttributeValue("ReBillNo",billSerialno);
					o.setAttributeValue("ACTUALFINEAMT",actualPayFineAmt);//��Ϣ
					m.saveObject(o);
				}
				if(actualPayCompoundInte>0){
					BizObject o = m.newObject();
					o.setAttributeValue("USERID", sTUserID);//�û���� Ͷ����
					o.setAttributeValue("AMOUNT", actualPayCompoundInte);//���׽��
					o.setAttributeValue("ACTUALAMOUNT", actualPayCompoundInte);//���׽��
					o.setAttributeValue("TRANSTYPE", "1090");//��������
					o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					o.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					o.setAttributeValue("BALANCE",balance+penal_value+actualPayCorpusAmt+actualPayInteAmt+actualPayFineAmt+actualPayCompoundInte);// ���
					o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
					o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
					o.setAttributeValue("REMARK","Ͷ������:����");//��ע
					o.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 5000));//����ʱ��
					o.setAttributeValue("TRANSDATE", sInputDate);//��������
					o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					o.setAttributeValue("STATUS", "10");//����״̬
					o.setAttributeValue("HANDLCHARGE", "0");//������
					o.setAttributeValue("ReBillNo",billSerialno);
					o.setAttributeValue("ACTUALCOMPDINTEAMT",actualPayCompoundInte);//����
					m.saveObject(o);
				}
			
			}
			
			double balanceInsure = Double.parseDouble(dorgMap.get("BALANCE").toString());
			balanceInsure = GeneralTools.round(balanceInsure-insuremanagement_fee-managefee, 2);
			if(insuremanagement_fee+managefee>0){//������
				//����
				if(insuremanagement_fee>0){
					BizObject bizObjectet = m.newObject();
					bizObjectet.setAttributeValue("USERID", dorgMap.get("ORGID"));//�û���� Ͷ����
					bizObjectet.setAttributeValue("AMOUNT",insuremanagement_fee);//���׽��
					bizObjectet.setAttributeValue("ACTUALAMOUNT",insuremanagement_fee);//���׽��
					bizObjectet.setAttributeValue("TRANSTYPE", "1090");//��������
					bizObjectet.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					bizObjectet.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					bizObjectet.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					bizObjectet.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					bizObjectet.setAttributeValue("BALANCE",balanceInsure+insuremanagement_fee);// ���
					bizObjectet.setAttributeValue("RELAACCOUNT",dorgMap.get("RELAACCOUNT"));//�����˻���ˮ��
					bizObjectet.setAttributeValue("RELAACCOUNTTYPE","002");//���׹����˻����� ���û��˻�/�����˻���
					bizObjectet.setAttributeValue("REMARK","������˾��������:���������["+GeneralTools.numberFormat(GeneralTools.round(insuremanagement_fee,2), 0, 2)+"]");//��ע
					bizObjectet.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 1000));//����ʱ��
					bizObjectet.setAttributeValue("TRANSDATE", sInputDate);//��������
					bizObjectet.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					bizObjectet.setAttributeValue("STATUS", "10");//����״̬
					bizObjectet.setAttributeValue("HANDLCHARGE", "0");//������
					bizObjectet.setAttributeValue("ACTUALGUARANTEEDMANAGE",insuremanagement_fee);//���������
					m.saveObject(bizObjectet);
				}
				
				if(managefee>0){
					BizObject bizObjectet = m.newObject();
					bizObjectet.setAttributeValue("USERID", dorgMap.get("ORGID"));//�û���� Ͷ����
					bizObjectet.setAttributeValue("AMOUNT",managefee);//���׽��
					bizObjectet.setAttributeValue("ACTUALAMOUNT",managefee);//���׽��
					bizObjectet.setAttributeValue("TRANSTYPE", "1090");//��������
					bizObjectet.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					bizObjectet.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					bizObjectet.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					bizObjectet.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					bizObjectet.setAttributeValue("BALANCE",balanceInsure+insuremanagement_fee+managefee);// ���
					bizObjectet.setAttributeValue("RELAACCOUNT",dorgMap.get("RELAACCOUNT"));//�����˻���ˮ��
					bizObjectet.setAttributeValue("RELAACCOUNTTYPE","002");//���׹����˻����� ���û��˻�/�����˻���
					bizObjectet.setAttributeValue("REMARK","������˾��������:������["+GeneralTools.numberFormat(GeneralTools.round(managefee, 2), 0, 2)+"]");//��ע
					bizObjectet.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 2000));//����ʱ��
					bizObjectet.setAttributeValue("TRANSDATE", sInputDate);//��������
					bizObjectet.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					bizObjectet.setAttributeValue("STATUS", "10");//����״̬
					bizObjectet.setAttributeValue("HANDLCHARGE", "0");//������
					bizObjectet.setAttributeValue("ReBillNo",billSerialno);
					bizObjectet.setAttributeValue("ACTUALPAYFEEAMT1",managefee);//������
					m.saveObject(bizObjectet);
				}
				
			}
			
			double balancePlant = Double.parseDouble(porgMap.get("BALANCE").toString());
			balancePlant = GeneralTools.round(balancePlant-plantmange-plantfee, 2);
			if(plantmange+plantfee>0){
				//ƽ̨
				if(plantmange>0){
					BizObject bizObjecto = m.newObject();
					bizObjecto.setAttributeValue("USERID", porgMap.get("ORGID"));//�û���� Ͷ����
					bizObjecto.setAttributeValue("AMOUNT",plantmange);//���׽��
					bizObjecto.setAttributeValue("ACTUALAMOUNT",plantmange);//���׽��
					bizObjecto.setAttributeValue("TRANSTYPE", "1090");//��������
					bizObjecto.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					bizObjecto.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					bizObjecto.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					bizObjecto.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					bizObjecto.setAttributeValue("BALANCE",balancePlant+plantmange);// ���
					bizObjecto.setAttributeValue("RELAACCOUNT",porgMap.get("RELAACCOUNT"));//�����˻���ˮ��
					bizObjecto.setAttributeValue("RELAACCOUNTTYPE","002");//���׹����˻����� ���û��˻�/�����˻���
					bizObjecto.setAttributeValue("REMARK","ƽ̨��������:�����["+GeneralTools.numberFormat(GeneralTools.round(plantmange,2), 0, 2)+"]");//��ע
					bizObjecto.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 1000));//����ʱ��
					bizObjecto.setAttributeValue("TRANSDATE", sInputDate);//��������
					bizObjecto.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					bizObjecto.setAttributeValue("STATUS", "10");//����״̬
					bizObjecto.setAttributeValue("HANDLCHARGE", "0");//������
					bizObjecto.setAttributeValue("ReBillNo",billSerialno);
					bizObjecto.setAttributeValue("ACTUALPLANTMANAGE",plantmange);//ƽ̨�����
					m.saveObject(bizObjecto);
				}
				if(plantfee>0){
					BizObject bizObjecto = m.newObject();
					bizObjecto.setAttributeValue("USERID", porgMap.get("ORGID"));//�û���� Ͷ����
					bizObjecto.setAttributeValue("AMOUNT",plantfee);//���׽��
					bizObjecto.setAttributeValue("ACTUALAMOUNT",plantfee);//���׽��
					bizObjecto.setAttributeValue("TRANSTYPE", "1090");//��������
					bizObjecto.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
					bizObjecto.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
					bizObjecto.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
					bizObjecto.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
					bizObjecto.setAttributeValue("BALANCE",balancePlant+plantmange+plantfee);// ���
					bizObjecto.setAttributeValue("RELAACCOUNT",porgMap.get("RELAACCOUNT"));//�����˻���ˮ��
					bizObjecto.setAttributeValue("RELAACCOUNTTYPE","002");//���׹����˻����� ���û��˻�/�����˻���
					bizObjecto.setAttributeValue("REMARK","ƽ̨��������:�����["+GeneralTools.numberFormat(GeneralTools.round(plantfee, 2), 0, 2)+"]");//��ע
					bizObjecto.setAttributeValue("UPDATETIME",addDateSeconds(StringFunction.getToday(), StringFunction.getNow(), 2000));//����ʱ��
					bizObjecto.setAttributeValue("TRANSDATE", sInputDate);//��������
					bizObjecto.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
					bizObjecto.setAttributeValue("STATUS", "10");//����״̬
					bizObjecto.setAttributeValue("HANDLCHARGE", "0");//������
					bizObjecto.setAttributeValue("ReBillNo",billSerialno);
					bizObjecto.setAttributeValue("ACTUALPAYFEEAMT2",plantfee);//ƽ̨�����
					m.saveObject(bizObjecto);
				}
				
			}
			/***
			if(thaw_amount>0){
				//�����ֵ
				BizObject bizObjecto = m.newObject();
				bizObjecto.setAttributeValue("USERID", sUserID);//�û���� Ͷ����
				bizObjecto.setAttributeValue("AMOUNT",thaw_amount);//���׽��
				bizObjecto.setAttributeValue("ACTUALAMOUNT",thaw_amount);//���׽��
				bizObjecto.setAttributeValue("TRANSTYPE", "1010");//��������
				bizObjecto.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
				bizObjecto.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
				bizObjecto.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
				bizObjecto.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
				bizObjecto.setAttributeValue("BALANCE",userMap.get("BALANCE"));// ���
				bizObjecto.setAttributeValue("RELAACCOUNT",userMap.get("RELAACCOUNT"));//�����˻���ˮ��
				bizObjecto.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
				bizObjecto.setAttributeValue("REMARK","�Զ���ֵ");//��ע
				bizObjecto.setAttributeValue("UPDATETIME",sInputDate+" "+sInputTime);//����ʱ��
				bizObjecto.setAttributeValue("TRANSDATE", sInputDate);//��������
				bizObjecto.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
				bizObjecto.setAttributeValue("STATUS", "10");//����״̬
				bizObjecto.setAttributeValue("HANDLCHARGE", "0");//������
				bizObjecto.setAttributeValue("ReBillNo",billSerialno);
				m.saveObject(bizObjecto);
			}
			***/
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
	}
	
	/**
	 * @param string
	 * @return
	 * @throws HandlerException 
	 */
	private Map<String, Object> getmap(JBOFactory jbo,Map<String, Object> map) throws HandlerException {
		Map<String, Object> resultmap = new HashMap<String, Object>();
		try {
			String userid =(String) map.get("userid");
			String tuserid = (String) map.get("tuserid") ;
			
			BizObjectManager  manager = jbo.getManager("jbo.trade.account_info",tx);
			BizObjectQuery query = manager.createQuery(" select USERID,SERIALNO ,ACCOUNTTYPE  ,  tua.LOCKFLAG  ,tua.USABLEBALANCE  , tua.FROZENBALANCE   from o, jbo.trade.user_account tua    where   userid = tua.userid and userid in (:userid,:tuserid)");
			query.setParameter("userid", (String) map.get("userid"));
			query.setParameter("tuserid",(String) map.get("tuserid"));
			List<BizObject> bizObjectlist =query.getResultList(false);
			if (bizObjectlist!=null) {
					for (int i = 0; i < bizObjectlist.size(); i++) {
						BizObject oo = bizObjectlist.get(i);
						Map<String, Object> userMap = new HashMap<String, Object>();
						if (oo!=null) {
							String suerid = oo.getAttribute("USERID").getValue()==null?"":oo.getAttribute("USERID").getString();
							if (userid.equals(suerid)) {
								double Usablebalance =  Double.parseDouble(oo.getAttribute("USABLEBALANCE").getValue()==null?"0":oo.getAttribute("USABLEBALANCE").getString()) ;
								double Frozenbalance =  Double.parseDouble(oo.getAttribute("FROZENBALANCE").getValue()==null?"0":oo.getAttribute("FROZENBALANCE").getString()) ;
								userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
								userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getValue()==null?"":oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
								userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getValue()==null?"":oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
								resultmap.put("userid", userMap);
							} else if (tuserid.equals(suerid)) {//Ͷ����
								double Usablebalance =  oo.getAttribute("USABLEBALANCE").getDouble() ;
								double Frozenbalance =  oo.getAttribute("FROZENBALANCE").getDouble() ;
								userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
								userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
								userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
								resultmap.put("tuserid", userMap);
							}
					}
				}
			}else {
				throw new HandlerException("account.notexist.error");//δ�ҵ����û��󶨵��˻�
			}
			
				//�����˻�
			manager = jbo.getManager("jbo.trade.org_account_info", tx);
			BizObjectQuery bObjectQuery = manager.createQuery(" serialno in (:puseridserialno,:duseridserialno)");
			bObjectQuery.setParameter("puseridserialno",(String) map.get("puseridserialno"));//ƽ̨�˻�orgid
			bObjectQuery.setParameter("duseridserialno",(String) map.get("duseridserialno"));//������˾�˻�orgid
			List<BizObject> listorgid = bObjectQuery.getResultList(true);

			if (listorgid != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < listorgid.size(); i++) {
					BizObject oo = listorgid.get(i);
					Double usablebalance = Double.parseDouble(oo.getAttribute("USABLEBALANCE").getValue()==null?"0":oo.getAttribute("USABLEBALANCE").getString());
					String sAccountType = oo.getAttribute("AccountType").getString();
					if ("0103".equals(sAccountType)) {
						//ƽ̨�˻�   ƽ̨�����
						Map<String, Object> userMap = new HashMap<String, Object>();
						Double Usablebalance =  oo.getAttribute("USABLEBALANCE").getDouble() ;
						Double Frozenbalance =  oo.getAttribute("FROZENBALANCE").getDouble() ;
						userMap.put("ORGID",oo.getAttribute("ORGID").getValue()==null?"":oo.getAttribute("ORGID").getString());// ����
						userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
						userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getValue()==null?"":oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
						userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getValue()==null?"":oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
						userMap.put("payamt",plantmange+plantfee);//���׽��
						resultmap.put("porgMap", userMap);
					}else if ("0202".equals(sAccountType)) {
						//������˾�˻�   ������
						Map<String, Object> userMap = new HashMap<String, Object>();
						Double Usablebalance =  oo.getAttribute("USABLEBALANCE").getDouble() ;
						Double Frozenbalance =  oo.getAttribute("FROZENBALANCE").getDouble() ;
						userMap.put("ORGID",oo.getAttribute("ORGID").getValue()==null?"":oo.getAttribute("ORGID").getString());// ����
						userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
						userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getValue()==null?"":oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
						userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getValue()==null?"":oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
						userMap.put("payamt",insuremanagement_fee+managefee);//���׽�� ���������    ������
						resultmap.put("dorgMap", userMap);
					}				
				}
			}else {
				throw new HandlerException("account.notexist.error");//δ�ҵ����û��󶨵��˻�
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}catch (HandlerException e) {
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
		return resultmap;
	}
	
	
	
	private double tUsablebalance(JBOFactory jbo,String userid) throws HandlerException {
		BizObjectManager manager;
		double tusablebalance =  0;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid=:userid ");
			query.setParameter("userid", userid);
			BizObject bizObject = query.getSingleResult(false);
			if (bizObject!=null) {
				tusablebalance = bizObject.getAttribute("USABLEBALANCE").getValue() == null ? 0 : bizObject.getAttribute("USABLEBALANCE").getDouble();// �����˻��������
			}else {
				throw new HandlerException("common.usernotexist");
			}
			return tusablebalance;
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}
	
	private void initTransactionRecord(JSONObject request,JBOFactory jbo) throws HandlerException{
		try{
		    Map<String ,Object> smap = getmap(jbo,map);
		    Map<String, Object> userMap = (Map<String, Object>) smap.get("userid");
			BizObjectManager m = jbo.getManager("jbo.trade.transaction_record",tx);
			BizObject oo = m.newObject();
			oo.setAttributeValue("USERID", map.get("userid"));//�û���� �����
			oo.setAttributeValue("AMOUNT",amtDouble);//���׽��
			oo.setAttributeValue("ACTUALAMOUNT",amtDouble);//���׽��
			oo.setAttributeValue("TRANSTYPE", "3".equals(paytypeString)?"1040":"1030");//��������
			oo.setAttributeValue("TRANSACTIONSERIALNO",  loanSerialnoString+"@"+this.transserialno);//���������ˮ��
			oo.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
			oo.setAttributeValue("DIRECTION", "P");// ��������  p֧����r����
			oo.setAttributeValue("BALANCE",userMap.get("BALANCE"));// ���
			oo.setAttributeValue("RELAACCOUNT",userMap.get("RELAACCOUNT"));//�����˻���ˮ��
			oo.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
			oo.setAttributeValue("REMARK","3".equals(paytypeString)?"��ǰ����":"�ֶ�����");//��ע
			oo.setAttributeValue("INPUTTIME", StringFunction.getTodayNow());//��������
			oo.setAttributeValue("UPDATETIME",StringFunction.getTodayNow());//����ʱ��
			oo.setAttributeValue("TRANSDATE", StringFunction.getToday());//��������
			oo.setAttributeValue("TRANSTIME", StringFunction.getNow());//����ʱ��
			oo.setAttributeValue("STATUS", "10");//����״̬
			oo.setAttributeValue("HANDLCHARGE", "0");//������
			oo.setAttributeValue("ReBillNo",billSerialno);
			oo.setAttributeValue("ACTUALEXPIATIONSUM",penal_value);//����ΥԼ��
			oo.setAttributeValue("ACTUALPAYCORPUSAMT",actualPayCorpusAmt);//����
			oo.setAttributeValue("ACTUALPAYINTEAMT",actualPayInteAmt);//��Ϣ
			oo.setAttributeValue("ACTUALFINEAMT",actualPayFineAmt);//��Ϣ
			oo.setAttributeValue("ACTUALCOMPDINTEAMT",actualPayCompoundInte);//����
			oo.setAttributeValue("ACTUALGUARANTEEDMANAGE",insuremanagement_fee);//���������
			oo.setAttributeValue("ACTUALPAYFEEAMT1",managefee);//������
			oo.setAttributeValue("ACTUALPLANTMANAGE",plantmange);//ƽ̨�����
            oo.setAttributeValue("ACTUALPAYFEEAMT2",plantfee);//ƽ̨�����
			m.saveObject(oo);
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		
	}
	
	private  static String addDateSeconds(String sDate,String sTime,int s){
		Calendar calendar = new GregorianCalendar();
		String[] sTimes = sTime.split(":");
		String[] sDates = sDate.split("/");
		int year = Integer.parseInt(sDates[0]);
		int month = Integer.parseInt(sDates[1]);
		int day = Integer.parseInt(sDates[2]);
		calendar.set(year, month-1, day, Integer.parseInt(sTimes[0]), Integer.parseInt(sTimes[1]),
				Integer.parseInt(sTimes[2]));
		calendar.add(Calendar.MILLISECOND, s);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		return sdf.format(calendar.getTime());
	}
}
