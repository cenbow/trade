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
 * �۰�̨�û�ʵ����֤��һ����¼�������Ϣ 
 * ���������
 * 		UserID		�û�ID
 * 		RealName    ��ʵ����
 * 		Sex         �Ա�
 * 		BornDate    ��������

 * ����������ɹ���־
 *
 */
public class SetHMTUserRealNameStepOneHandler extends JSONHandler {

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
		if (request.get("RealName") == null || "".equals(request.get("RealName")))
			throw new HandlerException("realname.error");
		if (request.get("Sex") == null || "".equals(request.get("Sex")))
			throw new HandlerException("sex.error");
		if (request.get("BornDate") == null || "".equals(request.get("BornDate")))
			throw new HandlerException("birthday.error");

		String sUserID = request.get("UserID").toString();//�û����
		String sRealName = request.get("RealName").toString();//��ʵ����
		String sSex = request.get("Sex").toString();//�Ա�
		String sBirthDay = request.get("BornDate").toString();//��������
		
		JBOFactory jbo = JBOFactory.getFactory();
		try {
			TimeTool tool = new TimeTool();
			String sCurrentTime = tool.getsCurrentMoment();
			
			// �˻���ϸ��Ϣ�������
			BizObjectManager detailManager = jbo.getManager(
					"jbo.trade.account_detail");
			BizObject detailBo = detailManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			//�ж���ϸ��Ϣ�Ƿ��д˼�¼
			if(detailBo == null){
				detailBo = detailManager.newObject();
				detailBo.setAttributeValue("INPUTTIME", sCurrentTime);
				detailBo.setAttributeValue("USERID", sUserID);
			}else{
				detailBo.setAttributeValue("UPDATETIME", sCurrentTime);
			}
			detailBo.setAttributeValue("REALNAME", sRealName);
			detailBo.setAttributeValue("SEXUAL", sSex);
			detailBo.setAttributeValue("BORNDATE", sBirthDay);
			detailManager.saveObject(detailBo);
			return null;
		} 
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("setuserrealname.error");
		}
	}

}
