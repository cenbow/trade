package com.amarsoft.p2ptrade.account;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.invest.P2pString;

public class RegisterCheckHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		Parser.registerFunction("substr");
		return selectInvestDetail(request);
	}

	private JSONObject selectInvestDetail(JSONObject request) throws HandlerException {
		JSONObject result = new JSONObject();
		
		String operate = (String) request.get("operate");
		String objname = (String) request.get("objname");
		String objvalue = (String) request.get("objvalue");
		String userid = (String) request.get("userid");
		
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.user_account");
			if("validateusername".equalsIgnoreCase(operate)){//��֤�û���
				BizObjectQuery query = m.createQuery("select UserID from o where username=:username");
				query.setParameter("username", objname);
				
				BizObject o = query.getSingleResult(false);
				if(o==null){
					result.put("result", "OK");
				}else{
					result.put("result", "USEREXISTS");
				}
			}else if ("validateauthentication".equalsIgnoreCase(operate)){//��֤�Ƿ�Ϊ��֤�û�
				BizObjectQuery query1 = m.createQuery("select USERAUTHFLAG from o where userid=:userid");
				query1.setParameter("userid", userid);
				BizObject o1 = query1.getSingleResult(false);
				if(o1!=null){
				    String authFlag=o1.getAttribute("USERAUTHFLAG").toString();
				    if(authFlag.equals("2")){
				        result.put("result", "OK");
				    }else
				        result.put("result", "notok");
				}else{
				    result.put("result", "notok");
				}		
			}else if ("validatemobile".equalsIgnoreCase(operate)){//��֤�ֻ���
			    BizObjectQuery query1 = m.createQuery("select UserID from o where phonetel=:phonetel");
			    query1.setParameter("phonetel", objname);
			    BizObject o1 = query1.getSingleResult(false);
			    if(o1==null){
			        result.put("result", "OK");
			    }else{
			        result.put("result", "USEREXISTS");
			    }		
			}else if ("validateold_password".equalsIgnoreCase(operate)){//��֤����
				BizObjectQuery query1 = m.createQuery("select UserID from o where password=:password and userid=:userid");
				query1.setParameter("password", MessageDigest.getDigestAsLowerHexString("MD5", objname).toUpperCase());
				query1.setParameter("userid", userid);
				BizObject o1 = query1.getSingleResult(false);
				if(o1==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if ("validatequestion".equalsIgnoreCase(operate)){//��֤��ȫ����
				BizObjectQuery query2 = m.createQuery("select UserID from o where securityquestion=:securityquestion and userid=:userid");
				query2.setParameter("securityquestion",objname );
				query2.setParameter("userid", userid);
				BizObject o2 = query2.getSingleResult(false);
				if(o2==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if ("validateanswer".equalsIgnoreCase(operate)){//��֤�����
				BizObjectQuery query3 = m.createQuery("select UserID from o where securityanswer=:securityanswer and userid=:userid");
				objname=URLDecoder.decode(URLDecoder.decode(objname, "UTF-8"),"UTF-8");
				query3.setParameter("securityanswer",objname );
				query3.setParameter("userid", userid);
				BizObject o3 = query3.getSingleResult(false);
				if(o3==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if ("validatetranspwd".equalsIgnoreCase(operate)){//��֤��������
				BizObjectQuery query1 = m.createQuery("select UserID from o where transpwd=:transpwd and userid=:userid");
				query1.setParameter("transpwd", MessageDigest.getDigestAsLowerHexString("MD5", objname).toUpperCase());
				query1.setParameter("userid", userid);
				BizObject o1 = query1.getSingleResult(false);
				if(o1==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if ("validatequestion2".equalsIgnoreCase(operate)){//��֤��ȫ����2
				BizObjectQuery query2 = m.createQuery("select UserID from o where securityquestion2=:securityquestion2 and userid=:userid");
				query2.setParameter("securityquestion2",objname );
				query2.setParameter("userid", userid);
				BizObject o2 = query2.getSingleResult(false);
				if(o2==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if ("validateanswer2".equalsIgnoreCase(operate)){//��֤�����2
				BizObjectQuery query3 = m.createQuery("select UserID from o where securityanswer2=:securityanswer2 and userid=:userid");
				objname=URLDecoder.decode(URLDecoder.decode(objname, "UTF-8"),"UTF-8");
				query3.setParameter("securityanswer2",objname );
				query3.setParameter("userid", userid);
				BizObject o3 = query3.getSingleResult(false);
				if(o3==null){
					result.put("result", "ERROR");
				}else{
					result.put("result", "OK");
				}
			}else if("validatecode".equals(operate)){//��֤������֤��
				BizObjectManager m1 = jbo.getManager("jbo.trade.phone_msg");
				//BizObjectQuery query1 = m1.createQuery("select chkmsg from o where status='0' and telphone=:telphone and chkmsg=:chkmsg and substr(inputtime,0,10)=:inputtime");
				String str = StringFunction.getToday()+" "+StringFunction.getNow();
				str = P2pString.addDateFormat(str, 1, -5,"yyyy/MM/dd HH:mm:ss");
				ARE.getLog().debug(str);
				BizObjectQuery query1 = m1.createQuery("select chkmsg from o where inputtime >='"+str+"' and status='0' and telphone=:telphone and chkmsg=:chkmsg" );
				query1.setParameter("telphone", objname).setParameter("chkmsg",objvalue).setParameter("inputtime", StringFunction.getToday());
				BizObject o1 = query1.getSingleResult(false);
				if(o1!=null){
					result.put("result", "OK");
				}else{
					result.put("result", "ERROR");
				}
			}else if("validatecode_clear".equals(operate)){//��֤������֤��
				BizObjectManager m1 = jbo.getManager("jbo.trade.phone_msg");
				//BizObjectQuery query1 = m1.createQuery("select chkmsg from o where status='0' and telphone=:telphone and chkmsg=:chkmsg and substr(inputtime,0,10)=:inputtime");
				String str = StringFunction.getToday()+" "+StringFunction.getNow();
				str = P2pString.addDateFormat(str, 1, -5,"yyyy/MM/dd HH:mm:ss");
				ARE.getLog().debug(str);
				BizObjectQuery query1 = m1.createQuery("select chkmsg from o where inputtime >='"+str+"' and status='0' and telphone=:telphone and chkmsg=:chkmsg" );
				query1.setParameter("telphone", objname).setParameter("chkmsg",objvalue).setParameter("inputtime", StringFunction.getToday());
				BizObject o1 = query1.getSingleResult(true);
				if(o1!=null){
					//���
					o1.setAttributeValue("status", "1");
					m1.saveObject(o1);
					result.put("result", "OK");
				}else{
					result.put("result", "ERROR");
				}
			}else if("validatecertid".equals(operate)){//��֤���֤����
				BizObjectManager m1 = jbo.getManager("jbo.trade.account_detail");
				BizObjectQuery query1 = m1.createQuery("select certid from o where certid=:certid");
				query1.setParameter("certid", objname);
				BizObject o1 = query1.getSingleResult(false);
				if(o1==null){
					if(CheckLisince(objname)){
					result.put("result", "OK");
					}else{
						//���Ϸ�
						result.put("result", "WRONG");
					}
				}else{
					result.put("result", "USEREXISTS");
				}
			}else if("validateAccountNo".equals(operate)){//��֤���п���
				BizObjectManager m1 = jbo.getManager("jbo.trade.account_info");
				BizObjectQuery query1 = m1.createQuery("select ACCOUNTNO from o where userid=:userid and accountno=:accountno and status=:status ");
				query1.setParameter("userid", userid);
				query1.setParameter("accountno", objname);
				query1.setParameter("status", "2");
				BizObject o1 = query1.getSingleResult(false);
				if(o1==null){
					result.put("result", "OK");
				}else{
					result.put("result", "USEREXISTS");
				}
			}else if("validateisReturnCard".equals(operate)){//��֤�Ƿ񻹿
				BizObjectManager m1 = jbo.getManager("jbo.trade.account_info");
				BizObjectQuery query1 = m1.createQuery("select ISRETURNCARD from o where userid=:userid");
				query1.setParameter("userid", userid);
				List<BizObject> list = query1.getResultList(false);
				if(list != null){
					for(int i=0;i<list.size();i++){
						BizObject bizObj = list.get(i);
						if(bizObj!=null){
							if("1".equals(bizObj.getAttribute("ISRETURNCARD").getString())){
								result.put("result", "EXISTS");
							}else{
								result.put("result", "OK");
							}
						}
					}
				}
			}else if("validate_email".equals(operate)){//��֤�����Ƿ񱻰�
				BizObjectManager m1 = jbo.getManager("jbo.trade.user_account");
				BizObjectQuery query1 = m1.createQuery("select emailauthflag ,email from  o where  emailauthflag='2' and email=:email");
				query1.setParameter("email", objname);
				
				BizObject o = query1.getSingleResult(false);
				if(o!=null){
					result.put("result", "EXISTS");
				} else{
					result.put("result", "OK");
				}
			}
			else if ("validatemobile_user".equalsIgnoreCase(operate)){//��֤�ֻ���:��Ե�ǰ�û�
			    BizObjectQuery query1 = m.createQuery("select UserID from o where phonetel=:phonetel and userid=:userid");
			    query1.setParameter("phonetel", objname).setParameter("userid", userid);
			    BizObject o1 = query1.getSingleResult(false);
			    if(o1!=null){
			        result.put("result", "OK");
			    }else{
			        result.put("result", "ERROR");
			    }		
			}else if("validatecertid_user".equals(operate)){//��֤���֤���룺��Ե�ǰ�û�
				BizObjectManager m1 = jbo.getManager("jbo.trade.account_detail");
				BizObjectQuery query1 = m1.createQuery("select certid from o where certid=:certid and userid=:userid");
				query1.setParameter("certid", objname).setParameter("userid", userid);
				BizObject o1 = query1.getSingleResult(false);
				if(o1!=null){
			        result.put("result", "OK");
			    }else{
			        result.put("result", "ERROR");
			    }	
			}else 
				result.put("result", "ERROR");
		} catch (JBOException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 	
		return result;
	}
	
	/**
	 * У�����֤���Ƿ�Ϸ�
	 * @throws Exception 
	 * */
	public static boolean CheckLisince(String ID)
	{    	
		String checkedValue = ID;
		checkedValue = checkedValue.trim();
		if (checkedValue.length() != 15 && checkedValue.length() != 18)
			return false;
		String dateValue;
		if (checkedValue.length() == 15)
			dateValue = "19" + checkedValue.substring(6, 12);		
		else
			dateValue = checkedValue.substring(6, 14);
		if (!checkDate(dateValue))
			return false;
		
		if (checkedValue.length() == 18)		    
			return checkPersonId(checkedValue);
		return true;   
	}
	/**
	 * ���֤У��
	 * */
	public static boolean checkDate(String sDate) 
	{
		String checkedDate = sDate;
		int year,month,day;	

		int maxDay [] = {0,31,29,31,30,31,30,31,31,30,31,30,31};
		//if(checkedDate == null ) return false;
		checkedDate = checkedDate.trim();
		if (checkedDate.length() != 8 && checkedDate.length() != 14) 
		{
			return false;
		}
		year = Integer.parseInt(checkedDate.substring(0, 4).trim());
		month = Integer.parseInt(checkedDate.substring(4, 6).trim());
		day = Integer.parseInt(checkedDate.substring(6, 8).trim());

		if (year < 1900) {
			return false;
		}

		if (month < 1 || month > 12) {
			return false;
		}

		if (day > maxDay[month] || day == 0) {
			return false;
		}

		if (day == 29 && month == 2 && (year % 4 != 0 || year % 100 == 0) && (year % 4 != 0 || year % 400 != 0)) 
		{
			return false;
		}
		if (checkedDate.length() == 14) 
		{
			
			int hour = Integer.parseInt(checkedDate.substring(8, 10));
			int miniute = Integer.parseInt(checkedDate.substring(10, 12));
			int second = Integer.parseInt(checkedDate.substring(12, 14));
			

			if (hour > 23 || hour < 0) {
				return false;
			}

			if (miniute > 59 || miniute < 0) {
				return false;
			}

			if (second > 59 || second < 0) {
				return false;
			}
		}
		return true;
	}
	
	public static boolean checkPersonId(String personId) 
	{
		String strJiaoYan[] = {"1","0","X","9","8","7","6","5","4","3","2"};
		int [] intQuan = {7,9,10,5,8,4,2,1,6,3,7,9,10,5,8,4,2,1};
		int intTemp = 0;
		for (int i = 0; i < personId.length() - 1; i++)
	    {
				intTemp += Integer.parseInt(personId.substring(i, i + 1)) * intQuan[i];
	    }
		intTemp %= 11;
		return personId.substring(personId.length() - 1).equals(strJiaoYan[intTemp]);
	}

	
	//���֤��֤����
	

}
