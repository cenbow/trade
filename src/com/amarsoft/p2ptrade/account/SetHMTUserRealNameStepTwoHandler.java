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

/**
 * �۰�̨�û�ʵ����֤�ڶ�����¼��ͨ��֤��Ϣ
 * ���������
 * 		UserID		�û�ID
 * 		PassNo      ͨ��֤����
 * 		PassType    ͨ��֤���ͣ����۰�̨��
 * 		PassExpiryDate    ͨ��֤��Ч��

 * ����������ɹ���־ 
 *
 */
public class SetHMTUserRealNameStepTwoHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return setUserRealName(request);
	}
	
	/**
	 * ʵ����֤
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject setUserRealName(JSONObject request) throws HandlerException {
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		if (request.get("PassNo") == null || "".equals(request.get("PassNo")))
			throw new HandlerException("passno.error");
		if (request.get("PassType") == null || "".equals(request.get("PassType")))
			throw new HandlerException("passtype.error");
		if (request.get("PassExpiryDate") == null || "".equals(request.get("PassExpiryDate")))
			throw new HandlerException("passexpirydate.error");

		String sUserID = request.get("UserID").toString();//�û����
		String sPassNo = request.get("PassNo").toString();//ͨ��֤����
		String sPassType = request.get("PassType").toString();//ͨ��֤����
		String sPassExpiryDate = request.get("PassExpiryDate").toString();//ͨ��֤��Ч��
		
		JBOFactory jbo = JBOFactory.getFactory();
		try {
			TimeTool tool = new TimeTool();
			String sCurrentTime = tool.getsCurrentMoment();
			
			// �û���֤��Ϣ�������
			BizObjectManager authenManager = jbo.getManager(
					"jbo.trade.user_authentication");
			BizObject authenBo = authenManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			//�ж���ϸ��Ϣ�Ƿ��д˼�¼
			if(authenBo == null){
				authenBo = authenManager.newObject();
				authenBo.setAttributeValue("USERID", sUserID);
				authenBo.setAttributeValue("INPUTTIME", sCurrentTime);
			}else{
				authenBo.setAttributeValue("UPDATETIME", sCurrentTime);
			}
			authenBo.setAttributeValue("PASSNO", sPassNo);
			authenBo.setAttributeValue("PASSTYPE", sPassType);
			authenBo.setAttributeValue("PASSEXPIRYDATE", sPassExpiryDate);
			authenManager.saveObject(authenBo);
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("setuserrealname.error");
		}
	}

}
