package com.amarsoft.p2ptrade.transaction;

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
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/*
 * @DrawDown  �ſ�
 * 
 */
public class DrawdownTransactionHandler extends TradeHandler {

	private String tuserid;
	private String userid;
	private double tamtString; // Ͷ����Ͷ�ʽ��
	private String proserialno; // ��Ŀ���
	private String loanSerialnoString; // ��Ŀ���
	private String  sContractid;
	private String Month;
	private String Day;
	private double amt;
	private Map<String, Object> map = new HashMap<String, Object>();
	private String pName;
	private String geeName;
	private String userPhoneNo;
	private String tuserPhoneNo;

	@Override
	protected Object requestObject(JSONObject request, JBOFactory jbo) throws HandlerException {
		if(request.get("Serialno")==null||request.get("TUserID")==null||request.get("TAmt")==null){
			throw new HandlerException("request.invalid");
		}
		 
		proserialno = (String)request.get("Serialno");
		tuserid = (String)request.get("TUserID");
		sContractid = getContractid(jbo);
		userid = getuserid(jbo);
		tamtString = Double.parseDouble((String)request.get("TAmt")==null?"0":(String)request.get("TAmt"));// Ͷ����Ͷ�ʽ��
		try {
			
			if(userid==null||userid.length()==0){
				throw new HandlerException("common.usernotexist");//δ�ҵ��û�
			}
			//�û�״̬�ж�  account_freeze
			GeneralTools.userAccountStatus(userid, tuserid) ;//�û�״̬�쳣
			//���У��,�����а󶨵Ŀ�����
			if(checkAccountStatus(userid,jbo)==false){
				throw new HandlerException("borrownobindcard.error");//δ�󶨿�
			}
			
			//�ֻ���
			userPhoneNo = getPhoneTel(userid, jbo);
			tuserPhoneNo =  getPhoneTel(tuserid, jbo);
			if(userPhoneNo.length()==0||tuserPhoneNo.length()==0){
			  throw new HandlerException("common.emptymobile");//δ�󶨿�
			}
				
			String lockflagString= "1";
			// ������Ŀ
			frozenproject(lockflagString,jbo);
			// �� �˻�
			frozenaccoutn(lockflagString,jbo);
			
			map = getamountanMap(jbo);// ��Ŀ�� ״̬
			
			// ��ѯͶ�����˻����ý�� ���
			double usbblance = tUsablebalance(jbo);// Ͷ�����˻����ý��  
			if (usbblance < tamtString||tamtString<=0) {
				throw new HandlerException("tusaamtnoenough.error");
			}
			// ��Ŀ״̬��֤
			String status = (String) map.get("Status");
			if (!"1".equals(status)) {   // ״̬     1 ���ϼܴ�Ͷ��  2 ���¼�   
				throw new HandlerException("projectend.error");
			}
			
				// ��Ŀ����
				Updateproloanamt(jbo);
				//Ͷ���˽���
				tfrozenbanlance(jbo);
				// ����˽���
				frozenbanlance(jbo);
				
				//tfrozen(jbo);
				
				lockflagString= "2";
				// ���� ��Ŀ
				unfrozenproject(lockflagString,jbo);
				// ���� �˻�
				unfrozenaccoutn(lockflagString,jbo);
				
				String[] args =  getSereialno().split("@",-1);
				pName = args[0];
				geeName = args[1];
				// ���º�ͬ��Ϣ
				UserContract(request, jbo);
				//���뽻�׼�¼
				insertTraction(getMapName(jbo),tamtString,this.transserialno, jbo);
				
				request.put("ContractSerialNo", sContractid);
				request.put("Amt",  GeneralTools.numberFormat(tamtString,0,2));
				request.put("Method", "DrawDown");
				request.put("UserID", userid);
				request.put("detailList", setInvestUser(jbo));
				
//				try{
//					tx.commit();
//				}catch(Exception e){
//					tx.rollback();
//					e.printStackTrace();
//				throw new HandlerException("transrun.err");
//				}
//			
			
		} catch (HandlerException e) {
			e.printStackTrace();
			try {
				tx.rollback();  
				e.printStackTrace();
			} catch (JBOException e1) {
			}
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			try {
				tx.rollback();
			} catch (Exception e1) {
			}
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return request;
	}

	/*
	 * ���ر�����װ
	 */

	@Override
	protected Object responseObject(JSONObject request, JSONObject response,String logid, String transserialno, JBOFactory jbo)
			throws HandlerException {
		try {
			
			String NextPaydate = (String) response.get("NextPaydate");
			amt = Double.parseDouble((String) response.get("amt")==null?"0":(String) response.get("amt"));
			if(NextPaydate.length()>0&&NextPaydate.length()==10){
				String[] split = NextPaydate.split("/",-1);
				Month = split[1];
				Day = split[2];
				
				if(Month.startsWith("0")){
					Month = Month.substring(1);
				}
				
				if(Day.startsWith("0")){
					Day = Day.substring(1);
				}
				
			}
			
			try{
				senmsg(jbo);
			}catch(Exception e){
				e.printStackTrace();
				ARE.getLog().info("���ŷ���ʧ�ܽ���ˣ���ͬ�ţ�"+sContractid);
				// throw new HandlerException("smsreminder.error");
			}
			try{
				sentmsg(jbo);
			}catch(Exception e){
				e.printStackTrace();
				ARE.getLog().info("���ŷ���ʧ��Ͷ���ˣ���ͬ�ţ�"+sContractid);
			// throw new HandlerException("smsreminder.error");
			}
			
			
		}  catch (Exception e) {
			e.printStackTrace();
			try {
				tx.rollback();  
				e.printStackTrace();
			} catch (JBOException e1) {
				e.printStackTrace();
				throw new HandlerException("transrun.err");
			}
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return response;
	}

	private Map<String, Object> getMapName( JBOFactory jbo) throws HandlerException {
		Map<String, Object> map = new HashMap<String, Object>();
		BizObjectManager manager = null;
		try {
			manager = jbo.getManager("jbo.trade.user_contract",tx);
			BizObjectQuery query = manager.createQuery("CONTRACTNO=:CONTRACTNO ");
			query.setParameter("CONTRACTNO", sContractid);
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
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("common.usernotexist");
		}
		return map;
	}
	
	/**
	 * @param map2
	 * @param tamtString2
	 * @param request
	 * @param response
	 * @param logid
	 * @param transserialno
	 * @param jbo
	 */
	/*
	 * 
	 * ���뽻�׼�¼
	 * */
	private  void  insertTraction(Map<String, Object> map,Double payamt,String transserialno,JBOFactory jbo) throws HandlerException {
		loanSerialnoString  = sContractid;
		
		Map<String, Object> tuserMap = new HashMap<String, Object>();
		Map<String, Object> userMap = new HashMap<String, Object>();
		Map<String ,Object> smap = getmap(jbo,map);
		tuserMap = (Map<String, Object>) smap.get("tuserid");
		userMap = (Map<String, Object>) smap.get("userid");
		BizObjectManager m =null;
		String sInputDate =GeneralTools.getDate();
		String sInputTime =GeneralTools.getTime();
		try {
			 m = jbo.getManager("jbo.trade.transaction_record",tx);
			BizObject o = m.newObject();
			o.setAttributeValue("USERID", tuserid);//�û���� Ͷ����
			o.setAttributeValue("AMOUNT",payamt);//���׽��
			o.setAttributeValue("ACTUALAMOUNT",payamt);//���׽��
			o.setAttributeValue("TRANSTYPE", "1061");//��������
			o.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
			o.setAttributeValue("TRANSACTIONSERIALNO", loanSerialnoString+"@"+this.transserialno);//���������ˮ��
			o.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
			o.setAttributeValue("DIRECTION", "P");// ��������  p֧����r����
			o.setAttributeValue("BALANCE",tuserMap.get("BALANCE"));// ���
			o.setAttributeValue("RELAACCOUNT",tuserMap.get("RELAACCOUNT"));//�����˻���ˮ��
			o.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
			o.setAttributeValue("REMARK","Ͷ��");//��ע
			o.setAttributeValue("UPDATETIME",sInputDate+" "+sInputTime);//����ʱ��
			o.setAttributeValue("TRANSDATE", sInputDate);//��������
			o.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
			o.setAttributeValue("STATUS", "10");//����״̬
			o.setAttributeValue("HANDLCHARGE", "0");//������
			m.saveObject(o);
			
			BizObject oo = m.newObject();
			oo.setAttributeValue("USERID", userid);//�û���� �����
			oo.setAttributeValue("AMOUNT",payamt);//���׽��
			oo.setAttributeValue("ACTUALAMOUNT",payamt);//���׽��
			oo.setAttributeValue("TRANSTYPE", "1060");//��������
			oo.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
			oo.setAttributeValue("TRANSACTIONSERIALNO",  loanSerialnoString+"@"+this.transserialno);//���������ˮ��
			oo.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
			oo.setAttributeValue("DIRECTION", "R");// ��������  p֧����r����
			oo.setAttributeValue("BALANCE",userMap.get("BALANCE"));// ���
			oo.setAttributeValue("RELAACCOUNT",userMap.get("RELAACCOUNT"));//�����˻���ˮ��
			oo.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
			oo.setAttributeValue("REMARK","���ſ�");//��ע
			oo.setAttributeValue("UPDATETIME",sInputDate+" "+sInputTime);//����ʱ��
			oo.setAttributeValue("TRANSDATE", sInputDate);//��������
			oo.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
			oo.setAttributeValue("STATUS", "10");//����״̬
			oo.setAttributeValue("HANDLCHARGE", "0");//������
			m.saveObject(oo);
			
			BizObject ooo = m.newObject();
			ooo.setAttributeValue("USERID", userid);//�û���� �����
			ooo.setAttributeValue("AMOUNT",payamt);//���׽��
			ooo.setAttributeValue("ACTUALAMOUNT",payamt);//���׽��
			ooo.setAttributeValue("TRANSTYPE", "1020");//��������
			ooo.setAttributeValue("INPUTTIME", sInputDate+" "+sInputTime);//��������
			ooo.setAttributeValue("TRANSACTIONSERIALNO",  loanSerialnoString+"@"+this.transserialno);//���������ˮ��
			ooo.setAttributeValue("TRANSLOGID", this.logidString);//������־��logid
			ooo.setAttributeValue("DIRECTION", "P");// ��������  p֧����r����
			ooo.setAttributeValue("BALANCE",userMap.get("BALANCE"));// ���
			ooo.setAttributeValue("RELAACCOUNT",userMap.get("RELAACCOUNT"));//�����˻���ˮ��
			ooo.setAttributeValue("RELAACCOUNTTYPE","001");//���׹����˻����� ���û��˻�/�����˻���
			ooo.setAttributeValue("REMARK","�ſ��Զ�����");//��ע
			ooo.setAttributeValue("Transchannel","1010");
			ooo.setAttributeValue("UPDATETIME",sInputDate+" "+sInputTime);//����ʱ��
			ooo.setAttributeValue("TRANSDATE", sInputDate);//��������
			ooo.setAttributeValue("TRANSTIME", sInputTime);//����ʱ��
			ooo.setAttributeValue("STATUS", "01");//����״̬
			ooo.setAttributeValue("HANDLCHARGE", "0");//������
			m.saveObject(ooo);
			
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
			String tuserid = (String) map.get("tuserid");
			BizObjectManager  manager = jbo.getManager("jbo.trade.account_info",tx);
			BizObjectQuery query = manager.createQuery(" select USERID,SERIALNO ,ACCOUNTTYPE  ,  tua.LOCKFLAG  ,tua.USABLEBALANCE  , tua.FROZENBALANCE   from o, jbo.trade.user_account tua    where   userid = tua.userid and userid in (:userid,:tuserid) and status='2'");
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
								double Usablebalance =  oo.getAttribute("USABLEBALANCE").getDouble() ;
								double Frozenbalance =  oo.getAttribute("FROZENBALANCE").getDouble() ;
								userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
								userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getValue()==null?"":oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
								userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getValue()==null?"":oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
								resultmap.put("userid", userMap);
							} else if (tuserid.equals(suerid)) {
								double Usablebalance =  oo.getAttribute("USABLEBALANCE").getDouble() ;
								double Frozenbalance =  oo.getAttribute("FROZENBALANCE").getDouble() ;
								userMap.put("BALANCE",Usablebalance+Frozenbalance);// ���
								userMap.put("RELAACCOUNT",oo.getAttribute("SERIALNO").getValue()==null?"":oo.getAttribute("SERIALNO").getString());//�����˻���ˮ��
								userMap.put("RELAACCOUNTTYPE",oo.getAttribute("ACCOUNTTYPE").getValue()==null?"":oo.getAttribute("ACCOUNTTYPE").getString());//���׹����˻����� ���û��˻�/�����˻���
								resultmap.put("tuserid", userMap);
							}
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
			throw new HandlerException("account.notexist.error");//δ�ҵ����û��󶨵��˻�
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
		return resultmap;
	}
	/**
	 * @param jbo
	 * @param request
	 * @param tx
	 * @throws HandlerException
	 */
	private void frozenproject(String lockflagString ,JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno and status='1' ");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo != null) {
				// ������Ŀ
				
				//ȡ��ʼ����ʱ��Ƚ��Ƿ���Խ���Ͷ��
				String sEndDate = bo.getAttribute("EndDate").getValue()==null?"":bo.getAttribute("EndDate").getString();
				String sEndTime = bo.getAttribute("EndTime").getValue()==null?"":bo.getAttribute("EndTime").getString();
				String sBeginDate = bo.getAttribute("BeginDate").getValue()==null?"":bo.getAttribute("BeginDate").getString();
				String sBeginTime = bo.getAttribute("BeginTime").getValue()==null?"":bo.getAttribute("BeginTime").getString();
				//��ǰʱ��
				Calendar calendar = getCalendar(StringFunction.getToday(),StringFunction.getNow());
				//��ʼʱ��
				Calendar calendar1 =  getCalendar(sBeginDate,sBeginTime);
				//�ȵ�ǰʱ�����Ͷ��
				if(calendar.getTimeInMillis() < calendar1.getTimeInMillis()){
					throw new HandlerException("timeunlate.error");
				}
				if("".equals(sEndDate)||null==sEndDate||sEndDate.length()==0||"".equals(sEndTime)||null==sEndTime||sEndTime.length()==0){
					
				}else{
					//����ʱ��
					//�ȵ�ǰʱ��С����Ͷ��
					Calendar calendar2 =  getCalendar(sEndDate,sEndTime);
					if(calendar.getTimeInMillis() > calendar2.getTimeInMillis()){
						throw new HandlerException("timelate.error");
					}
				}
				 bo.setAttributeValue("LOCKFLAG", lockflagString);
				manager.saveObject(bo);
			} else {
				throw new HandlerException("common.projectnotexist");
			}

		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}

	//ʱ��
	private Calendar getCalendar(String sDate,String sTime){
		Calendar calendar = new GregorianCalendar();
		String[] sTimes = sTime.split(":");
		String[] sDates = sDate.split("/");
		int year = Integer.parseInt(sDates[0]);
		int month = Integer.parseInt(sDates[1]);
		int day = Integer.parseInt(sDates[2]);
		calendar.set(year, month, day, Integer.parseInt(sTimes[0]), Integer.parseInt(sTimes[1]),
				Integer.parseInt(sTimes[2]));
		return calendar;
	}
	
	
	/**
	 * ������Ŀ
	 * 
	 * @param jbo
	 * @param tx
	 * @throws HandlerException
	 */
	private void unfrozenproject(String lockflagString,JBOFactory jbo) throws HandlerException {

		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo!=null) {
				// ������Ŀ
				bo.setAttributeValue("LOCKFLAG", lockflagString);
				manager.saveObject(bo);
			}else {
				throw new HandlerException("common.projectnotexistt");
			}
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

	/**
	 * @param jbo
	 * @param request
	 * @return * Ͷ�����˻� ���ý��
	 */

	private double tUsablebalance(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		double tusablebalance =  0;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid=:userid ");
			query.setParameter("userid", tuserid);
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

	/**
	 * ��Ŀ���� �ʽ� ״̬ ���
	 * 
	 * @param tlDouble
	 *            Ͷ��ʣ����
	 * @param jbo
	 * @throws JBOException
	 */
	private void Updateproloanamt(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		Double sLoanamount = (Double) map.get("Loanamount"); // ��Ŀ���
		Double tlDouble = sLoanamount - tamtString;// ��Ͷ��ʣ����Ŀ���
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo!=null) {
				if (tlDouble > 0) {
					// ������Ŀ��Ͷ�ʽ��
					bo.setAttributeValue("LOANAMOUNT", tlDouble);
					bo.setAttributeValue("OPERATETIME",StringFunction.getNow());
				} else  if (GeneralTools.round(tlDouble, 2) == 0) {
					// ��Ŀ�¼�
					bo.setAttributeValue("STATUS", "3"); // ��Ŀ״̬ �¼�
					bo.setAttributeValue("ENDDATE", StringFunction.getToday());
					bo.setAttributeValue("ENDTIME", StringFunction.getNow());
					bo.setAttributeValue("ENDUSER", "system");
					bo.setAttributeValue("ENDREASON", " ����Ŀû�п�Ͷ���,Ͷ�ʳɹ�");
					bo.setAttributeValue("DEALTIME", StringFunction.getToday() + " "+ StringFunction.getNow());
					bo.setAttributeValue("OPERATETIME", StringFunction.getToday() + " "+ StringFunction.getNow());
				}
		    	manager.saveObject(bo);
			}else {
				throw new HandlerException("common.projectnotexist");
			}
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

	/*
	 * Ͷ�����˻� ������
	 */
	private void tfrozenbanlance(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid=:userid");
			query.setParameter("userid", tuserid);
			BizObject bizObject = query.getSingleResult(true);
			if (bizObject!=null) {
				double tusablebalance = Double.parseDouble(bizObject.getAttribute(
						"USABLEBALANCE").getValue()==null?"0":bizObject.getAttribute("USABLEBALANCE").getString());// �����˻��������
				//double frozenbalance =Double.parseDouble(bizObject .getAttribute("frozenbalance").getValue()==null?"0":bizObject.getAttribute("frozenbalance").getString());//�����˻��������
				double tusaamt = tusablebalance - tamtString;// ���� Ͷ���˿��ý��
				bizObject.setAttributeValue("USABLEBALANCE", GeneralTools.numberFormat(GeneralTools.round(tusaamt,2), 0, 2));
				//bizObject.setAttributeValue("frozenbalance", GeneralTools.numberFormat(GeneralTools.round(frozenbalance+tusaamt,2), 0, 2));
				manager.saveObject(bizObject);
			}else {
				throw new HandlerException("common.usernotexist");
			}
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


	// ���� Ͷ���˽���� account
	private void frozenaccoutn(String lockflagString,JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager
					.createQuery("userid  in ( :tuserid, :userid)");
			query.setParameter("tuserid", tuserid);
			query.setParameter("userid", userid);
			List<BizObject> list = query.getResultList(true);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					if (o!=null) {
						o.setAttributeValue("LOCKFLAG", lockflagString);
						manager.saveObject(o);
					}else {
						throw new HandlerException("common.usernotexist");
					}
				}
			}
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("common.usernotexist");//δ�ҵ����û��󶨵��˻�
		}
	}

	// ���� Ͷ���˽���� account
	private void unfrozenaccoutn(String lockflagString,JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager
					.createQuery("userid  in ( :tuserid, :userid)");
			query.setParameter("tuserid", tuserid);
			query.setParameter("userid", userid);

			List<BizObject> list = query.getResultList(true);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					if (o!=null) {
						o.setAttributeValue("LOCKFLAG",lockflagString);
						manager.saveObject(o);
					}else {
						throw new HandlerException("common.usernotexist");
					}
				}
			}
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

	/*
	 * ������˻�������
	 */

	private void frozenbanlance(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid =:userid");
			query.setParameter("userid", userid);

			BizObject bizObject = query.getSingleResult(true); // ����˶���
			if (bizObject!=null) {
				double frozenbalance = Double.parseDouble(bizObject.getAttribute(
					"FROZENBALANCE").getValue()==null?"0":bizObject.getAttribute("FROZENBALANCE").getString());// �����˻�������

				double frozenamt = frozenbalance + tamtString; // ����� ������ ����
			bizObject.setAttributeValue("FROZENBALANCE", GeneralTools.round(frozenamt, 2));
			manager.saveObject(bizObject);
			}else {
				throw new HandlerException("common.usernotexist");
			}
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

	/*
	 * ��Ŀ��� ״̬ ��֤
	 */
	private Map<String, Object> getamountanMap(JBOFactory jbo) throws HandlerException {
		Map<String, Object> map = new HashMap<String, Object>();
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo!=null) {
				// ��ѯ��Ŀ��Ͷ�ʽ�� ״̬
				Double sLoanamount = Double.parseDouble(bo.getAttribute(
						"LOANAMOUNT").getValue()==null?"0":bo.getAttribute("LOANAMOUNT").getString());
				String sStatus = bo.getAttribute("STATUS").getValue()==null?"":bo.getAttribute("STATUS").getString();
				map.put("Loanamount", sLoanamount);
				map.put("Status", sStatus);
			}else {
				throw new HandlerException("common.projectnotexist");
			}
			return map;
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("common.projectnotexist");//δ�ҵ����û��󶨵��˻�
		}
	}

	//У���ʻ��Ƿ�����
	private boolean checkAccountStatus(String userid,JBOFactory jbo) throws HandlerException {
		Map<String, Object> map = new HashMap<String, Object>();
		BizObjectManager manager;
		boolean isok = false;
		try {
			manager = jbo.getManager("jbo.trade.account_info", tx);
			BizObjectQuery query = manager.createQuery("userid =:userid and status='2'");
			query.setParameter("userid", userid);
			List<BizObject> list = query.getResultList(false);
			if (list!=null) {
				int n=0;
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					String suserid = o.getAttribute("userid").getString();
					if (userid.equals(suserid)) {
						n ++;//�����
					} else{
						isok = false;
					}
				}
				if(n<=0||n>1) {
					isok = false;
				}else{
					isok = true;
				}
			}else {
				isok = false;
			}
			return isok;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}
	
	/*
	 * ��ȡ��ͬ��
	 */
	private String getContractid(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		String sContractid = "";
		try {
			manager = jbo.getManager("jbo.trade.project_info");
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(false);
			if (bo!=null) {
				sContractid = bo.getAttribute("CONTRACTID").getValue()==null?"":bo.getAttribute("CONTRACTID").getString();
			}else {
				throw new HandlerException("common.projectnotexist");
			}
			return sContractid;
		} catch (HandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}
	
	/*
	 * �����
	 */
	private String getuserid(JBOFactory jbo) throws HandlerException{
		BizObjectManager manager;
		String sUserID;
		try {
			manager = jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery query = manager.createQuery(" RELATIVETYPE='001' AND  CONTRACTID=:CONTRACTID");
			query.setParameter("CONTRACTID", sContractid);
			BizObject bo = query.getSingleResult(false);
			if (bo!=null) {
				sUserID = bo.getAttribute("USERID").getValue()==null?"":bo.getAttribute("USERID").getString();
			}else {
				throw new HandlerException("common.contractnotexist");
			}
			return sUserID;
		} catch (HandlerException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}

	/**
	 * @param string
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unused")
	private Map<String, Object> getmap(JBOFactory jbo, String useid) throws HandlerException {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			BizObjectManager manager = jbo
					.getManager("jbo.trade.account_info");
			BizObjectQuery query = manager
					.createQuery(" select  SERIALNO,ACCOUNTTYPE ,  tua.LOCKFLAG  ,tua.USABLEBALANCE  , tua.FROZENBALANCE   "
							+ "  from o, jbo.trade.user_account tua  where status='2' and userid = tua.userid and userid =:userid");
			query.setParameter("userid", useid);
			BizObject bizObject = query.getSingleResult(false);
			if (bizObject != null) {
					double Usablebalance = Double.parseDouble(bizObject
							.getAttribute("USABLEBALANCE").getValue()==null?"0":bizObject.getAttribute("USABLEBALANCE").getString());
					double Frozenbalance = Double.parseDouble(bizObject
							.getAttribute("FROZENBALANCE").getValue()==null?"0":bizObject.getAttribute("FROZENBALANCE").getString());
					map.put("BALANCE", GeneralTools.round(Usablebalance + Frozenbalance, 2));// ���
					map.put("RELAACCOUNT", bizObject.getAttribute("SERIALNO")
							.getValue()==null?"":bizObject.getAttribute("SERIALNO").getString());// �����˻���ˮ��
					map.put("RELAACCOUNTTYPE",
							bizObject.getAttribute("ACCOUNTTYPE").getValue()==null?"":bizObject.getAttribute("ACCOUNTTYPE").getString());// ���׹����˻�����
																				// ���û��˻�/�����˻���
			} else {
				throw new HandlerException("account.notexist.error");// δ�ҵ����û��󶨵��˻�
			}
		} catch (HandlerException e) {
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return map;
	}

	// ���º�ͬ��Ϣ
	private void UserContract(JSONObject request, JBOFactory jbo) throws HandlerException {
		try {
			// ���� ����� Ͷ����
			BizObjectManager manager = jbo.getManager("jbo.trade.user_contract",tx);
			BizObject o = manager.newObject();
			o.setAttributeValue("CONTRACTID", sContractid);
			o.setAttributeValue("CONTRACTNO", sContractid);
			o.setAttributeValue("INPUTTIME", StringFunction.getToday());
			o.setAttributeValue("UPDATETIME", StringFunction.getToday());
			o.setAttributeValue("USERID", tuserid);
			o.setAttributeValue("RELATIVETYPE", "002");// codeno='UserRelativeType'
			manager.saveObject(o);
			
			 manager = jbo.getManager("jbo.trade.ti_contract_info",tx);
			BizObject bizObject = manager.newObject();
			bizObject.setAttributeValue("CONTRACTID", sContractid); // p2p ��ͬ�ź�
			bizObject.setAttributeValue("CONTRACTTYPE", "");// ��ͬ����
			bizObject.setAttributeValue("LOANNO", sContractid);// �Ŵ� ��ݺ�
			bizObject.setAttributeValue("CONTRACTNO", sContractid);// �Ŵ���ͬ��
			bizObject.setAttributeValue("STATUS", "100");// ��ͬ״̬	Code:ContractStatus
			bizObject.setAttributeValue("INPUTTIME",  StringFunction.getToday());
			bizObject.setAttributeValue("UPDATETIME", StringFunction.getToday());
			bizObject.setAttributeValue("GUARANTEECOMPANY", geeName);// ������˾
			manager.saveObject(bizObject);

		}  catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
	}
	
	private void tfrozen(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid  = :tuserid");
			query.setParameter("tuserid", tuserid);
			BizObject tbizObject = query.getSingleResult(true); // Ͷ���˶���
			if (tbizObject!=null) {
				Double tfrozenbalance = Double.parseDouble(tbizObject.getAttribute("FROZENBALANCE").toString());// Ͷ���������˻�������
				Double tfrozenamt = tfrozenbalance - tamtString; // Ͷ���˶����� �ⶳ
				tbizObject.setAttributeValue("FROZENBALANCE", tfrozenamt.toString());
				manager.saveObject(tbizObject);
			}else {
				throw new HandlerException("common.usernotexist");
			}
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("default.database.error");//δ�ҵ����û��󶨵��˻�
		}
	}

	
	private String getSereialno() throws HandlerException{
		String sProjectName ="";
		String sgeeName = "";
		BizObjectManager m;
		JBOFactory jbo =JBOFactory.getFactory();
		try {
			m = jbo.getManager("jbo.trade.project_info");
			BizObjectQuery query= m.createQuery(" SERIALNO=:SERIALNO");
			query.setParameter("SERIALNO",proserialno);
			BizObject o = query.getSingleResult(false);
			sProjectName=o.getAttribute("PROJECTNAME").getValue()==null?"":o.getAttribute("PROJECTNAME").getString();
			sgeeName=o.getAttribute("GRANANTEE").getValue()==null?"":o.getAttribute("GRANANTEE").getString();
		}catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
		return sProjectName+"@"+sgeeName;
	}
	
	/**
	 *  @throws HandlerException 
	 * 
	 */
	private void senmsg(JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
	// ���Ͷ�������  �����
	 GeneralTools.sendSMS("P2P_FKCG", userPhoneNo, setHashMap());
	}
	
	private void sentmsg(JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
	// ���Ͷ�������  �����
	 GeneralTools.sendSMS("P2P_TZCG", tuserPhoneNo, settHashMap());
	}
	
	private JSONArray setInvestUser(JBOFactory jbo){
		//investOrgID  investBusiessSum  loanBusiessSum
		JSONArray ar = new JSONArray();
		JSONObject J = new JSONObject();
		J.put("investOrgID", tuserid);
		J.put("investBusiessSum", tamtString);
		J.put("loanBusiessSum", tamtString);
		ar.add(J);
		return ar;
	}
	
	/**
	 * @param jbo
	 * @return Ӧ�� ʵ�� ���
	 * @throws HandlerException 
	 */
	private HashMap<String, Object> setHashMap() throws HandlerException {
		// TODO Auto-generated method stub
		HashMap<String , Object> map1   = new HashMap<String, Object>();
		map1.put("PayAmount", GeneralTools.numberFormat(GeneralTools.round(amt,2),0,2));
		map1.put("ContractNo", sContractid);
		map1.put("Month", Month);
		map1.put("Day", Day);
		return map1;
	}
	
	private HashMap<String, Object> settHashMap() throws HandlerException {
		// TODO Auto-generated method stub
		HashMap<String , Object> map1   = new HashMap<String, Object>();
		map1.put("ProjectName", pName);
		map1.put("Balance", GeneralTools.numberFormat(GeneralTools.round(tamtString,2),0,2));
		return map1;
	}
}
