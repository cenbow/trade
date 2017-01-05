package com.amarsoft.p2ptrade.account;

import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * ɾ�����п�
 * ���������
 * 		serialno:	��ˮ��
 * ���������
 * 		SuccessFlag:�Ƿ�ɹ�	S/F
 *
 */
public class DeleteBankcardHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String sUserID = (String) request.get("UserID");
		String operate = (String) request.get("operate");
		String SerialNO = (String) request.get("objname");
		//�����벻��Ϊ��
		if(SerialNO == null || SerialNO.length() == 0){
			throw new HandlerException("emptyserialno");
		}
		
		JSONObject result = new JSONObject();
		
		try{
			if("validatebankcard".equalsIgnoreCase(operate)){//��֤���п�
				JBOFactory jbo = JBOFactory.getFactory();
				BizObjectManager m =jbo.getManager("jbo.trade.account_info");
				
				BizObjectQuery query = m.createQuery("select * from o where serialno=:serialno and userid=:userid");
				query.setParameter("serialno", SerialNO).setParameter("userid", sUserID);
	    
				BizObject o = query.getSingleResult(true);
				if(o!=null){
					String sStatus = Integer.toString(0);//�޸�״ֵ̬
					
					//��󿨲��� add by xjqin 20150122
					HashMap<String,Object> parameters = new HashMap<String,Object>();
					parameters.put("TRANSCHANNEL", "3010");
					parameters.put("TRANSTYPE", "1075");
					parameters.put("RELAACCOUNT", SerialNO);
					parameters.put("USERID", sUserID);
					
					RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation("3010", "1075");
					rttt.init(parameters);
					rttt.execute();
					String sLogId = rttt.getLogId();
					if(rttt.getTemplet().isSuccess())
					{
						o.setAttributeValue("STATUS",sStatus );
						m.saveObject(o);
						result.put("result", "OK");
						result.put("message", "");
					}
					else
					{
						result.put("result", "ERROR");
						result.put("message", rttt.getTemplet().getFeedBackMsg());
						//throw new HandlerException("delete.error");
					}
				}
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("delete.error");
		}	
		
		return result;
	}
}
