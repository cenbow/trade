package com.amarsoft.p2ptrade.account;


import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * ���ֻ�
 * ���������
 * 		UserID		�û�ID
 * 		phonetel   ���ֻ���
 * 		phoneauthflag���ֻ��Ƿ���֤����2��Ϊ����֤
 * 
 * ���������
 * 		SuccessFlag:�ɹ���ʶ	S/F
 * 		FailCode:	ʧ��ԭ��
 * 		FailDesc:	ʧ��ԭ��˵��		
 * @author yxpan 2014/9/24
 *
 */                     
public class  ModifyPhoneHandler extends JSONHandler {

	@SuppressWarnings("unchecked")
	@Override
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userID = (String) request.get("UserID");
		//ԭ�ֻ�����
		//String phonetel1= (String) request.get("phonetel1");
		//���ֻ�����
		String phonetel = (String) request.get("phonetel");
       //�ж��û�ID�Ƿ�Ϊ��
		if(userID == null || userID.length() == 0){
			throw new HandlerException("modifyuseraccount.emptyuserid");
		}
		//�ж����ֻ��ֻ����Ƿ�Ϊ��
		if(phonetel == null || phonetel.length() == 0){
			throw new HandlerException("modifyphonetel.empty");
		}
		JBOFactory jbo = JBOFactory.getFactory();
		JSONObject result = new JSONObject();
		//�����ݿ��ѯԭ�����ֻ�����
		String oldphonetel=getOldPhone(jbo,phonetel);
		if(!phonetel.equals(oldphonetel)){
			try{
				
				BizObjectManager userAcctManager =jbo.getManager("jbo.trade.user_account");
				BizObjectQuery query = userAcctManager.createQuery("select * from o where UserID=:UserID");
				query.setParameter("UserID", userID);
				BizObject userAccount = query.getSingleResult(true);
				if(userAccount != null){
					//����ʵʱ����,�����û���֤����
					java.util.HashMap<String,Object> recordMap = new java.util.HashMap<String,Object>();
					recordMap.put("USERID", userID);
					recordMap.put("PHONENO", phonetel);
					//��ʱд��
					RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation("3010", "1001");
					rttt.init(recordMap);
					rttt.execute();
					
					if(rttt.getTemplet().isSuccess())
					{
						//�޸Ļ�����Ϣ
						userAccount.setAttributeValue("phonetel", phonetel);
						//Ĭ�Ͻ��ֻ���֤��ʶ����Ϊ2
						userAccount.setAttributeValue("phoneauthflag","2");
					    userAcctManager.saveObject(userAccount);
					}
					else
						throw new HandlerException("modifyuseraccount.error");
					
				}else{
					throw new HandlerException("modifyuseraccount.usernotexist");
				}
			}catch(JBOException e){
				e.printStackTrace();
				throw new HandlerException("modifyuseraccount.error");
			}catch(HandlerException e){
				e.printStackTrace();
				throw e;
			}catch(Exception e){
				e.printStackTrace();
				throw new HandlerException("modifyuseraccount.error");
			}
		
		}else{
			throw new HandlerException("modifyuseraccount.phonetelexist");
	
		}
		 result.put("flag","success");
		 return result;
	}
	//�ж����ݿ��ֻ�����
	@SuppressWarnings("unused")
	private String getOldPhone(JBOFactory jbo, String phonetel) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = m.createQuery("phonetel=:phonetel and phoneauthflag='2'");
			query.setParameter("phonetel", phonetel);
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				return o.getAttribute("phonetel").getString();
			} else {
				return "";
			}
		} catch (Exception e) {
			throw new HandlerException("quaryphonetel.error");
			//return "";
		}
	}			
}
