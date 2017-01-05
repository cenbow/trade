package com.amarsoft.p2ptrade.account;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/**
 * ��¼����
 * ���������
 * 		UserName:				�û���
 * 		Password:				����	
 * 		BelongSystem:			����ϵͳ
 * ���������
 * 		LogonFlag:				�Ƿ��¼�ɹ�	S/F/L
 * 		UserID:					�û�����UserID
 * 		UserName:				�û���
 * 		Mobile:					�ֻ���
 * 		EMail:					����
 * 		AccountType��			��¼��ʽ��u��:�û�������e�������䣬��m�����ֻ��ţ���s�������ϵ�¼��ʽ����o��������
 * 		SSOToken��				�û����ĵ����¼����
 * 		FailCode:				ʧ��ԭ��
 * 		FailDesc:				ʧ��ԭ��˵��
 * 		FailCount:				����������		
 * 		TSState��				�Ƿ������û���0�����ǡ�
 * 		RetrievePasswordFlag��	�����һ������ʶ
 * 		MobileChangFlag��		�����ֻ������ʶ
 * 		
 * 		
 *
 */
public class LogonHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userName = (String) request.get("UserName");
		String password = (String) request.get("PassWord");
		
		//��¼ƾ֤����Ϊ��
		if(userName == null || userName.length() == 0){
			throw new HandlerException("logon.emptyuser");
		}
		//���벻��Ϊ��
		if(password == null || password.length() == 0){
			throw new HandlerException("logon.emptypassword");
		}
		
		return logonWithUserCenter(request);
	}
	
	private JSONObject logonWithUserCenter(JSONObject request) throws HandlerException{
		JSONObject result = new JSONObject();
		String sFirstFlag = "F";
		try{
			String userName = (String) request.get("UserName");
			String password = (String) request.get("PassWord");
			//password = MessageDigest.getDigestAsUpperHexString("MD5", password);
			
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("(UserName=:UserName or phonetel=:UserName) and password=:password ");//and lockflag<>'1'");
			query.setParameter("UserName", userName).setParameter("password", password);
			
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				request.put("UserID", o.getAttribute("USERID").getString());
				result.put("UserID", o.getAttribute("USERID").getString());
				result.put("UserName", o.getAttribute("USERNAME").getString());
				result.put("invitecode", o.getAttribute("invitecode").getString());
				
				String status = o.getAttribute("lockflag").getString();
					
				String userId = o.getAttribute("USERID").getString();
				//��������
				if("1".equals(status)){
					int s = setLogonLock(jbo, userId);
					if(s==0){					
						result.put("LogonFlag", "T");
						return result;
					}
				}
				//��ȡ�ͻ��������е���� modify by xjqin 20150120
				//modify end
				result.put("isvip", o.getAttribute("isvip").getString());
				result.put("Mobile", o.getAttribute("PHONETEL").getString());
				result.put("EMail", o.getAttribute("EMAIL").getString());
				result.put("RetrievePasswordFlag", o.getAttribute("RETRIEVEPASSWORDFLAG")==null?"":o.getAttribute("RETRIEVEPASSWORDFLAG").getString());
				result.put("MobileChangFlag", o.getAttribute("MOBILECHANGEFLAG")==null?"":o.getAttribute("MOBILECHANGEFLAG").getString());
				
				BizObjectManager userAcctManager = jbo.getManager("jbo.trade.account_detail");
				BizObjectQuery qu = userAcctManager.createQuery("select * from o where userid=:userid");
				qu.setParameter("userid", o.getAttribute("USERID").getString());
				BizObject userAccount = qu.getSingleResult(false);
				if(userAccount != null){
					result.put("IdCard", userAccount.getAttribute("certid").toString());
					result.put("realName", userAccount.getAttribute("realName").toString());
				}
				sFirstFlag = "S";
				//����û���¼���������־
				PlatformErrorHandler.clearTransPassErrorCount(jbo, o.getAttribute("USERID").getString(),"U");
				//�����¼��¼
				SingleSignOnHandler single = new SingleSignOnHandler();
				single.createResponse(request, null);
			}else{
				sFirstFlag = "F";
				insertLogonError(jbo,request);
			}
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("logon.error");
		}
		result.put("LogonFlag", sFirstFlag);
		return result;
	}
	
	/**
	 * ��¼�û���¼���������־
	 */
	private void insertLogonError(JBOFactory jbo, JSONObject request) {
		try {
			BizObjectManager m = jbo.getManager("jbo.trade.user_account");
			String userName = (String) request.get("UserName");
			String sValidType = "U";
			BizObjectQuery query = m
					.createQuery("(UserName=:UserName or UserID=:UserName or PhoneTel =:UserName or email=:UserName)");
			query.setParameter("UserName", userName);
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				String sUserID = o.getAttribute("USERID").getString();
				PlatformErrorHandler.insertValidlog(jbo, sUserID, sValidType);
				JSONObject r0 = PlatformErrorHandler.getTransPassErrorCount(
						sUserID, sValidType);
				int iCount = Integer.parseInt(r0.get("ContinueCount")
						.toString());
				if (iCount > 4) {
					String sFailedType = "U";
					PlatformErrorHandler.lockUser(jbo, sUserID,
							sFailedType);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * ���ݵ�¼���������־�ж��Ƿ����
	 */
	private int setLogonLock(JBOFactory jbo, String sUserId) {
		int flag = 0;

		try {
			JSONObject r0 = PlatformErrorHandler.getTransPassErrorCount(sUserId, "U");
			int iCount = Integer.parseInt(r0.get("ContinueCount").toString());
			if (iCount > 4) {
				String sLastTime = PlatformErrorHandler.getLastPassErrorTime(sUserId, "U");
				try {
					boolean f = isInThreeHours(sLastTime.replaceAll("/", "-"), StringFunction.getTodayNow().replaceAll("/", "-"), "00");
					if(f==false){
						PlatformErrorHandler.unlockUser(jbo, sUserId, "U");
						flag = 1;
					}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		} catch (HandlerException e) {
			e.printStackTrace();
		}
		return flag;
	}
	
	//�Ƿ���3��Сʱ����
	public static boolean isInThreeHours(String sStartTime,String sEndTime,String sType) throws ParseException{
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date d1 = sdf.parse(sEndTime);
		Date d2 = sdf.parse(sStartTime);
		long diff = d1.getTime() - d2.getTime();
		long leng = 0;
		if(sType.equals("00")){
			leng = 1000 * 60 * 180;
		}else{
			leng = 1000 * 60 * 175;
		}
		if(diff <= leng){
			return true;
		}else{
			return false;
		}
	}
}
