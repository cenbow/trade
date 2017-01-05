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
 * ���֧������
 * ��������� 
 * 			UserID:�˻���� 
 * 			Amount:��ֵ��� 
 * ��������� ChargeFlag����ֵ�Ƿ�ɹ� (true-->�ɹ� false-->ʧ��)
 * 			Amount����ֵ���
 * 
 */
public class OnceChargeApplyHandler extends JSONHandler {
	public static final String TRANS_CODE_CHARGE_DELAY = "1013";
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
		
		
		
		
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
		JBOTransaction tx = null;
		boolean savaRecordFlag = false;
		try {
			tx = jbo.createTransaction();
			
			BizObject accountBiz = this.getAccountInfo(jbo, sUserID);
			//��ȡ�˻���Ϣ
			String sSerialNo = accountBiz.getAttribute("SERIALNO").toString();
			String sAccountBelong = accountBiz.getAttribute("ACCOUNTBELONG").toString();
			
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


			String sTranType = null;
			String sStatus = null;
			//���������
			/******��ʼ********/
			BizObjectManager recordManager = jbo.getManager("jbo.trade.transaction_record", tx);
			BizObject recordBo = saveTransRecord(jbo, recordManager,sUserID, amount,
					sAccountBelong, sSerialNo,sTranType, sStatus);
			String transSerialNo = recordBo.getAttribute("SERIALNO").toString();
			
			JSONObject obj = new JSONObject();
			for (int j = 0; j < recordBo.getAttributeNumber(); j++) {
				obj.put(recordBo.getAttribute(j).getName()
						.toUpperCase(), recordBo.getAttribute(j)
						.getValue());
			}
			/**��ֵ���к�*/
			String channel = String.valueOf(obj.get("TRANSCHANNEL"));
			String transCode = String.valueOf(obj.get("TRANSTYPE"));
			RealTimeTradeTranscation rttt = TranscationFactory
					.getRealTimeTradeTranscation(channel, transCode);
			rttt.init(obj);
			rttt.execute();
			String sLogId = rttt.getLogId();
			String sTransUrl = null;
			if (rttt.getTemplet().isSuccess()) {//�ɹ�
				String status = "10";
				String is_verified = "false" ; //�Ƿ���Ҫ�ƽ���Ĭ��Ϊ��
				//����ticket,��Ҫ�ƽ�
				String ticket = (String)InterfaceHelper.getFieldValue(rttt.getReponseMessage(), "ticket",""); 
				if(InterfaceHelper.isNotNull(ticket))
				{
					is_verified = "true";
					//TODO 
					status = "01"; //�ƽ���״̬������Ӧ�õ�����һ���ƽ���״̬����������������Ӧ��15����ʧЧ�Ļ���
				}
				result.put("is_verified", is_verified);
				
				recordBo.setAttributeValue("STATUS", "01");// ���ύ��������
				recordBo.setAttributeValue("TRANSLOGID", sLogId);// 
				recordBo.setAttributeValue("TRANSDATE",StringFunction.getToday());
				recordBo.setAttributeValue("TRANSTIME",StringFunction.getNow());
				recordManager.saveObject(recordBo);
				result.put("ChargeFlag", true);
				result.put("TransSerialNo", transSerialNo);
				result.put("LogId", sLogId);
				result.put("isSUCCESS", true);
			} else {// ʧ��
				//������ڴ�������Ҫ������ѯ�����ñ�־Ϊ03
				if(rttt.isProcessed())
				{
					recordBo.setAttributeValue("STATUS", "03");// ���ڴ�����
				}
				else
				{
					recordBo.setAttributeValue("STATUS", "04");// ��ʧЧ
				}
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
	private BizObject getAccountInfo(JBOFactory jbo, String sUserId)
			throws HandlerException {
		try {
			BizObjectManager m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery(" userId=:userId and status=:status");
			query.setParameter("userId", sUserId);
			query.setParameter("status", "2");
			BizObject o = query.getSingleResult(false);
			if(o==null) throw new HandlerException("queryaccountinfo.error");
			return o;
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

			//TODO Ҫ�Ҹ��ط��ú�����
			String sTransChannel = "3010";
//			String sTransChannel = GeneralTools.getTransChannel(jbo,
//					sAccountBelong, "ATTRIBUTE1");


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
