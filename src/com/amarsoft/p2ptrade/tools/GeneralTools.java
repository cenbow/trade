/**
 * 
 */
package com.amarsoft.p2ptrade.tools;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.security.MessageDigest;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.dict.als.manage.CodeManager;
import com.amarsoft.dict.als.object.Item;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.p2ptrade.util.SendMobileMessage;

/**
 * @ 2014-5-12
 * 
 * @author yyzhao
 * 
 */
public class GeneralTools {

	public static String getDate() throws HandlerException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String sDateAndTime = sdf.format(date);
		String[] sTimearray = sDateAndTime.split(" ");
		String sInputDate = sTimearray[0];
		return sInputDate;
	}

	public static String getTime() throws HandlerException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String sDateAndTime = sdf.format(date);
		String[] sTimearray = sDateAndTime.split(" ");
		String sInputTime = sTimearray[1];
		return sInputTime;
	}

	/**
	 * ���� n�������� ���� String
	 * 
	 * @param addday
	 *            add����
	 * @return
	 * @throws ParseException
	 */
	public static String getDate(int addday) throws HandlerException {
		Calendar c = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		c.setTime(new Date());
		c.add(Calendar.DATE, addday);
		Date date = c.getTime();
		String sDateAndTime = sdf.format(date);
		String[] sTimearray = sDateAndTime.split(" ");
		String sInputDate = sTimearray[0];
		return sInputDate;
	}

	public static double round(double d, int num) {
		BigDecimal b = new BigDecimal(Double.toString(d));
		BigDecimal one = new BigDecimal("1");
		// ������ֵ�����зǱ�����Чλ������������ʧ��
		BigDecimal r = b.divide(one, num, BigDecimal.ROUND_HALF_UP);
		// ����num+1λС����������ֵe
		BigDecimal e = b.divide(one, num + 1, BigDecimal.ROUND_HALF_UP);
		// ����num+5λС����������ֵs
		BigDecimal s = b.divide(one, num + 5, BigDecimal.ROUND_HALF_UP);
		// ���s��e��ֵ֮��Ĳ����10^(-(num+5))�ڣ���ϵͳ��Ϊ�Ǽ����о������⣬������s����ֵ������ԭ��������r�Ľ��
		if (Math.abs(s.doubleValue() - e.doubleValue()) < Math
				.pow(0.1, num + 5))
			r = s.divide(one, num, BigDecimal.ROUND_HALF_UP);
		return r.doubleValue();
	}

	/**
	 * ����ʱ��� ���� String
	 * 
	 * @param dataString
	 * @return
	 * @throws ParseException
	 */
	public static String getdiffDate(String paydate, String currdate)
			throws HandlerException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		try {
			Date pay = sdf.parse(paydate);
			Date curr = sdf.parse(currdate);
			long diffDate = pay.getTime() - curr.getTime();
			diffDate = diffDate / 1000 / 24 / 60 / 60;
			return diffDate + "";
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("getdiffDate.error");
		}
	}

	/**
	 * ����ʱ��� ���� int
	 * 
	 * @param dataString
	 * @return
	 * @throws ParseException
	 */
	public static int getdiffDateInt(String paydate, String currdate)
			throws HandlerException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		try {
			Date pay = sdf.parse(paydate);
			Date curr = sdf.parse(currdate);
			long diffDate = pay.getTime() - curr.getTime();
			diffDate = diffDate / 1000 / 24 / 60 / 60;
			int diff = (int) diffDate;
			return diff;
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("getdiffDate.error");
		}
	}

	/**
	 * ���Ͷ���
	 * 
	 * @param sTempletID
	 * @param sPhoneTel
	 * @param parameters
	 * @param jbo
	 * @param tx
	 * @return
	 * @throws HandlerException
	 */
	public static boolean sendSMS(String sTempletID, String sPhoneTel, HashMap<String, Object> parameters) throws HandlerException {
		SendMobileMessage sms = new SendMobileMessage();
		try {
			sms.execute(sTempletID, sPhoneTel, parameters);
			return sms.getMobileService().isSuccess();
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * ��ȡcode��code_library���е�����ֵ��itemname��
	 * 
	 * @param jbo
	 * @param codeNo
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getItemName(JBOFactory jbo, String codeNo)
			throws HandlerException {
		try{
			String[] codes = CodeManager.getItemArray(codeNo);
			JSONObject obj = new JSONObject();
			for(int i=0;i<codes.length;i+=2){
				obj.put(codes[i], codes[i+1]);
			}
			return obj;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getitemname.error");
		}
		/*
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("select ITEMNO,ITEMNAME from o where codeno=:codeno and isinuse='1'");
			query.setParameter("codeno", codeNo);

			List<BizObject> list = query.getResultList(false);
			JSONObject obj = new JSONObject();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					obj.put(o.getAttribute("ITEMNO").toString(), o
							.getAttribute("ITEMNAME").toString());
				}
			}
			return obj;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getitemname.error");
		}
		*/
	}

	/**
	 * ��ȡcode��code_library���е�����ֵ��itemname��
	 * 
	 * @param jbo
	 * @param codeNo
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	public static JSONObject getItemName(JBOFactory jbo, String codeNo,
			String paramString, String argString) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery(" codeno=:codeno " + argString);
			query.setParameter("codeno", codeNo);

			List<BizObject> list = query.getResultList(false);
			JSONObject obj = new JSONObject();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					obj.put(o.getAttribute("ITEMNO").toString(), o
							.getAttribute(paramString).toString());
				}
			}
			return obj;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getitemname.error");
		}
	}

	/**
	 * ��ȡ�趨�õĶ�����Ч�ڣ���λ��ms��
	 * 
	 * @param jbo
	 * @return
	 * @throws HandlerException
	 */
	public static int getOTPMesValidTime(JBOFactory jbo)
			throws HandlerException {
		try {
			Item item = CodeManager.getItem("OTPValidPeriod", "OTPValidPeriod");
			if(item!=null){
				int time = Integer.parseInt(item.getItemAttribute() == null ? "120000" : item.getItemAttribute());
				return time;
			}
			else{
				return 120000; 
			}
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getotpmesvalidtime.error");
		}
		/*
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("select ITEMATTRIBUTE from o where codeno=:codeno and itemno=:itemno");
			query.setParameter("codeno", "OTPValidPeriod").setParameter(
					"itemno", "OTPValidPeriod");

			BizObject o = query.getSingleResult(false);
			if (o != null) {
				int time = Integer.parseInt(o.getAttribute("ITEMATTRIBUTE")
						.toString() == null ? "120000" : o.getAttribute(
						"ITEMATTRIBUTE").toString());
				return time;
			} else {
				return 120000;
			}
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getotpmesvalidtime.error");
		}
		*/
	}

	/**
	 * ���㽻��������
	 * 
	 * @param jbo
	 * @return
	 * @throws HandlerException
	 */
	public static double getCalTransFee(JBOFactory jbo, String feeType,
			double amount) throws HandlerException {
		
		try {
			Item item = CodeManager.getItem("FeeCode", feeType);
			double feeProp=0.0;
			if(item!=null){
				feeProp = Double.parseDouble(item.getAttribute1() == null ? "120000" : item.getAttribute1());
			}
			return amount * (feeProp / 100.00);
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("calctransfee.error");
		}
		/*
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("select ATTRIBUTE1 from o where codeno=:codeno and itemno=:itemno");
			query.setParameter("codeno", "FeeCode").setParameter("itemno",
					feeType);

			BizObject o = query.getSingleResult(false);
			double feeProp;// ��������ȡ����
			if (o != null) {
				feeProp = Double.parseDouble(o.getAttribute("ATTRIBUTE1")
						.toString() == null ? "0" : o
						.getAttribute("ATTRIBUTE1").toString());
			} else {
				feeProp = 0.0;
			}
			return amount * (feeProp / 100.00);
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("calctransfee.error");
		}
		*/
	}

	/**
	 * ��ȡ��������
	 * 
	 * @param jbo
	 * @param sAccountBelong
	 *            ��������
	 * @param sTransChannelKey
	 *            �������� ATTRIBUTE1��ʵʱ���� ATTRIBUTE2��ʵʱ���� ATTRIBUTE5����������
	 *            ATTRIBUTE6����������
	 * @return
	 * @throws HandlerException
	 */
	public static String getTransChannel(JBOFactory jbo, String sAccountBelong,
			String sTransChannelKey) throws HandlerException {
		
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("codeno=:codeno and itemno=:itemno");
			query.setParameter("codeno", "BankNo").setParameter("itemno",
					sAccountBelong);

			BizObject o = query.getSingleResult(false);

			if (o != null) {
				return o.getAttribute(sTransChannelKey).toString() == null ? ""
						: o.getAttribute(sTransChannelKey).toString();
			} else {
				throw new HandlerException("transchannel.notexist.error");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("gettranschannel.error");
		}
		
	}

	/**
	 * ��ȡ���׵����޶�
	 * 
	 * @param jbo
	 * @param sAccountBelong
	 *            ��������
	 * @param sTransType
	 *            �������� ATTRIBUTE3���������ʴ����޶� ATTRIBUTE7���������ʴ����޶�
	 * @return
	 * @throws HandlerException
	 */
	public static double getLimitAmount(JBOFactory jbo, String sAccountBelong,
			String sTransType) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("codeno=:codeno and itemno=:itemno");
			query.setParameter("codeno", "BankNo").setParameter("itemno",
					sAccountBelong);

			BizObject o = query.getSingleResult(false);

			if (o != null) {
				return Double
						.parseDouble(o.getAttribute(sTransType).toString() == null ? "0.0"
								: o.getAttribute(sTransType).toString());
			} else {
				throw new HandlerException("translimitamount.notexist.error");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getlimitamount.error");
		}
	}

	/**
	 * ��ȡ��ֵ�ָ�ʱ���
	 * 
	 * @param jbo
	 * @param sChargeType
	 *            �������� 0010��ʵʱ��ֵ 0020�����س�ֵ
	 * @return
	 * @throws HandlerException
	 */
	public static JSONObject getChargeDividTime(JBOFactory jbo,
			String sChargeType) throws HandlerException {
		try {
			JSONObject timeArray = new JSONObject();
			Item item = CodeManager.getItem("ChargeDividTime", sChargeType);
			if(item!=null){
				String startTime = item.getAttribute1() == null ? "": item.getAttribute1();
				String endTime = item.getAttribute2() == null ? "": item.getAttribute2();
				timeArray.put("StartTime", startTime);
				timeArray.put("EndTime", endTime);
				return timeArray;
			} else {
				throw new HandlerException("getchargedividtime.notexist.error");
			}
			
		}
		catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getchargedividtime.error");
		}
		/*
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.code_library");
			BizObjectQuery query = m
					.createQuery("codeno=:codeno and itemno=:itemno");
			query.setParameter("codeno", "ChargeDividTime").setParameter(
					"itemno", sChargeType);
			BizObject o = query.getSingleResult(false);
			JSONObject timeArray = new JSONObject();
			if (o != null) {
				String startTime = o.getAttribute("ATTRIBUTE1").getValue() == null ? ""
						: o.getAttribute("ATTRIBUTE1").getString();
				String endTime = o.getAttribute("ATTRIBUTE2").getValue() == null ? ""
						: o.getAttribute("ATTRIBUTE2").getString();
				timeArray.put("StartTime", startTime);
				timeArray.put("EndTime", endTime);
				return timeArray;
			} else {
				throw new HandlerException("getchargedividtime.notexist.error");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getchargedividtime.error");
		}
		*/
	}

	/**
	 * �ж�������֤�Ƿ�ɹ�
	 * 
	 * @param jbo
	 * @return
	 * @throws HandlerException
	 */
	public static boolean getApplicationAuth(JBOFactory jbo, String sUserID)
			throws HandlerException {
		try {
			boolean auth = false;
			jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.user_account");

			BizObjectQuery query = m.createQuery("userid=:userid");
			query.setParameter("userid", sUserID);

			BizObject o = query.getSingleResult(false);
			if (o != null) {
				String sUserIdQuery = o.getAttribute("USERID").toString();
				String sPhonetel = o.getAttribute("PHONETEL").toString();
				String sUserAuthFlag = o.getAttribute("USERAUTHFLAG")
						.toString();
				String sTransPwd = o.getAttribute("TRANSPWD").toString();
				String sSecurityQuestion = o.getAttribute("SECURITYQUESTION")
						.toString();
				String sSecurityAnswer = o.getAttribute("SECURITYANSWER")
						.toString();
				if ((sUserIdQuery != null) && (sPhonetel != null)
						&& (sUserAuthFlag != null) && (sTransPwd != null)
						&& (sSecurityQuestion != null)
						&& (sSecurityAnswer != null)) {
					auth = true;
				} else {
					throw new HandlerException("queryinvestmentstat.error");
				}
			}

			return auth;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("QueryApplicationAuth.error");
		}
	}

	/**
	 * ��JSONObjectת���ɷ��������ʽ��JSON�ַ���
	 * 
	 * @param request
	 * @return
	 */
	public static String createJsonString(JSONObject request) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("RequestParams", request);
		jsonObject.put("deviceType", "Pc");
		return jsonObject.toJSONString();
	}

	/**
	 * ��JSONObjectת���ɷ��������ʽ��JSON�ַ���
	 * 
	 * @param request
	 * @return
	 */
	public static String createJsonString(JSONArray requestArray) {
		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("RequestParams", requestArray);
		jsonObject.put("deviceType", "Pc");
		return jsonObject.toJSONString();
	}

	/**
	 * ��ѧ������ת�ַ���
	 * 
	 * @param d
	 * @param integerDigits
	 * @param fractionDigits
	 * @return
	 */
	public static String numberFormat(double d, int integerDigits,
			int fractionDigits) {
		String sTemp = "";
		if (-1 < d && d < 1 && integerDigits == 0) {
			integerDigits = 1;
		}
		if (fractionDigits > 0) {
			sTemp += ".0";
			for (int i = 0; i < fractionDigits - 1; i++) {
				sTemp += "0";
			}
		}
		for (int i = 0; i < integerDigits; i++) {
			sTemp = "0" + sTemp;
		}
		DecimalFormat df = new DecimalFormat(sTemp);
		return df.format(d);
	}

	/**
	 * ����ʽת������ͨ��ת��Ϊ��λС��
	 * 
	 * @param d
	 *            ��ͨ��ʽ�Ľ��
	 * @return String�͵Ľ��
	 */
	public static String numberFormat(double d) {
		// ��ѧ���㷨ת��ͨ
		// BigDecimal big = new BigDecimal(d);
		// //System.out.println(big.setScale(2, 2));
		// return big.setScale(2, 2).toString();
		// ����ʽ��
		// DecimalFormat myformat = new DecimalFormat();
		// myformat.applyPattern("##,##0.00");
		// return myformat.format(d);
		int integerDigits = 1;
		int fractionDigits = 2;
		String sTemp = "";
		if (d == 0 && integerDigits == 0) {
			integerDigits = 1;
		}
		if (fractionDigits > 0) {
			sTemp += ".0";
			for (int i = 0; i < fractionDigits - 1; i++) {
				sTemp += "0";
			}
		}
		for (int i = 0; i < integerDigits; i++) {
			sTemp = "0" + sTemp;
		}
		DecimalFormat df = new DecimalFormat(sTemp);
		return df.format(d);
	}

	public static String moneyFormat(double d) {
		DecimalFormat myformat = new DecimalFormat();
		myformat.applyPattern("##,##0.00");
		return myformat.format(d);
	}

	/**
	 * SHA-256����
	 * 
	 * @param sPassWord
	 * @return
	 * @throws Exception
	 */
	public static String SHADes(String sPassWord) throws Exception {
		MessageDigest digest = MessageDigest.getInstance("SHA-256");
		digest.update(sPassWord.getBytes("GBK"));
		return bytes2Hex(digest.digest());
	}

	/**
	 * �������ַ�
	 * 
	 * @param sPassWord
	 * @return
	 * @throws Exception
	 */
	public static String OrigWord(String sPassWord) throws Exception {
		return sPassWord;
	}

	/**
	 * ����
	 * 
	 * @param bts
	 * @return
	 */
	public static String bytes2Hex(byte[] bts) {
		String des = "";
		String tmp = null;
		for (int i = 0; i < bts.length; i++) {
			tmp = (Integer.toHexString(bts[i] & 0xFF));
			if (tmp.length() == 1) {
				des += "0";
			}
			des += tmp;
		}
		return des;
	}

	public static String toString(String s) {
		if (s == null) {
			return "";
		} else {
			return s;
		}
	}

	/**
	 * У���˻��Ƿ��ڶ�����
	 * 
	 * @param userid
	 * @param tuserid
	 * @param useraccount
	 * @param jbo
	 * @throws HandlerException
	 */
	public static void userAccountStatus(String userid, String tuserid) throws HandlerException {

		BizObjectManager manager;
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			manager = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = manager
					.createQuery("select FrozenLockFalg,userid from o  "
							+ "where userid in (:tuserid, :userid)");
			query.setParameter("tuserid", tuserid);// Ͷ�����˻�
			query.setParameter("userid", userid);// ������˻�

			List<BizObject> list = query.getResultList(false);
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);

					if (tuserid.equals(o.getAttribute("userid").getValue() == null ? "": o.getAttribute("userid").toString())) {// Ͷ����
						if ("1".equals(o.getAttribute("FrozenLockFalg").getValue() == null ? "": o.getAttribute("FrozenLockFalg").toString())) {
							throw new HandlerException("account.freeze");
						}

					} else if (userid.equals(o.getAttribute("userid").getValue() == null ? "" : o.getAttribute("userid").toString())) {// �����
						
						if ("1".equals(o.getAttribute("FrozenLockFalg").getValue() == null ? "": o.getAttribute("FrozenLockFalg").toString())) {
							throw new HandlerException("account.freeze");
						}
					}

				}
			}

		} catch (HandlerException he) {
			throw he;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("account.freeze");
		}
	}

	/**
	 * �ļ�ת��Ϊ�ֽ�����
	 * 
	 * @param file
	 * @return
	 */
	public static byte[] getBytesFromFile(File file) {
		FileInputStream in = null;
		ByteArrayOutputStream out = null;
		byte[] ret = null;
		try {
			if (file == null) {
				// log.error("helper:the file is null!");
				return null;
			}
			in = new FileInputStream(file);
			out = new ByteArrayOutputStream(1000);
			byte[] b = new byte[1000];
			int n;
			while ((n = in.read(b)) != -1) {
				out.write(b, 0, n);
			}
			ret = out.toByteArray();
		} catch (IOException e) {
			// log.error("helper:get bytes from file process error!");
			e.printStackTrace();
		} finally{
			if(in != null){
				try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(out != null){
				try {
					out.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return ret;
	}

	// public static byte[] getBytesFromFile(String filePth) throws
	// HandlerException {
	// InputStream is;
	// try {
	// is = new FileInputStream(filePth);
	//
	// // ��ȡ�ļ���С
	//
	// long length = file.length();
	// if (length > Integer.MAX_VALUE) {
	// // �ļ�̫���޷���ȡ
	// throw new HandlerException("File is to large " + file.getName());
	//
	// }
	// // ����һ�������������ļ�����
	// byte[] bytes = new byte[(int) length];
	//
	// // ��ȡ���ݵ�byte������
	// int offset = 0;
	// int numRead = 0;
	// while (offset < bytes.length
	// && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
	// offset += numRead;
	// }
	//
	// // ȷ���������ݾ�����ȡ
	// if (offset < bytes.length) {
	// throw new HandlerException("Could not completely read file "
	// + file.getName());
	// }
	//
	// // Close the input stream and return bytes
	// is.close();
	// return bytes;
	// } catch (Exception e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// throw new HandlerException("");
	// }
	//
	// }

	public static void main(String[] args) {
		GeneralTools tool= new GeneralTools();
		String scode = "1234";
		HashMap<String, Object> hm = new HashMap<String, Object>();
		hm.put("AuthCode", scode);
		boolean f = false;
		try {
			f = tool.sendSMS("P2P_REG", "15121161173", hm);
		} catch (HandlerException e) {
			e.printStackTrace();
		}
		if(f){
			String sql =" insert into phone_msg () value()";
			
		}
	}
}
