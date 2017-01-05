package com.amarsoft.p2ptrade.account;

import java.util.Properties;
import java.util.Random;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.util.JavaSendMail;
import com.amarsoft.p2ptrade.util.UrlBase64;

/**
 * ���ֻ�
 * ���������
 * 		UserID		�û�ID
 * 		mail  ������
 * 		EMAILAUTHFLAG�������ʼ�����1��Ϊ�ѷ��ͣ�����֤����֤�ɹ�EMAILAUTHFLAGΪ��2��
 * 
 * ���������
 * 		SuccessFlag:�ɹ���ʶ	S/F
 * 		FailCode:	ʧ��ԭ��
 * 		FailDesc:	ʧ��ԭ��˵��		
 * @author yxpan 2014/9/24
 *
 */                     
public class  ModifyMailHandler extends JSONHandler {


	@SuppressWarnings("unchecked")
	@Override
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userID = (String) request.get("UserID");
		String email = (String) request.get("email");
		 // �������ַ
		String url= ARE.getProperty("url");
       //�ж��û�ID�Ƿ�Ϊ��
		if(userID == null || userID.length() == 0){
			throw new HandlerException("modifyuseraccount.emptyuserid");
		}
		//�ж������Ƿ�Ϊ��
		if(email == null || email.length() == 0){
			throw new HandlerException("modifymail.empty");
		}

		    JSONObject result = new JSONObject();
			try{
				JBOFactory jbo = JBOFactory.getFactory();
				BizObjectManager userAcctManager =jbo.getManager("jbo.trade.user_account");
				
				BizObjectQuery q = userAcctManager.createQuery("select * from o where email=:email");
				q.setParameter("email", email);
				BizObject em = q.getSingleResult(false);
				if(em!=null){
					throw new HandlerException("modifyuseraccount.emailexist");
				}
				
				BizObjectQuery query = userAcctManager.createQuery("select * from o where UserID=:UserID");
				query.setParameter("UserID", userID);
				BizObject userAccount = query.getSingleResult(true);
				if(userAccount != null){

				    //��mail_msg���в�������
				    String inputtime = StringFunction.getTodayNow();
					String chkmsg = getRandomString(6);
					BizObjectManager m0 = jbo.getManager("jbo.trade.email_msg");
					BizObject newobject = m0.newObject();
					newobject.setAttributeValue("userid",userID);
					newobject.setAttributeValue("email", email);
			    	newobject.setAttributeValue("inputtime",inputtime);
					newobject.setAttributeValue("chkmsg", chkmsg);
					newobject.setAttributeValue("emailauthflag", "1");
					m0.saveObject(newobject);
					String sSerialNo = newobject.getAttribute("serialno").toString();
					String sContent = "����~��л��ע��񱾽��ڣ�����֤�������䡣���������֤���䣺 "+url+"/mail_success.jsp?serialno="+UrlBase64.encoded(sSerialNo)+"__chkmsg="+UrlBase64.encoded(chkmsg);
					newobject.setAttributeValue("content", sContent);
					m0.saveObject(newobject);
					//�����ʼ�
					JavaSendMail.SendEmail(sContent,email);
					String sendtime = StringFunction.getTodayNow();
					newobject.setAttributeValue("sendtime",sendtime);
					m0.saveObject(newobject);
				    
					//�޸Ļ�����Ϣ
					userAccount.setAttributeValue("email", email);
					//Ĭ�Ͻ��ʼ���־��Ϊ1
					userAccount.setAttributeValue("emailauthflag","1");
				    userAcctManager.saveObject(userAccount);
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
		
		 result.put("flag","success");
		 return result;
	}

	

	



	//�Զ�����6λ�����
	public  String getRandomString(int length) { // length��ʾ�����ַ����ĳ���
		String base = "0123456789";
		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < length; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}	
}
