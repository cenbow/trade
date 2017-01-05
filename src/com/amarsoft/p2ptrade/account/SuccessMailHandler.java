package com.amarsoft.p2ptrade.account;

import java.io.UnsupportedEncodingException;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
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
public class  SuccessMailHandler extends JSONHandler {


	@SuppressWarnings("unchecked")
	@Override
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//String userID = (String) request.get("UserID");
		String urlSerialNo = (String) request.get("serialno");
		String urlChkmsg = (String) request.get("chkmsg");
		
		String SerialNo ="";
		String Chkmsg="";

		
		 //�ж���ˮ���Ƿ�Ϊ��
		if(urlSerialNo == null || urlSerialNo.length() == 0){
				throw new HandlerException("mailserialno.isempty");
		}
		 if(urlSerialNo.split("__chkmsg=").length>1){
			 String str [] = urlSerialNo.split("__chkmsg=");
			 SerialNo = str[0];
			 Chkmsg = str[1];
		 }else{
			 throw new HandlerException("mailserialno.isempty");
		 }
			try {
				SerialNo = UrlBase64.decode(SerialNo);
				Chkmsg = UrlBase64.decode(Chkmsg);
			} catch (UnsupportedEncodingException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		//���ݿ��в�ѯ����ˮ��
		JBOFactory jbo1 = JBOFactory.getFactory();
    	String userID=getUserId(jbo1,SerialNo);
       //�ж��û�ID�Ƿ�Ϊ��
		if(userID == null || userID.length() == 0){
			throw new HandlerException("modifyuseraccount.emptyuserid");
		}

		 
	 //�ж���֤��Ϣ�Ƿ�Ϊ��
//		if(Chkmsg == null || Chkmsg.length() == 0){
//					throw new HandlerException("mailchkmsg.isempty");
//				}
				JBOFactory jbo = JBOFactory.getFactory();
				//���ݿ��в�ѯ����ˮ��
		    	String serialNo1=getSerialNo(jbo,userID,SerialNo);
				//���ݿ��в�ѯ������֤��Ϣ
		    	String chkmsg1=getChkmsg(jbo,userID,SerialNo);

		     JSONObject result = new JSONObject();
		     if(SerialNo.equals(serialNo1)&&Chkmsg.equals(chkmsg1)){
			try{
				BizObjectManager userAcctManager =jbo.getManager("jbo.trade.user_account");
				BizObjectQuery query = userAcctManager.createQuery("select * from o where UserID=:UserID");
				query.setParameter("UserID", userID);
				BizObject userAccount = query.getSingleResult(true);
				if(userAccount != null){
					//Ĭ�Ͻ��ʼ���־��Ϊ2
					userAccount.setAttributeValue("emailauthflag","2");
				    userAcctManager.saveObject(userAccount);
				    //��mail_msg���в�������,������֤��emailauthflag��Ϊ2
					JBOTransaction tx = jbo.createTransaction();;
					BizObjectManager m0 = jbo.getManager("jbo.trade.email_msg",tx);
					BizObject detailBo = m0.createQuery("o.userid=:userid and o.serialno=:SerialNo")
							.setParameter("userid", userID).setParameter("serialno", SerialNo).getSingleResult(true);
					if(null !=  detailBo){
						detailBo.setAttributeValue("emailauthflag", "2");
						m0.saveObject(detailBo);
					}else{
						throw new HandlerException("queryuseraccount.nodata.error");
					}
					tx.commit();
				    
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
					throw new HandlerException("mailserialno.isempty");
		     }

		 result.put("flag","success");
		 return result;
	}

	
	//�жϵ�ַ������Ϣ�а�������ˮ����֤��Ϣ�Ƿ���ʼ���������ĵ�ַһ��
	@SuppressWarnings("unused")
	private String getSerialNo(JBOFactory jbo, String userID,String SerialNo)
			throws HandlerException {
			BizObjectManager m;
			try {
				m = jbo.getManager("jbo.trade.email_msg");
				BizObjectQuery query = m.createQuery("select serialno from o where o.userid=:userid and o.serialno=:SerialNo");
				query.setParameter("userid", userID);
				query.setParameter("serialno",SerialNo);
				BizObject o = query.getSingleResult(false);
				if (o != null) {
					return o.getAttribute("serialno").getString();
				} else {
					return "";
				}
			} catch (Exception e) {
				throw new HandlerException("mailserialno.iserror");
			}
		}
	//��ȡ������֤��Ϣ
	private String getChkmsg(JBOFactory jbo, String userID,String SerialNo)
			throws HandlerException {
			BizObjectManager m;
			try {
				m = jbo.getManager("jbo.trade.email_msg");
				BizObjectQuery query = m.createQuery("select chkmsg from o where o.userid=:userid and o.serialno=:SerialNo");
				query.setParameter("userid", userID);
				query.setParameter("serialno",SerialNo);
				BizObject o = query.getSingleResult(false);
				if (o != null) {
					return o.getAttribute("chkmsg").getString();
				} else {
					return "";
				}
			} catch (Exception e) {
				throw new HandlerException("mailchkmsg.iserror");
			}
		}
	
	//��ȡ�û�ID
		private String getUserId(JBOFactory jbo,String SerialNo)
				throws HandlerException {
				BizObjectManager m;
				try {
					m = jbo.getManager("jbo.trade.email_msg");
					BizObjectQuery query = m.createQuery("select userid from o where o.serialno=:SerialNo");
					query.setParameter("serialno",SerialNo);
					BizObject o = query.getSingleResult(false);
					if (o != null) {
						return o.getAttribute("userid").getString();
					} else {
						return "";
					}
				} catch (Exception e) {
					throw new HandlerException("modifyuseraccount.emptyuserid");
				}
			}
		
	
}
