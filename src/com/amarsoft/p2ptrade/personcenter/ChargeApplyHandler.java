package com.amarsoft.p2ptrade.personcenter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.account.common.ObjectBalanceUtils;
import com.amarsoft.account.common.ObjectConstants;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2p.interfaces.utils.InterfaceHelper;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;
import com.amarsoft.p2ptrade.util.RunTradeService;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * �����ֵ���� 
 * ��������� 
 * 			UserID:�˻���� 
 * 			SerialNo:��ˮ��
 * 			Amount:��ֵ��� 
 * ��������� ChargeFlag����ֵ�Ƿ�ɹ� (true-->�ɹ� false-->ʧ��)
 * 			Amount����ֵ���
 * 
 */
public class ChargeApplyHandler extends JSONHandler {
	public static final String TRANS_CODE_CHARGE_DELAY = "1012";
	ArrayList<BizObject> recordBizList = null;
	ArrayList<BizObjectManager> managerList = null;

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return runChargeApply(request);
	}

	/**
	 * �����ֵ
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject runChargeApply(JSONObject request)
			throws HandlerException {
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		if (request.get("SerialNo") == null || "".equals(request.get("SerialNo")))
			throw new HandlerException("param.emptyserialno.error");
		if (request.get("Amount") == null || "".equals(request.get("Amount")))
			throw new HandlerException("param.emptyamount.error");
		double amount = 0d;
		try {
			amount = Double.parseDouble(request.get("Amount").toString());
		} catch (NumberFormatException e2) {
			throw new HandlerException("param.formatamount.error");
		}
		if (amount <= 0) {
			throw new HandlerException("param.limitamount.error");
		}

		String sUserID = request.get("UserID").toString();
		String sSerialNo = request.get("SerialNo").toString();
		
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
		JBOTransaction tx = null;
		boolean savaRecordFlag = false;
		try {
			tx = jbo.createTransaction();

			// ��ȡ��ǰʱ��
			TimeTool tool = new TimeTool();

//			// �����޶�У��
//			double chargeAllAmount = getChargeCountByDate(jbo, sUserID,
//					tool.getsCurrentDate());
//			if (chargeAllAmount + amount >= 1000000) {
//				result.put("isSUCCESS", false);
//				result.put("ChargeFlag",false);
//				result.put("ChargeResultFlag", "limitallamount.notenough");
//				//throw new HandlerException("charge.limitallamount.error");
//				return result;
//			}

			// ��ȡ�û���ֵ���п���Ϣ
			String sAccountBelong = sSerialNo;//getAccountBelong(jbo, sSerialNo);

			String sTranType = null;
			String sStatus = null;
//			String sCurrentTime = tool.getsCurrentTime();// ��ȡ��ǰʱ�䣬��ʽHH:mm:ss
//			JSONObject timeArray = GeneralTools.getChargeDividTime(jbo, "0010");
//			String startTime = timeArray.get("StartTime").toString();
//			String endTime = timeArray.get("EndTime").toString();
//			
//			
//			SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
//			Date d1 = sdf.parse(sCurrentTime);
//			Date d2 = sdf.parse(startTime);
//			Date d3 = sdf.parse(endTime);
//			long diff1 = d1.getTime() - d2.getTime();
//			long diff2 = d1.getTime() - d3.getTime();
//			if (!(diff1 < 0 && diff2 > 0)) {// 23��55 ~ 01:05
//				throw new HandlerException("out.bankchargetime.error");
//			}

			// ��ֳ�ֵ���
/*********��ʼ************/
//			double limitAmount = GeneralTools.getLimitAmount(jbo,
//					sAccountBelong, "ATTRIBUTE7");// ��ȡ�����޶�
//			recordBizList = new ArrayList<BizObject>();
//			managerList = new ArrayList<BizObjectManager>();
//			
//			savaRecordFlag = getAmountList(jbo, tx, sUserID,
//					sAccountBelong, sSerialNo, sTranType, sStatus, amount,
//					limitAmount);
//			tx.commit();
//
//			for (int i = 0; i < recordBizList.size(); i++) {
//				if (diff1 < 0 && diff2 > 0) {// 01:00 ~ 23��00 ��ʵʱ�ӿڳ�ֵ
//					BizObjectManager recordManager = managerList.get(i);
//					BizObject recordBo = recordBizList.get(i);
//					String transSerialNo = recordBo.getAttribute("SERIALNO").toString();
//					double transAmount = recordBo.getAttribute("AMOUNT").getDouble();
//					double handlCharge = recordBo.getAttribute("HANDLCHARGE").getDouble();
//					double usableBalance = 0;// �˻��������
//					double frozenBalance = 0;// �˻�������
//					double userBalance = 0;// �˻����
//					double reChargeAmount = 0;// ��ֵ�ɹ�����û��˻����
//					double reUsableBalance = 0;// ��ֵ�ɹ�����˻��������
//					
//					// �˻���Ϣ�������
//					BizObjectManager accountManager = jbo.getManager(
//							"jbo.trade.user_account", tx);
//					BizObject accountBo = accountManager
//							.createQuery("userid=:userid")
//							.setParameter("userid", sUserID)
//							.getSingleResult(true);
//					// ��ȡ�û������Ϣ
//					if (accountBo != null) {
//						accountBo.setAttributeValue("LOCKFLAG", "1");
//						accountManager.saveObject(accountBo);
//
//						usableBalance = Double.parseDouble(accountBo
//								.getAttribute("USABLEBALANCE").toString());
//						frozenBalance = Double.parseDouble(accountBo
//								.getAttribute("FROZENBALANCE").toString());
//
//						userBalance = usableBalance + frozenBalance;// �˻����
//						reUsableBalance = usableBalance + transAmount
//								- handlCharge;// ��ֵ�ɹ����˻��������
//						reChargeAmount = userBalance + transAmount
//								- handlCharge;// ��ֵ�ɹ�����û��˻����
//					} else {
//						throw new HandlerException("quaryaccountamount.nodata.error");
//					}
//					JSONObject obj = new JSONObject();
//					for (int j = 0; j < recordBo.getAttributeNumber(); j++) {
//						obj.put(recordBo.getAttribute(j).getName()
//								.toUpperCase(), recordBo.getAttribute(j)
//								.getValue());
//					}
//					String channel = String.valueOf(obj.get("TRANSCHANNEL"));
//					String transCode = String.valueOf(obj.get("TRANSTYPE"));
//					RealTimeTradeTranscation rttt = TranscationFactory
//							.getRealTimeTradeTranscation(channel, transCode);
//					rttt.init(obj);
//					rttt.execute();
//					String sLogId = rttt.getLogId();
//					String sTransUrl = null;
//					if (rttt.getTemplet().isSuccess()) {//�ɹ�
//						if(rttt.getReponseMessage().getSingleMessage("INFO")!=null){
//							com.amarsoft.message.Message infoMessage = rttt.getReponseMessage().getSingleMessage("INFO");
//							if(infoMessage.getField("URL")!=null)
//								sTransUrl = infoMessage.getField("URL").getStringValue();
//						}
//						recordBo.setAttributeValue("STATUS", "01");// ���ύ��������
//						recordBo.setAttributeValue("TRANSLOGID", sLogId);// 
//						recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
//						recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
//						recordManager.saveObject(recordBo);
//						result.put("TransURL", sTransUrl);
//						result.put("ChargeFlag", true);
//						result.put("TransSerialNo", transSerialNo);
//						result.put("LogId", sLogId);
//						result.put("isSUCCESS", true);
//					} else {// ʧ��
//						recordBo.setAttributeValue("STATUS", "04");// ��ʧЧ
//						recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
//						recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
//						recordManager.saveObject(recordBo);
//						result.put("ChargeFlag", false);
//						result.put("isSUCCESS", false);
//					}
//				} 
//				tx.commit();
//			}
/*********����************/
			//���������
			/******��ʼ********/
			BizObjectManager recordManager = jbo.getManager("jbo.trade.transaction_record", tx);
			BizObject recordBo = saveTransRecord(jbo, recordManager,sUserID, amount,
					sAccountBelong, sSerialNo,sTranType, sStatus);
			String transSerialNo = recordBo.getAttribute("SERIALNO").toString();
			double transAmount = recordBo.getAttribute("AMOUNT").getDouble();
			double handlCharge = recordBo.getAttribute("HANDLCHARGE").getDouble();
			double usableBalance = 0;// �˻��������
			double frozenBalance = 0;// �˻�������
			double userBalance = 0;// �˻����
			double reChargeAmount = 0;// ��ֵ�ɹ�����û��˻����
			double reUsableBalance = 0;// ��ֵ�ɹ�����˻��������
			
			// �˻���Ϣ�������
			BizObjectManager accountManager = jbo.getManager(
					"jbo.trade.user_account", tx);
			BizObject accountBo = accountManager
					.createQuery("userid=:userid")
					.setParameter("userid", sUserID)
					.getSingleResult(true);
			// ��ȡ�û������Ϣ
			if (accountBo != null) {
//				accountBo.setAttributeValue("LOCKFLAG", "1");
//				accountManager.saveObject(accountBo);

				/*usableBalance = Double.parseDouble(accountBo
						.getAttribute("USABLEBALANCE").toString());
				frozenBalance = Double.parseDouble(accountBo
						.getAttribute("FROZENBALANCE").toString());
				*/
				//��ȡ�ͻ��������е���� modify by xjqin 20150120
				HashMap<String,Double> balances = ObjectBalanceUtils.ObjectBalanceUtils(sUserID, ObjectConstants.OBJECT_TYPE_001);
				if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_001))
					usableBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_001); //��ѯ�������
				
				if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_002))
					frozenBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_002); //��ѯ�������
				
				//modify end
				
				
				userBalance = usableBalance + frozenBalance;// �˻����
				reUsableBalance = usableBalance + transAmount
						- handlCharge;// ��ֵ�ɹ����˻��������
				reChargeAmount = userBalance + transAmount
						- handlCharge;// ��ֵ�ɹ�����û��˻����
			} else {
				throw new HandlerException("quaryaccountamount.nodata.error");
			}
			JSONObject obj = new JSONObject();
			for (int j = 0; j < recordBo.getAttributeNumber(); j++) {
				obj.put(recordBo.getAttribute(j).getName()
						.toUpperCase(), recordBo.getAttribute(j)
						.getValue());
			}
			/**��ֵ���к�*/
			obj.remove("RELAACCOUNT");
			obj.put("BANKNO", sSerialNo);
			String channel = String.valueOf(obj.get("TRANSCHANNEL"));
			String transCode = String.valueOf(obj.get("TRANSTYPE"));
			RealTimeTradeTranscation rttt = TranscationFactory
					.getRealTimeTradeTranscation(channel, transCode);
			rttt.init(obj);
			rttt.execute();
			String sLogId = rttt.getLogId();
			String sTransUrl = null;
			if (rttt.getTemplet().isSuccess()) {//�ɹ�
				sTransUrl = (String)InterfaceHelper.getFieldValue(rttt.getReponseMessage(), "URL","");
//				if(rttt.getReponseMessage().getSingleMessage("INFO")!=null){
//					com.amarsoft.message.Message infoMessage = rttt.getReponseMessage().getSingleMessage("INFO");
//					if(infoMessage.getField("URL")!=null)
//						sTransUrl = infoMessage.getField("URL").getStringValue();
//				}
				recordBo.setAttributeValue("STATUS", "01");// ���ύ��������
				recordBo.setAttributeValue("TRANSLOGID", sLogId);// 
				recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
				recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
				recordManager.saveObject(recordBo);
				result.put("TransURL", sTransUrl);
				result.put("ChargeFlag", true);
				result.put("TransSerialNo", transSerialNo);
				result.put("LogId", sLogId);
				result.put("isSUCCESS", true);
			} else {// ʧ��
				recordBo.setAttributeValue("STATUS", "04");// ��ʧЧ
				recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
				recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
				recordManager.saveObject(recordBo);
				result.put("isSUCCESS", false);
			}
			tx.commit();
			/*******����*******/
		} catch (HandlerException e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			if (!savaRecordFlag) {
				throw e;
			}
		} catch (Exception e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			if (!savaRecordFlag) {
				throw new HandlerException("chargeapply.error");
			}
		}
		return result;
	}

	/**
	 * ��ѯ�˻���Ϣ
	 * @param jbo
	 *            JBOFactory
	 * @param sSerialNo
	 *             ��ˮ��
	 * @return
	 * @throws HandlerException
	 */
	private String getAccountBelong(JBOFactory jbo, String sSerialNo)
			throws HandlerException {
		try {
			BizObjectManager m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery("serialno = :serialno");
			query.setParameter("serialno", sSerialNo);

			BizObject o = query.getSingleResult(false);
			if (o != null) {
				String sAccountBelong = o.getAttribute("ACCOUNTBELONG").toString();
				return sAccountBelong;
			} else {
				throw new HandlerException("queryaccountinfo.nodata.error");
			}
		} catch (HandlerException e) {
			throw e;
		} catch (Exception e) {
			throw new HandlerException("queryaccountinfo.error");
		}
	}

	/**
	 * ���潻�׼�¼
	 * @param recordManager
	 *            BizObjectManager
	 * @param recordBo
	 *            BizObject
	 * @param sUserID
	 *            �û����
	 * @param mount
	 *            ��ֵ���
	 * @param infoObj
	 *            �˻���Ϣ����
	 * @param sTranType
	 *            ��������
	 * @param sStatus
	 *            ����״̬
	 * @param tool
	 *            ʱ�乤����
	 * @return
	 * @throws HandlerException
	 */
	private BizObject saveTransRecord(JBOFactory jbo,
			BizObjectManager recordManager, String sUserID,
			double amount, String sAccountBelong,
			String sSerialNo, String sTranType, String sStatus)
			throws HandlerException {
		try {
			BizObjectQuery q = recordManager.createQuery("RELAACCOUNT=:RELAACCOUNT").setParameter("RELAACCOUNT", sSerialNo);
			BizObject recordBo = q.getSingleResult(true);
			
			recordBo = recordManager.newObject();
			recordBo.setAttributeValue("USERID", sUserID);// �û����
			recordBo.setAttributeValue("DIRECTION", "R");// ��������(����)
			recordBo.setAttributeValue("AMOUNT", amount);// ���׽��

			double handlCharge = GeneralTools.getCalTransFee(jbo, "0020",
					amount);// ����������

			recordBo.setAttributeValue("HANDLCHARGE", handlCharge);// ������
			
			recordBo.setAttributeValue("TRANSTYPE", TRANS_CODE_CHARGE_DELAY);// �������ͣ���ֵ,�ӳٳ�ֵ��
			recordBo.setAttributeValue("INPUTTIME",
					new TimeTool().getsCurrentMoment());// ����ʱ��
			recordBo.setAttributeValue("STATUS", "00");// ����״̬
			recordBo.setAttributeValue("RELAACCOUNT", sSerialNo);// �����˻���ˮ��
			recordBo.setAttributeValue("RELAACCOUNTTYPE", "001");// ���׹����˻�����

			String sTransChannel = GeneralTools.getTransChannel(jbo,
					sAccountBelong, "ATTRIBUTE1");


			recordBo.setAttributeValue("TRANSCHANNEL", sTransChannel);// ��������
			recordBo.setAttributeValue("REMARK",
					"��ֵ");// + "|" + infoObj.get("ACCOUNTBELONGNAME").toString());// ��ע
			recordManager.saveObject(recordBo);

			return recordBo;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("savetransrecord.error");
		}
	}

	/**
	 * ��ȡ�ֻ�����
	 * 
	 * @param accountBo
	 * @throws HandlerException
	 */
	private String getPhoneTel(BizObject accountBo) throws HandlerException {
		try {
			return accountBo.getAttribute("PHONETEL").toString();
		} catch (Exception e) {
			// throw new HandlerException("quaryphonetel.error");
			return "";
		}
	}

	/**
	 * ��ȡ�����ѽ������ֽ��׵Ĵ���
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

	/**
	 * �����޶�ȥ��֣��緢�����>�޶�,���������²�֣���ֽ�������ArrayList�з���
	 * 
	 * @return ArrayList<Double>
	 * @throws HandlerException
	 * @throws JBOException 
	 * */
	private final boolean getAmountList(JBOFactory jbo,JBOTransaction tx
			, String sUserID,
			String sAccountBelong, String sSerialNo,
			String sTranType, String sStatus, double amount,
			double limitAmount) throws HandlerException, JBOException {
		// ����޶����0 ����Ĭ��Ϊ������
		if (amount <= limitAmount || limitAmount == 0) {
			BizObjectManager recordManager = jbo.getManager(
					"jbo.trade.transaction_record", tx);
			BizObject recordBo = saveTransRecord(jbo, recordManager,
					sUserID, amount, sAccountBelong, sSerialNo,
					sTranType, sStatus);
			managerList.add(recordManager);
			recordBizList.add(recordBo);
		} else {
			double dTemp = amount;
			while (dTemp > limitAmount) {
				dTemp -= limitAmount;
				BizObjectManager recordManager = jbo.getManager(
						"jbo.trade.transaction_record", tx);
				BizObject recordBo = saveTransRecord(jbo, recordManager,
						sUserID, limitAmount, sAccountBelong,
						sSerialNo, sTranType, sStatus);
				managerList.add(recordManager);
				recordBizList.add(recordBo);
			}

			if (dTemp > 0) {
				BizObjectManager recordManager = jbo.getManager(
						"jbo.trade.transaction_record", tx);
				BizObject recordBo = saveTransRecord(jbo, recordManager,
						sUserID, dTemp, sAccountBelong, sSerialNo,
						sTranType, sStatus);
				managerList.add(recordManager);
				recordBizList.add(recordBo);
			}
		}
		return true;
	}
}
