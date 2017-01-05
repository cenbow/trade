package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.TimeTool;
import com.amarsoft.p2ptrade.webservice.valids.UniqueCertIDHandler;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * ��½�û�ʵ����֤
 * ���������
 * 		UserID		�û�ID
 * 		RealName    ��ʵ����
 * 		CertID		���֤��
 * 		ExpiryDate  ֤����Ч�� 
 * ����������ɹ���־
 *
 */
public class SetContUserRealNameHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//������֤�Ƿ��ظ�
		UniqueCertIDHandler handler = new UniqueCertIDHandler();
		JSONObject checkResult = (JSONObject)handler.createResponse(request, arg1);
		if(checkResult.containsKey("CheckResult")){
			String sCheckResult = checkResult.get("CheckResult").toString();
			if(sCheckResult.equals("1")){
				return setUserRealName(request);
			}
			else{
				return checkResult;
			}
		}
		else{
			throw new HandlerException("�����������");
		}
	}
	
	/**
	 * ʵ����֤
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject setUserRealName(JSONObject request) throws HandlerException {
		JSONObject result = new JSONObject();
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("userid.error");
		if (request.get("RealName") == null || "".equals(request.get("RealName")))
			throw new HandlerException("realname.error");
		if (request.get("CertID") == null || "".equals(request.get("CertID")))
			throw new HandlerException("certid.error");
//		if (request.get("ExpiryDate") == null || "".equals(request.get("ExpiryDate")))
//			throw new HandlerException("expirydate.error");

		String sUserID = request.get("UserID").toString();
		String sRealName = request.get("RealName").toString();//��ʵ����
		String sCertID = request.get("CertID").toString();//֤����
//		String sExpiryDate = request.get("ExpiryDate").toString();//֤����Ч��
		
		JBOFactory jbo = JBOFactory.getFactory();
		JBOTransaction tx = null;
		try {
			tx = jbo.createTransaction();
			
			TimeTool tool = new TimeTool();
			String sCurrentTime = tool.getsCurrentMoment();
			
			// �˻���ϸ��Ϣ�������
			BizObjectManager detailManager = jbo.getManager(
					"jbo.trade.account_detail",tx);
			BizObject detailBo = detailManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			//�ж���ϸ��Ϣ�Ƿ��д˼�¼
			if(detailBo == null){
				detailBo = detailManager.newObject();
				detailBo.setAttributeValue("USERID", sUserID);
				detailBo.setAttributeValue("INPUTTIME", sCurrentTime);
			}else{
				detailBo.setAttributeValue("UPDATETIME", sCurrentTime);
			}
			
			int a = Integer.parseInt(sCertID.substring(sCertID.length()-2, sCertID.length()-1));
			String sBornDate = null;
			String sSex = null;
			if(a % 2 == 0){//ż����Ů
				sSex = "2";
			}else{//��������
				sSex = "1";
			}
			
			if(sCertID.length() == 18){
				sBornDate = sCertID.substring(6, 10) + "/" +sCertID.substring(10, 12)+"/"+sCertID.substring(12, 14);
			}
			
			detailBo.setAttributeValue("REALNAME", sRealName);
			detailBo.setAttributeValue("CERTTYPE", "Ind01");
			detailBo.setAttributeValue("CERTID", sCertID);
			if(sSex != null){
				detailBo.setAttributeValue("SEXUAL", sSex);
			}
			if(sBornDate != null){
				detailBo.setAttributeValue("BORNDATE", sBornDate);
			}
			detailManager.saveObject(detailBo);
			
			//�û���֤��Ϣ
			BizObjectManager authenManager = jbo.getManager(
					"jbo.trade.user_authentication",tx);
			BizObject authenBo = authenManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			if(authenBo == null){
				authenBo = authenManager.newObject();
				authenBo.setAttributeValue("USERID", sUserID);
				authenBo.setAttributeValue("INPUTTIME", sCurrentTime);
			}else{
				authenBo.setAttributeValue("UPDATETIME", sCurrentTime);
			}
			authenBo.setAttributeValue("IDTYPE", "Ind01");
			authenBo.setAttributeValue("DOCID", sCertID);
//			authenBo.setAttributeValue("EXPIRYDATE", sExpiryDate);
			authenBo.setAttributeValue("STATUS", "2");
			authenManager.saveObject(authenBo);
			
			//�û��ʺ���Ϣ��֤��־
			BizObjectManager accountManager = jbo.getManager(
					"jbo.trade.user_account",tx);
			BizObject accountBo = accountManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			if(null !=  accountBo){
				
				//���͵�����ϵͳʵ����֤��Ϣ
				java.util.HashMap<String,Object> recordMap = new java.util.HashMap<String,Object>();
				recordMap.put("USERNAME", sRealName);
				recordMap.put("CERTID", sCertID);
				recordMap.put("USERID", sUserID);
				//��ʱд�� ʵ����֤��Ϣ
				RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation("3010", "1002");
				rttt.init(recordMap);
				rttt.execute();
				//������ɹ������׳��쳣
				if(rttt.getTemplet().isSuccess())
				{
					accountBo.setAttributeValue("USERAUTHFLAG", "2");
					accountManager.saveObject(accountBo);
				}
				else
					throw new HandlerException("setuserrealname.error");
			}else{
				throw new HandlerException("queryuseraccount.nodata.error");
			}
			tx.commit();
			result.put("SuccessFlag", "S");
			return result;
		} 
		catch (HandlerException e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			throw e;
		} 
		catch (Exception e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new HandlerException("setuserrealname.error");
		}
	}

}
