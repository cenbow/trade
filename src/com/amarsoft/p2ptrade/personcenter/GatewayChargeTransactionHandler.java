package com.amarsoft.p2ptrade.personcenter;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.message.Message;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.invest.P2pString;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * ��ֵ���� 
 * ��������� 
 * 			TransSerialNo��			������	
 * 			PayResult��				������ (1-->�ɹ�	0-->ʧ��)
 * 			PayAmount��				ʵ��֧�����
 * 			LogId:                  ��ֵʱ��ȡ����־���
 * ��������� 
 * 			ChargeFlag��				�����Ƿ�ɹ�  (true-->�ɹ�  false-->ʧ��)
 * 			SendSMSFlag��			���Ͷ��������Ƿ�ɹ�  (true-->�ɹ�  false-->ʧ��)
 * 			payAmount                  ʵ��֧�����
 */
public class GatewayChargeTransactionHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
		
		String logId = String.valueOf(request.get("outer_trade_no"));
		JBOTransaction tx = null;
		
		String channelId = "";
		String userId = "";
		try {
			tx = jbo.createTransaction();
			
			Date d = new Date();
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd"); 
			SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss"); 
			String time = P2pString.addDateFormat(sdf1.format(d), 1, -30,"HH:mm:ss");
			BizObjectManager transManager = jbo.getManager("jbo.trade.transaction_record",tx);
			BizObjectQuery transquery = transManager.createQuery("TRANSLOGID=:TRANSLOGID and TransDate=:date and TransTime >:time");
			transquery.setParameter("TRANSLOGID",logId).setParameter("date", sdf.format(d)).setParameter("time", time);
			BizObject transaction = transquery.getSingleResult(true);
			if(transaction == null){
				throw new HandlerException("common.trans.transactionnotexists");
			}
			else
			{
				channelId = transaction.getAttribute("TRANSCHANNEL").toString();
				userId = transaction.getAttribute("userid").toString();
			}
			
			
			RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation(channelId, "ChargeCallBack");
			rttt.init(request);
			rttt.execute();
			
			boolean cResult = false;
			if(rttt.getTemplet()!=null)
			{
				if(rttt.getTemplet().isSuccess())
				{
					cResult = true;
				}
			}
			
			BizObjectManager userManager = jbo.getManager("jbo.trade.user_account",tx);
			BizObjectQuery query = userManager.createQuery("UserID=:UserID");
			query.setParameter("UserID",userId);
			BizObject user = query.getSingleResult(false);
			//��ȡ���
			double amount = Double.parseDouble(String.valueOf(request.get("deposit_amount")));
			TimeTool tool = new TimeTool();
			HashMap<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("Amount", DataConvert.toMoney(amount));
			parameters.put("Date", tool.getsChDate());
			
			String mobile = user.getAttribute("PHONETEL").toString();
			if(mobile != null && !"".equals(mobile)){
				// ���Ͷ�������
				boolean sSendResult = GeneralTools.sendSMS("1".equals(cResult)?"P2P_CZCG":"P2P_CZSB",mobile, parameters);
				if (sSendResult) {
					result.put("SendSMSFlag", true);
				} else {
					result.put("SendSMSFlag", false);
				}	
			}
			result.put("payAmount",amount);
			return result;
		}catch(HandlerException e){
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			throw e;
		}catch(Exception e){
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new HandlerException("runcharge.error");
		}
	}
	
	
	/**
	 * ��ȡMessage�е���Ϣֵ
	 * */
	private final String get(Message responseMessage,String fieldName) throws Exception
	{
		if(responseMessage.getField(fieldName)!=null)
			return responseMessage.getField(fieldName).getStringValue();
		else
			return "";
		
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
}
