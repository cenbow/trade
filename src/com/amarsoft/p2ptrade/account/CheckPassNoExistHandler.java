package com.amarsoft.p2ptrade.account;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.bbcloud.accessmanager.sdk.az.BBCloudUserService;
import com.bbcloud.accessmanager.sdk.az.impl.UserServiceMgr;

/**
 * ���ͨ��֤�����Ƿ����
 * ���������
 * 		PassNo:ͨ��֤����
 * ���������
 * 		У����
 *
 */
public class CheckPassNoExistHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String sPassNo = (String) request.get("PassNo");
		if(sPassNo == null || sPassNo.length() == 0){
			throw new HandlerException("common.emptyPass");
		}

		//����˻��Ƿ��쳣
		try{
			BizObject boz = JBOFactory.getBizObjectManager("jbo.trade.user_authentication")
					.createQuery("select * from o where passno=:passno")
					.setParameter("passno", sPassNo)
					.getSingleResult(false);
			if(boz!=null){
				throw new HandlerException("passno.exist.error");
			}
			return null;
		}
		catch(JBOException je){
			je.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}

}
