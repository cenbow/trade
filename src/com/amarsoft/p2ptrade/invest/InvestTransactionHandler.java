package com.amarsoft.p2ptrade.invest;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import com.amarsoft.account.common.ObjectConstants;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.account.QueryUserAccountInfoHandler;
import com.amarsoft.p2ptrade.project.ProjectdetailHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;
import com.amarsoft.utils.jbo.JBOHelper;

/*
 * @DrawDown  Ͷ�� ����
 * 
 */
@SuppressWarnings("unchecked")
public class InvestTransactionHandler  extends JSONHandler{

	protected String investuser;//Ͷ���û�
	protected String loanuser;//����û�
	protected double tamtString; // Ͷ����Ͷ�ʽ��
	protected String proserialno; // ��Ŀ���
	protected String  sContractid;//��ͬ���
	protected double  remainamount;//��Ͷ���

	protected double tusaamt;
	protected double tlDouble;
	protected JBOTransaction tx = null;
	
	private String sSUBCONTRACTNO = "";
	@Override
	public Object createResponse(JSONObject request, Properties arg1) throws HandlerException {
		return tradeCenter(request,arg1);
	}

	protected Object tradeCenter(JSONObject request,Properties arg1) throws HandlerException {
		JSONObject  result = new JSONObject();
		
		JBOFactory jbo = JBOFactory.getFactory();
		
		try {
			//���׼��
			beforeTrans(request, jbo);			
			//Ͷ�ʽ��ײ���
			runTrans(request, jbo);
			result.put("chk", "S");				
		
		} catch (HandlerException e) {
			throw e;
		} 	catch (Exception e) {
			e.printStackTrace();
		} 				
		return result;
	}
	protected void beforeTrans(JSONObject request, JBOFactory jbo)throws HandlerException {
		if(request.get("Serialno")==null||request.get("TUserID")==null||request.get("TAmt")==null){
			throw new HandlerException("request.invalid");
		}
		proserialno = (String)request.get("Serialno");//��Ŀ���
		investuser = (String)request.get("TUserID");//Ͷ���û�
		sContractid = getContractid(jbo);//����ͬ
		loanuser = geinvestuser(jbo);//����û�
		tamtString = Double.parseDouble((String)request.get("TAmt")==null?"0":(String)request.get("TAmt"));// Ͷ����Ͷ�ʽ��
		if(loanuser==null||loanuser.length()==0){
			throw new HandlerException("common.usernotexist");//δ�ҵ�����û�
		}
		//�û�״̬�ж�  ACCOUNT_FREEZE
		GeneralTools.userAccountStatus(loanuser, investuser) ;//�û�״̬�쳣
		//���У��,�����а󶨵Ŀ�����
		if(checkAccountStatus()==false){
			throw new HandlerException("borrownobindcard.error");//δ�󶨿�
		}
		//�����Ŀ��Ͷ�ʽ���Ƿ�Ϸ�
		checkInvestSum(request,jbo);
		
	}


	protected Object runTrans(JSONObject request, JBOFactory jbo) throws HandlerException {
		
		
		try {
			tx  = jbo.createTransaction();
//			String lockflagString= "1";
//			frozenproject(lockflagString,jbo);
//			// �� �˻�
//			frozenaccoutn(lockflagString,jbo);
//			map = getamountanMap(jbo);// ��Ŀ�� ״̬
//			
//			// ��Ŀ״̬��֤
//			String status = (String) map.get("Status");
//			if (!"1".equals(status)) {   // ״̬     1 ���ϼܴ�Ͷ��  2 ���¼�   
//				throw new HandlerException("projectend.error");
//			}
			// ��Ŀ����
			Updateproloanamt(jbo);
			//Ͷ���˽���
			tfrozenbanlance(jbo);
//			lockflagString= "2";
//			// ���� ��Ŀ
//			unfrozenproject(lockflagString,jbo);
//			// ���� �˻�
//			unfrozenaccoutn(lockflagString,jbo);
//			String[] args =  getSereialno().split("@",-1);
//			pName = args[0];
//			geeName = args[1];

			// ����Ͷ�ʺ�ͬ��Ϣ
			UserContract(request, jbo);
			//�ж��Ƿ�������뷵��
			isTransfer();
			
			
			
			tx.commit();
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

	
	/**������Ŀ
	 * @param jbo
	 * @param request
	 * @param tx
	 * @throws HandlerException
	 */
	private void frozenproject(String lockflagString ,JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("update o set LOCKFLAG=:flag where serialno=:serialno and status='1' ");
			query.setParameter("serialno", proserialno);
			query.setParameter("flag", lockflagString);
			query.executeUpdate();
		}
		 catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");
		}
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
	 * ��Ŀ���� �ʽ� ״̬ ���
	 * 
	 * @param tlDouble
	 *            Ͷ��ʣ����
	 * @param jbo
	 * @throws JBOException
	 */
	private void Updateproloanamt(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;

		tlDouble = remainamount - tamtString;// ��Ͷ��ʣ����Ŀ���
		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo!=null) {
				if (tlDouble > 0) {
					// ������Ŀ��Ͷ�ʽ��
					bo.setAttributeValue("remainamount", tlDouble);
					bo.setAttributeValue("OPERATETIME",StringFunction.getNow());
				} else  if (GeneralTools.round(tlDouble, 2) == 0) {
					// ��Ŀ������
					bo.setAttributeValue("remainamount", 0);
					bo.setAttributeValue("STATUS", "104"); // ��Ŀ״̬ ������
					bo.setAttributeValue("ENDUSER", "system");
					bo.setAttributeValue("ENDREASON", " ����Ŀû�п�Ͷ���,Ͷ�ʳɹ�");
					bo.setAttributeValue("ENDDATE", StringFunction.getToday());
					bo.setAttributeValue("ENDTIME", StringFunction.getNow());
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
			query.setParameter("userid", investuser);
			BizObject bizObject = query.getSingleResult(true);
			if (bizObject!=null) {
//				ObjectBalanceUtils.freezeObjectBalance(investuser, ObjectConstants.OBJECT_TYPE_001,jboHelper, tusaamt, jsonObject.toJSONString());
			/*	double tusablebalance = Double.parseDouble(bizObject.getAttribute(
						"USABLEBALANCE").getValue()==null?"0":bizObject.getAttribute("USABLEBALANCE").getString());// �����˻��������
				double frozenbalance =Double.parseDouble(bizObject .getAttribute("frozenbalance").getValue()==null?"0":bizObject.getAttribute("frozenbalance").getString());//�����˻��������
				tusaamt = tusablebalance - tamtString;// ���� Ͷ���˿��ý��
				bizObject.setAttributeValue("USABLEBALANCE", GeneralTools.numberFormat(GeneralTools.round(tusaamt,2), 0, 2));
				bizObject.setAttributeValue("frozenbalance", GeneralTools.numberFormat(GeneralTools.round(frozenbalance+tamtString,2), 0, 2));
				manager.saveObject(bizObject);
				*/
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
					.createQuery("userid  in ( :investuser, :userid)");
			query.setParameter("investuser", investuser);
			query.setParameter("userid", loanuser);
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
					.createQuery("userid  in ( :investuser, :userid)");
			query.setParameter("investuser", investuser);
			query.setParameter("userid", loanuser);

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


	/**
	 * ��Ŀ���� �ʽ� ״̬ �ع�
	 * 
	 * @param tlDouble
	 *            Ͷ��ʣ����
	 * @param jbo
	 * @throws JBOException
	 */
	private void UpdateProject(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;

		try {
			manager = jbo.getManager("jbo.trade.project_info", tx);
			BizObjectQuery query = manager.createQuery("serialno=:serialno");
			query.setParameter("serialno", proserialno);
			BizObject bo = query.getSingleResult(true);
			if (bo!=null) {
				bo.setAttributeValue("remainamount", tamtString);
				bo.setAttributeValue("STATUS", "1"); // ��Ŀ״̬ �ָ�
				bo.setAttributeValue("ENDUSER", "system");
				bo.setAttributeValue("ENDREASON", " ���һ��Ͷ��ʧ�ܣ��ָ�");
				bo.setAttributeValue("DEALTIME", StringFunction.getToday() + " "+ StringFunction.getNow());
				bo.setAttributeValue("OPERATETIME", StringFunction.getToday() + " "+ StringFunction.getNow());
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
	 * Ͷ�����˻����ⶳ�ָ�
	 */
	private void frozenbanlance(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account", tx);
			BizObjectQuery query = manager.createQuery("userid=:userid");
			query.setParameter("userid", investuser);
			BizObject bizObject = query.getSingleResult(true);
			if (bizObject!=null) {
				double tusablebalance = Double.parseDouble(bizObject.getAttribute(
						"USABLEBALANCE").getValue()==null?"0":bizObject.getAttribute("USABLEBALANCE").getString());// �����˻��������
				double frozenbalance =Double.parseDouble(bizObject .getAttribute("frozenbalance").getValue()==null?"0":bizObject.getAttribute("frozenbalance").getString());//�����˻��������
				tusaamt = tusablebalance + tamtString;// �ָ� Ͷ���˿��ý��
				bizObject.setAttributeValue("USABLEBALANCE", GeneralTools.numberFormat(GeneralTools.round(tusaamt,2), 0, 2));
				bizObject.setAttributeValue("frozenbalance", GeneralTools.numberFormat(GeneralTools.round(frozenbalance-tamtString,2), 0, 2));
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

	
	//У���ʻ��Ƿ����� �жϽ���û��Ƿ��Ѿ��󶨻��
	private boolean checkAccountStatus() throws HandlerException {

		JBOFactory jbo = JBOFactory.getFactory();
		BizObjectManager manager;
		boolean isok = false;
		try {
			manager = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = manager.createQuery("userid =:userid and status='2' ");//and ISRETURNCARD='1'");
			query.setParameter("userid", loanuser);
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				isok = true;
			}
			return isok;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");
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
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}
	
	/*
	 * ��ȡԤǩ��ͬ��
	 */
	private String getPreContractid(JBOFactory jbo) throws HandlerException {
		BizObjectManager manager;
		String precontractno = "";
		try {
			manager = jbo.getManager("jbo.trade.loan_apply");
			BizObjectQuery query = manager.createQuery(" select o.precontractno from o,jbo.trade.business_contract bc where o.precontractno=bc.serialno and bc.serialno=:serialno");
			query.setParameter("serialno", sContractid);
			BizObject bo = query.getSingleResult(false);
			if (bo!=null) {
				precontractno = bo.getAttribute("precontractno").getValue()==null?"":bo.getAttribute("precontractno").getString();
			}else {
				throw new HandlerException("common.projectnotexist");
			}
			return precontractno;
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.error");
		}
	}
	
	/*
	 * �����
	 */
	private String geinvestuser(JBOFactory jbo) throws HandlerException{
		BizObjectManager manager;
		String sUserID;
		try {
			manager = jbo.getManager("jbo.trade.business_contract");
			BizObjectQuery query = manager.createQuery(" SERIALNO=:SERIALNO");
			query.setParameter("SERIALNO", sContractid);
			BizObject bo = query.getSingleResult(false);
			if (bo!=null) {
				sUserID = bo.getAttribute("CUSTOMERID").getValue()==null?"":bo.getAttribute("CUSTOMERID").getString();
			}else {
				throw new HandlerException("common.contractnotexist");
			}
			return sUserID;
		} catch (HandlerException e) {
			e.printStackTrace();
			throw e;
		} catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("transrun.err");//δ�ҵ����û��󶨵��˻�
		}
	}


	// ����Ͷ���˺�ͬ��Ϣ
	private void UserContract(JSONObject request, JBOFactory jbo) throws HandlerException {
		Connection conn = null;
		try {
			// ���� Ͷ���˺�ͬ
			BizObjectManager manager = jbo.getManager("jbo.trade.user_contract",tx);
			BizObjectQuery query = manager.createQuery("userid=:userid and contractid=:contractid")
			.setParameter("userid", investuser)
			.setParameter("contractid", sContractid);
			BizObject o = query.getSingleResult(true);
			if(o==null){
				o = manager.newObject();
				//SUBCONTRACTNO
				//Ԥǩ��ͬ
				conn = ARE.getDBConnection(ARE.getProperty("dbName"));
				String precontractno =  sContractid;//getPreContractid(jbo);
				sSUBCONTRACTNO = P2pString.getSerialNo("user_contract", "subcontractno", precontractno, conn);
				o.setAttributeValue("SUBCONTRACTNO", sSUBCONTRACTNO);
				o.setAttributeValue("CONTRACTID", sContractid);
				o.setAttributeValue("CONTRACTNO", sContractid);
				o.setAttributeValue("INPUTTIME", StringFunction.getToday()+" "+StringFunction.getNow());
				o.setAttributeValue("UPDATETIME", StringFunction.getToday()+" "+StringFunction.getNow());
				o.setAttributeValue("USERID", investuser);
				o.setAttributeValue("projectid", proserialno);
				o.setAttributeValue("investsum", tamtString);
				o.setAttributeValue("LastInvestSum",tamtString);
				o.setAttributeValue("status", "0");
				o.setAttributeValue("RELATIVETYPE", "002");// codeno='UserRelativeType'
				manager.saveObject(o);
				
			
				
			}else{
				double investsum = Double.parseDouble(o.getAttribute("investsum")==null?"0":o.getAttribute("investsum").toString());
				o.setAttributeValue("UPDATETIME", StringFunction.getToday()+" "+StringFunction.getNow());
				o.setAttributeValue("LastInvestSum",tamtString);
				o.setAttributeValue("investsum", (investsum + tamtString));
				manager.saveObject(o);
			}
			
			
			JBOHelper jboHelper = new JBOHelper(tx);

			JSONObject jsonObject = new JSONObject();
			String transCode = "7001";
			jsonObject.put("REMARK", "�ʽ𶳽ᴦ��");
			jsonObject.put("PROJECTNO", proserialno);
			jsonObject.put("SERIALNO", o.getAttribute("SUBCONTRACTNO").getValue());
			jsonObject.put("USERID", investuser);
			jsonObject.put("USERACCOUNTTYPE", ObjectConstants.ACCOUNT_TYPE_001);
			jsonObject.put("AMOUNT", tamtString);
			jsonObject.put("TRANSCHANNEL", "3010");
			jsonObject.put("TRANSTYPE", transCode);
			
			RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation("3010", transCode);
			rttt.init(jsonObject);
			rttt.execute();
			
			if(!rttt.getTemplet().isSuccess())
			{
				throw new HandlerException("transrun.err");
			}

			
		}  catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("transrun.error");
		}finally{
			if(conn!=null){
				try {
					conn.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	
	// ���Ͷ�ʽ���Ƿ�Ϸ�
	private void checkInvestSum(JSONObject request, JBOFactory jbo) throws HandlerException {
		request.put("UserID", investuser);
		//��ȡ��Ŀ��Ϣ
		ProjectdetailHandler pd = new ProjectdetailHandler();
		JSONObject rp  = (JSONObject)pd.createResponse(request, null);
		remainamount = Double.parseDouble(rp.get("remainamount").toString());
		//���ʱ���Ƿ���Ч
		if((rp.get("BeginDate").toString() +  " " + rp.get("BeginTime").toString()).compareToIgnoreCase(StringFunction.getTodayNow())>0){
			throw new HandlerException("invest.project.error");
		}
		//�ж���Ŀ״̬
		if(!"1".equals(rp.get("Status").toString())){
			throw new HandlerException("invest.project.error");			
		}
		//����Ƿ�ǰ�û�
		if(loanuser.equals(investuser)){
			throw new HandlerException("invest.check.sameuser");
		}
		//��ȡ��Ա��Ϣ
		QueryUserAccountInfoHandler qd = new QueryUserAccountInfoHandler();
		JSONObject rq = (JSONObject)qd.createResponse(request, null);
		//ʵ����֤
		if(!"2".equals(rq.get("UserAuthFlag"))){
			throw new HandlerException("invest.user.error");	
		}
		//��ȫ����
//		if(!"Y".equals(rq.get("SecurityQuestionFlag"))){
//			throw new HandlerException("invest.user.error");
//		}
		//��������
		if(!"Y".equals(rq.get("TransPWDFlag"))){
			throw new HandlerException("invest.user.error");	
		}
		//����ʽ��˻��Ƿ��쳣
		if("1".equals(rq.get("FrozenLockFalg"))){
			throw new HandlerException("invest.user.error");	
		}

		//�ж������Ͷ�ʽ��
		String TAmt = request.get("TAmt").toString();

		if(TAmt!=null&&TAmt.length()>0) {
			
			//Ͷ�ʽ������˻����
			if(Double.parseDouble(TAmt)>Double.parseDouble(rq.get("UsableBalance").toString())){
				throw new HandlerException("invest.sum.error");
				
			}
			
			//Ͷ�ʽ����ڿ�Ͷ���
			if(Double.parseDouble(TAmt)>remainamount){
				throw new HandlerException("invest.sum.error");
				
			}
			
			//���Ͷ��
			if(Double.parseDouble(TAmt)==remainamount){			
				
			}else{
				//��Ͷ���С����Ͷ���
				if(remainamount<Double.parseDouble(rp.get("BEGINAMOUNT").toString())){
					//����Ͷ�����
					if(Double.parseDouble(TAmt)!=remainamount){
						throw new HandlerException("invest.sum.error");
						
					}
				}else{
					//Ͷ�ʽ��С����Ͷ���
					if(Double.parseDouble(TAmt)<Double.parseDouble(rp.get("BEGINAMOUNT").toString())){
						throw new HandlerException("invest.sum.error");					
					}
					
					//Ͷ�ʽ��������� ��Ͷ�����ϵ�����
					if((Double.parseDouble(TAmt)-Double.parseDouble(rp.get("BEGINAMOUNT").toString()))%Double.parseDouble(rp.get("ADDAMOUNT").toString())==0){
						
					}else{
						throw new HandlerException("invest.sum.error");					
					}
				}
			}
			
		} else {
			throw new HandlerException("invest.sum.error");		
		}		
	}
	
	
	//�Ƿ����뷵��
	private void isTransfer() {
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager manager = jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery q0 = manager.createQuery(" select count(userid) as v.count from o where userid=:userid");
			q0.setParameter("userid", investuser);
			BizObject o0 = q0.getSingleResult(false);
			double count = 0;
			if(o0!=null){
				count = o0.getAttribute("count").getDouble();
			}
			//��һ��Ͷ��
			if(count<1){
				//����Ͷ���˲�ѯ������
				BizObjectManager userAcctManager =jbo.getManager("jbo.trade.user_account");
				BizObjectQuery qq = userAcctManager.createQuery(" select usercode from o where userid=:userid");
				qq.setParameter("userid", investuser);
				BizObject oo = qq.getSingleResult(false);
				String usercode = "";
				if(oo!=null){
					usercode = oo.getAttribute("usercode").toString();
				}
				BizObjectQuery q = userAcctManager.createQuery(" select userid from o where ( userid=:usercode or username=:usercode or phonetel=:usercode or invitecode=:usercode) and userauthflag='2'");
					q.setParameter("usercode", usercode);
				BizObject o = q.getSingleResult(false);
				if(o!=null){
					String sUserID = o.getAttribute("userid").toString();
					//�������뷵����¼
					setTransfer(sUserID, sSUBCONTRACTNO, tamtString);
				}
			}
		} catch (JBOException e) {
			e.printStackTrace();
		}
		
	}
	
	/**
	 * �������뷵����Ϣ
	 * */
	private void setTransfer(String sUserID,String investCode,double investsum){
		JBOFactory jbo = JBOFactory.getFactory();
		try{
			BizObjectManager m = jbo.getManager("jbo.trade.restore_rule");
			//��ǩ�����������ѯ��ǰ�ɷ������
			BizObjectQuery ruleq = m.createQuery(" select restoresum,restoresum2,restoretype,restoretype2 from o where rulecode='invite_invest' and minimum <=:ucsum order by minimum desc");
			ruleq.setParameter("ucsum", investsum);
			BizObject ruleo = ruleq.getSingleResult(false);
			double restoresum = 0;
			double restoresum2 = 0;
			if(ruleo!=null){
				restoresum = ruleo.getAttribute("restoresum").getDouble();
				restoresum2 = ruleo.getAttribute("restoresum2").getDouble();
				
				if(2==ruleo.getAttribute("restoretype").getInt())
					restoresum = restoresum*investsum/100;
				
				if(2==ruleo.getAttribute("restoretype2").getInt())
					restoresum2 = restoresum2*investsum/100;
			}
			
			//�Ƽ���
			if(restoresum>0){
				//���뽻�׼�¼
				BizObjectManager recordm =jbo.getManager("jbo.trade.transaction_record",tx);
				
				//���뽻�׼�¼
				BizObject recordo1 = recordm.newObject();
				recordo1.setAttributeValue("USERID", ARE.getProperty("HouBankSerialNo"));
				recordo1.setAttributeValue("relaaccount", "1");
				recordo1.setAttributeValue("DIRECTION", "P");
				recordo1.setAttributeValue("AMOUNT", restoresum);
				recordo1.setAttributeValue("BALANCE", 0);
				recordo1.setAttributeValue("TRANSTYPE", "2040");
				recordo1.setAttributeValue("TRANSDATE", StringFunction.getToday());
				recordo1.setAttributeValue("TRANSTIME", StringFunction.getNow());
				recordo1.setAttributeValue("inputTIME", StringFunction.getToday() +" "+ StringFunction.getNow());
				recordo1.setAttributeValue("STATUS", "99");
				recordo1.setAttributeValue("USERACCOUNTTYPE", "003");
				recordm.saveObject(recordo1);
				
				BizObject recordo = recordm.newObject();
				recordo.setAttributeValue("USERID", sUserID);
				recordo.setAttributeValue("relaaccount", "2");
				recordo.setAttributeValue("DIRECTION", "R");
				recordo.setAttributeValue("AMOUNT", restoresum);
				recordo.setAttributeValue("BALANCE", 0);
				recordo.setAttributeValue("TRANSTYPE", "2040");
				recordo.setAttributeValue("TRANSDATE", StringFunction.getToday());
				recordo.setAttributeValue("TRANSTIME", StringFunction.getNow());
				recordo1.setAttributeValue("inputTIME", StringFunction.getToday() +" "+ StringFunction.getNow());
				recordo.setAttributeValue("STATUS", "99");
				recordo.setAttributeValue("USERACCOUNTTYPE", "001");
				recordm.saveObject(recordo);
				
				//�������
				BizObjectManager mm =jbo.getManager("jbo.trade.acct_transfer",tx);
				
				//ƽ̨Ӫ���˻�
				BizObject transfer1 = mm.newObject();
				transfer1.setAttributeValue("objectno", investCode);
				transfer1.setAttributeValue("objecttype", "040");
				transfer1.setAttributeValue("seqid", "1");
				transfer1.setAttributeValue("userid", ARE.getProperty("HouBankSerialNo"));
				transfer1.setAttributeValue("direction", "R");
				transfer1.setAttributeValue("amount", restoresum);
				transfer1.setAttributeValue("status", "99");
				transfer1.setAttributeValue("inputdate", StringFunction.getToday());
				transfer1.setAttributeValue("inputtime", StringFunction.getNow());
				transfer1.setAttributeValue("transserialno", recordo1.getAttribute("serialno"));
				transfer1.setAttributeValue("remark", "��������Ͷ������");
				transfer1.setAttributeValue("transcode", "1001");
				transfer1.setAttributeValue("useraccounttype", "003");
				mm.saveObject(transfer1);
				
				//������
				BizObject transfer = mm.newObject();						
				transfer.setAttributeValue("objectno", investCode);
				transfer.setAttributeValue("objecttype", "040");
				transfer.setAttributeValue("seqid", "2");
				transfer.setAttributeValue("userid", sUserID);
				transfer.setAttributeValue("direction", "P");
				transfer.setAttributeValue("amount", restoresum);
				transfer.setAttributeValue("status", "99");
				transfer.setAttributeValue("inputdate", StringFunction.getToday());
				transfer.setAttributeValue("inputtime", StringFunction.getNow());
				transfer.setAttributeValue("transserialno", recordo.getAttribute("SERIALNO"));
				transfer.setAttributeValue("remark", "��������Ͷ������");
				transfer.setAttributeValue("transcode", "2001");
				transfer.setAttributeValue("useraccounttype", "001");
				mm.saveObject(transfer);
			}
			
			//Ͷ����
			if(restoresum2>0){
				//���뽻�׼�¼
				BizObjectManager recordm =jbo.getManager("jbo.trade.transaction_record",tx);
				
				//���뽻�׼�¼
				BizObject recordo1 = recordm.newObject();
				recordo1.setAttributeValue("USERID", ARE.getProperty("HouBankSerialNo"));
				recordo1.setAttributeValue("relaaccount", "1");
				recordo1.setAttributeValue("DIRECTION", "P");
				recordo1.setAttributeValue("AMOUNT", restoresum2);
				recordo1.setAttributeValue("BALANCE", 0);
				recordo1.setAttributeValue("TRANSTYPE", "2040");
				recordo1.setAttributeValue("TRANSDATE", StringFunction.getToday());
				recordo1.setAttributeValue("TRANSTIME", StringFunction.getNow());
				recordo1.setAttributeValue("inputTIME", StringFunction.getToday() +" "+ StringFunction.getNow());
				recordo1.setAttributeValue("STATUS", "99");
				recordo1.setAttributeValue("USERACCOUNTTYPE", "003");
				recordm.saveObject(recordo1);
				
				BizObject recordo = recordm.newObject();
				recordo.setAttributeValue("USERID", investuser);
				recordo.setAttributeValue("relaaccount", "3");
				recordo.setAttributeValue("DIRECTION", "R");
				recordo.setAttributeValue("AMOUNT", restoresum2);
				recordo.setAttributeValue("BALANCE", 0);
				recordo.setAttributeValue("TRANSTYPE", "2040");
				recordo.setAttributeValue("TRANSDATE", StringFunction.getToday());
				recordo.setAttributeValue("TRANSTIME", StringFunction.getNow());
				recordo1.setAttributeValue("inputTIME", StringFunction.getToday() +" "+ StringFunction.getNow());
				recordo.setAttributeValue("STATUS", "99");
				recordo.setAttributeValue("USERACCOUNTTYPE", "001");
				recordm.saveObject(recordo);
				
				//�������
				BizObjectManager mm =jbo.getManager("jbo.trade.acct_transfer",tx);
				
				//ƽ̨Ӫ���˻�
				BizObject transfer1 = mm.newObject();
				transfer1.setAttributeValue("objectno", investCode);
				transfer1.setAttributeValue("objecttype", "040");
				transfer1.setAttributeValue("seqid", "1");
				transfer1.setAttributeValue("userid", ARE.getProperty("HouBankSerialNo"));
				transfer1.setAttributeValue("direction", "R");
				transfer1.setAttributeValue("amount", restoresum2);
				transfer1.setAttributeValue("status", "99");
				transfer1.setAttributeValue("inputdate", StringFunction.getToday());
				transfer1.setAttributeValue("inputtime", StringFunction.getNow());
				transfer1.setAttributeValue("transserialno", recordo1.getAttribute("serialno"));
				transfer1.setAttributeValue("remark", "��������Ͷ������");
				transfer1.setAttributeValue("transcode", "1001");
				transfer1.setAttributeValue("useraccounttype", "003");
				mm.saveObject(transfer1);
				
				//Ͷ����
				BizObject transfer = mm.newObject();						
				transfer.setAttributeValue("objectno", investCode);
				transfer.setAttributeValue("objecttype", "040");
				transfer.setAttributeValue("seqid", "3");
				transfer.setAttributeValue("userid", investuser);
				transfer.setAttributeValue("direction", "P");
				transfer.setAttributeValue("amount", restoresum2);
				transfer.setAttributeValue("status", "99");
				transfer.setAttributeValue("inputdate", StringFunction.getToday());
				transfer.setAttributeValue("inputtime", StringFunction.getNow());
				transfer.setAttributeValue("transserialno", recordo.getAttribute("SERIALNO"));
				transfer.setAttributeValue("remark", "��������Ͷ������");
				transfer.setAttributeValue("transcode", "2001");
				transfer.setAttributeValue("useraccounttype", "001");
				mm.saveObject(transfer);
			}
			
		}catch(Exception e){}
	}
}