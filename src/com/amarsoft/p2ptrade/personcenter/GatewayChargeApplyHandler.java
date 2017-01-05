package com.amarsoft.p2ptrade.personcenter;

import java.text.SimpleDateFormat;
import java.util.Date;
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
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;
import com.amarsoft.p2ptrade.util.RunTradeService;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * �����ֵ���� 
 * ��������� UserID: �˻����
 *  AccountNo: ���п���
 *  AccountName: �˻���
 *  AccountBelong: ������
 *  Amount: ��ֵ��� 
 * ��������� 
 *  ChargeFlag�� �ɹ���ʶ��true/false��
 *  TransSerialNo�� ������ 
 *  LogId����־���
 *  TransURL:��תURL
 * 
 * @author 
 */
public class GatewayChargeApplyHandler extends JSONHandler {
	public static final String TRANS_CODE_CHARGE_DELAY = "1012";

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userID = (String) request.get("UserID");
		String sSerialNo = (String) request.get("SerialNo");
//		String accountNo = (String) request.get("AccountNo");
//		String accountName = (String) request.get("AccountName");
//		String accountBelong = (String) request.get("AccountBelong");

		// ����У��
		if (userID == null || userID.length() == 0)
			throw new HandlerException("common.emptyuserid");
//		if (accountNo == null || accountNo.length() == 0)
//			throw new HandlerException("accountno.error");
//		if (accountName == null || accountName.length() == 0)
//			throw new HandlerException("accountname.error");
//		if (accountBelong == null || accountBelong.length() == 0)
//			throw new HandlerException("accountbelong.error");

		double amount = 0d;
		try {
			amount = Double.parseDouble((String) request.get("Amount"));
		} catch (NumberFormatException e2) {
			throw new HandlerException("chargeapply.amount.error");
		}
		if (amount <= 0) {
			throw new HandlerException("chargeapply.amount.error");
		}

		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();

		//String serialNo = null, accountBelongName = null;
		try {
			// ��ȡ��ǰʱ��
			TimeTool tool = new TimeTool();
			
			String sCurrentTime = tool.getsCurrentTime();// ��ȡ��ǰʱ�䣬��ʽHH:mm:ss
			JSONObject timeArray = GeneralTools.getChargeDividTime(jbo, "0020");
			String startTime = timeArray.get("StartTime").toString();
			String endTime = timeArray.get("EndTime").toString();
			
			
			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
			Date d1 = sdf.parse(sCurrentTime);
			Date d2 = sdf.parse(startTime);
			Date d3 = sdf.parse(endTime);
			long diff1 = d1.getTime() - d2.getTime();
			long diff2 = d1.getTime() - d3.getTime();
			if (!(diff1 < 0 && diff2 > 0)) {// 23��55 ~ 01:05
				throw new HandlerException("out.bankchargetime.error");
			}

			// �����޶�У��
			double chargeAllAmount = getChargeCountByDate(jbo, userID,
					tool.getsCurrentDate());
			if (chargeAllAmount + amount >= 1000000) {
				throw new HandlerException("charge.limitallamount.error");
			}
			
//			//��ȡ���ƽ��
//			double limitAmount = GeneralTools.getLimitAmount(jbo, accountBelong, "ATTRIBUTE7");//��ȡ�����޶�
//			if(amount > limitAmount){
//				throw new HandlerException("charge.limitamount.error");
//			}
			

//			BizObjectManager accountInfoManager = jbo
//					.getManager("jbo.trade.account_info");
//			BizObjectQuery query = accountInfoManager
//					.createQuery("userid=:userid and accountno=:accountno "
//							+ "and accountbelong=:accountbelong  and status='2'");
//			query.setParameter("userid", userID);
//			query.setParameter("accountno", accountNo);
//			query.setParameter("accountbelong", accountBelong);
//			//query.setParameter("accountname", accountName);
//
//			BizObject accountInfo = query.getSingleResult(false);
//			if (accountInfo != null) {
//				serialNo = accountInfo.getAttribute("SERIALNO").toString();
//				JSONObject items = GeneralTools.getItemName(jbo, "BankNo");
//				accountBelongName = items.containsKey(accountBelong) ? items
//						.get(accountBelong).toString() : accountBelong;
//			} else {
//				throw new HandlerException("queryaccountinfo.nodata.error");
//			}
			BizObjectManager recordManager = jbo
					.getManager("jbo.trade.transaction_record");
			BizObject recordBo = recordManager.newObject();

			recordBo.setAttributeValue("USERID", userID);// �û����
			recordBo.setAttributeValue("DIRECTION", "R");// ��������(����)
			recordBo.setAttributeValue("AMOUNT", amount);// ���׽��
			recordBo.setAttributeValue("HANDLCHARGE", 0);// ������
			recordBo.setAttributeValue("TRANSTYPE", TRANS_CODE_CHARGE_DELAY);// �������ͣ����س�ֵ��
			recordBo.setAttributeValue("INPUTTIME",StringFunction.getTodayNow());// ����ʱ��
			recordBo.setAttributeValue("RELAACCOUNT", sSerialNo);// �����˻���ˮ��
			recordBo.setAttributeValue("RELAACCOUNTTYPE", "001");// ���׹����˻�����
			recordBo.setAttributeValue("TRANSCHANNEL", "1020");// ��������(����)
			recordBo.setAttributeValue("REMARK", "��ֵ");//+ "|" + accountBelongName);// ��ע
			recordBo.setAttributeValue("STATUS", "00");// 
			recordManager.saveObject(recordBo);
			String transSerialNo = recordBo.getAttribute("SERIALNO").toString();
			
			//����P2P_Trade����
//			String sMethod = "runrealtimetrans";
//			String sRequestFormat = "json";
			
			JSONObject obj = new JSONObject();
			for (int j = 0; j < recordBo.getAttributeNumber(); j++) {
				obj.put(recordBo.getAttribute(j).getName().toUpperCase(), recordBo.getAttribute(j)
						.getValue());
			}
			String sRequestStr = GeneralTools.createJsonString(obj);
			
//			JSONObject responsePram = RunTradeService.runTranProcess(sMethod, sRequestFormat, sRequestStr);
//			String sReturnCode = responsePram.get("TransFlag") == null ? "":responsePram.get("TransFlag").toString();
//			String sLogId = responsePram.get("LogId") == null ? "":responsePram.get("LogId").toString();
//			String sTransChannel = responsePram.get("TransChannel") == null ? "":responsePram.get("TransChannel").toString();
			
			String channel = String.valueOf(obj.get("TRANSCHANNEL"));
			String transCode = String.valueOf(obj.get("TRANSTYPE"));
			RealTimeTradeTranscation rttt = TranscationFactory
					.getRealTimeTradeTranscation(channel, transCode);
			rttt.init(obj);
			rttt.execute();
			String sLogId = rttt.getLogId();
			String sTransUrl = null;
			
			if(rttt.getTemplet().isSuccess()){
//				sTransUrl = responsePram.get("TransURL") == null ? "":responsePram.get("TransURL").toString();
				
				if(rttt.getReponseMessage().getSingleMessage("INFO")!=null){
					com.amarsoft.message.Message infoMessage = rttt.getReponseMessage().getSingleMessage("INFO");
					if(infoMessage.getField("URL")!=null)
						sTransUrl = infoMessage.getField("URL").getStringValue();
				}
				
				recordBo.setAttributeValue("STATUS", "01");// ���ύ��������
				recordBo.setAttributeValue("TRANSLOGID", sLogId);// 
				recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
				recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
				recordManager.saveObject(recordBo);
				result.put("TransURL", sTransUrl);
				result.put("ChargeFlag", true);
				result.put("TransSerialNo", transSerialNo);
				result.put("LogId", sLogId);
			}else{
				recordBo.setAttributeValue("STATUS", "04");// ��ʧЧ
				recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
				recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
				recordManager.saveObject(recordBo);
				result.put("ChargeFlag", false);
				result.put("isSUCCESS", false);
			}
//			result.put("TransSerialNo", transSerialNo);
//			result.put("LogId", sLogId);
			//result.put("TransChannel", sTransChannel);
			return result;
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("chargeapply.error");
		}
	}

	/**
	 * ��ȡ�����ѽ��г�ֵ���׵Ĵ���
	 * 
	 * @param jbo
	 *            JBOFactory
	 * @param sUserID
	 *            �û����
	 * @param transdate
	 *            ��ǰ����
	 * @return �ѽ��г�ֵ���׵��ܽ��
	 * @throws HandlerException
	 */
	private double getChargeCountByDate(JBOFactory jbo, String sUserID,
			String transdate) throws HandlerException {
		double amount = 0;
		try {
			BizObjectManager manager;
			manager = jbo.getManager("jbo.trade.transaction_record");
			BizObjectQuery query = manager
					.createQuery("select sum(o.AMOUNT) as v.amount from o where userid=:userid and transdate=:transdate and transtype in (:transtype1,:transtype2,:transtype3) and status<>'04'");
			query.setParameter("userid", sUserID)
					.setParameter("transdate", transdate)
					.setParameter("transtype1", "1010")
					.setParameter("transtype2", "1011")
					.setParameter("transtype3", "1012");
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				amount = Double
						.parseDouble(o.getAttribute("amount").toString() == null ? "0.0"
								: o.getAttribute("amount").toString());
			} else {
				amount = 0;
			}
			return amount;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getchargeallamount.error");
		}
	}

}
