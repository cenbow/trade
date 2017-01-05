package com.amarsoft.p2ptrade.personcenter;


import java.util.Iterator;
import java.util.List;
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
import com.amarsoft.p2p.interfaces.utils.InterfaceHelper;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;
import com.amarsoft.transcation.RealTimeTradeTranscation;
import com.amarsoft.transcation.TranscationFactory;

/**
 * ����󿨽��� ��������� UserID:�˻���� AccountNo:���п��� AccountName:�˻��� AccountBelong:������
 * BelongArea:�������� ��������� �ɹ���ʶ BindFlag���Ƿ������ɹ� true-->�ɹ� false-->ʧ��
 * 
 * 
 */
public class BindingCardApplyHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return runBindingCardApply(request);
	}

	/**
	 * �����
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject runBindingCardApply(JSONObject request)
			throws HandlerException {
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");
		if (request.get("AccountNo") == null
				|| "".equals(request.get("AccountNo")))
			throw new HandlerException("accountno.error");
		if (request.get("AccountBelong") == null
				|| "".equals(request.get("AccountBelong")))
			throw new HandlerException("accountbelong.error");
		if (request.get("AccountName") == null
				|| "".equals(request.get("AccountName")))
			throw new HandlerException("accountname.error");

		if (request.get("province") == null
				|| "".equals(request.get("province")))
			throw new HandlerException("province.error");
		
		if (request.get("city") == null
				|| "".equals(request.get("city")))
			throw new HandlerException("city.error");
		
		String sUserID = request.get("UserID").toString();
		String sAccountNo = request.get("AccountNo").toString();
		String sCertID = (String)request.get("CertID");
		if(sCertID==null)sCertID="";
		String sAccountBelong = request.get("AccountBelong").toString();
		String sisReturnCard = (String)request.get("isReturnCard");
		String sAccountName = request.get("AccountName").toString();
		String province = request.get("province").toString();
		String city = request.get("city").toString();
		String phoneno = request.get("phoneno").toString();
		JSONObject result = new JSONObject();

		JBOFactory jbo = JBOFactory.getFactory();
		BizObjectManager accountManager = null;
		BizObject accountBo = null;
		//String sAccountName = getRealName(jbo,sUserID);
		try {
			CheckNeedBindBankHandler h0 = new CheckNeedBindBankHandler();
			JSONObject request0 = new JSONObject();
			request0.put("UserID", request.get("UserID"));
			request0.put("AccountNo", request.get("AccountNo"));
			request0.put("AccountName", request.get("AccountName"));
			request0.put("AccountBelong", request.get("AccountBelong"));
			request0.put("province", request.get("province"));
			request0.put("city", request.get("city"));
			JSONObject r0 = (JSONObject) h0.createResponse(request0, null);
			String againFlag = r0.get("againFlag").toString();

			// ��ȡ��ǰʱ��
			TimeTool tool = new TimeTool();
			String sInputTime = tool.getsCurrentMoment();
			//String againFlag = "true";
			if ("true".equals(againFlag)) {
				if(!sCertID.equals("")){
				//δʵ����֤��ֹ��	
					throw new HandlerException("queryuseraccount.nodata.error");				
				}

				// �ı�ԭ���Ѱ����п���״̬-->ʧЧ
				//changeOriginalCardStatus(jbo, sUserID);

				// �ʺ���Ϣ�������
				accountManager = jbo
						.getManager("jbo.trade.account_info");
				accountBo = accountManager.newObject();

				accountBo.setAttributeValue("USERID", sUserID);// �û����
				accountBo.setAttributeValue("ACCOUNTTYPE", "001");// �˻�����
				accountBo.setAttributeValue("ACCOUNTNO", sAccountNo);// �˻���
				accountBo.setAttributeValue("ACCOUNTNAME", sAccountName);// �˻���
				accountBo.setAttributeValue("ACCOUNTBELONG", sAccountBelong);// ��������
				//accountBo.setAttributeValue("BELONGAREA", sBelongArea);// ��������
				accountBo.setAttributeValue("STATUS", "1");// �Ƿ���֤(��֤��)
				accountBo.setAttributeValue("ISRETURNCARD", sisReturnCard);// �Ƿ񻹿
				accountBo.setAttributeValue("CHECKNUMBER", "0");// ��У�����
				accountBo.setAttributeValue("LIMITAMOUNT", 200000);// �޶�
				accountBo.setAttributeValue("INPUTTIME", sInputTime);// ����ʱ��
				accountBo.setAttributeValue("PHONENO", phoneno);// ����ʱ��
				accountBo.setAttributeValue("province", province);// ����ʱ��
				accountBo.setAttributeValue("city", city);// ����ʱ��
				accountManager.saveObject(accountBo);
				String sAccountSerialNo = accountBo.getAttribute("SERIALNO")
						.toString();
				// �����
				double amount = 0;

				String sStatus = null;
				String sTransType = null;
				
				sTransType = "1070";
				/*
				if (!("0302".equals(sAccountBelong))) {// ������������
					sStatus = "00";// ����״̬
					sTransType = "1070";
				} else {
					sStatus = "01";// ����״̬
					sTransType = "1071";
					result.put("BindFlag", "true");
				}
				*/
				JSONObject items = GeneralTools.getItemName(jbo, "BankNo");
				// ���׼�¼�������
				BizObjectManager recordManager = jbo.getManager(
						"jbo.trade.transaction_record");
				BizObject recordBo = recordManager.newObject();
				
				String sTranSerialNo = saveTransRecord(jbo, recordManager,
						recordBo, sUserID, sAccountSerialNo, sAccountBelong,
						amount, sInputTime, sStatus, sTransType, items);
				
				JSONObject obj = new JSONObject();
				for (int i = 0; i < recordBo.getAttributeNumber(); i++) {
					obj.put(recordBo.getAttribute(i).getName()
							.toUpperCase(), recordBo.getAttribute(i)
							.getValue());
				}
				
				//��ʱд�� ʵ����֤��Ϣ
				String channel = String.valueOf(obj.get("TRANSCHANNEL"));
				String transCode = String.valueOf(obj.get("TRANSTYPE"));
				
				RealTimeTradeTranscation rttt = TranscationFactory.getRealTimeTradeTranscation(channel, transCode);
				rttt.init(obj);
				rttt.execute();
				String sLogId = rttt.getLogId();
				
				/*if (!("0302".equals(sAccountBelong))) {// ������������
					// ��ӽ���ʱ��
					recordBo.setAttributeValue("TRANSDATE",
							tool.getsCurrentDate());
					recordBo.setAttributeValue("TRANSTIME",
							tool.getsCurrentTime());
					
					
					//////////////////////////////////////////////////////
					JSONObject obj = new JSONObject();
					for (int i = 0; i < recordBo.getAttributeNumber(); i++) {
						obj.put(recordBo.getAttribute(i).getName()
								.toUpperCase(), recordBo.getAttribute(i)
								.getValue());
					}
					
					String channel = String.valueOf(obj.get("TRANSCHANNEL"));
					String transCode = String.valueOf(obj.get("TRANSTYPE"));
					RealTimeTradeTranscation rttt = TranscationFactory
							.getRealTimeTradeTranscation(channel, transCode);
					rttt.init(obj);
					rttt.execute();
					String sLogId = rttt.getLogId();
					*/
					//�󿨽��
					if(rttt.getTemplet().isSuccess())
					{
						recordBo.setAttributeValue("STATUS", "10");// ״̬���ɹ���
						recordBo.setAttributeValue("TRANSLOGID", sLogId);// ������־���
						recordBo.setAttributeValue("ACTUALAMOUNT", amount);// ʵ�ʵ��ʽ��
						recordBo.setAttributeValue("UPDATETIME",
								new TimeTool().getsCurrentMoment());// ��Ӹ���ʱ��
						recordManager.saveObject(recordBo);

						
						//����֧�����Ƿ���Ҫ�ƽ� ����Ҫ�ƽ�,���������Ҫ����������֤������ƽ�����
						String ticket = (String)InterfaceHelper.getFieldValue(rttt.getReponseMessage(), "ticket","");
						if(!InterfaceHelper.isEmpty(ticket))
						{//���ƽ�
							result.put("is_verified", "true");
							result.put("BindFlag", "0");
							result.put("serialno", accountBo.getAttribute("serialno").toString());
							result.put("transSerialNo", obj.get("SERIALNO"));
							
						}else{//�󿨳ɹ�
							accountBo.setAttributeValue("UPDATETIME",
									new TimeTool().getsCurrentMoment()).setAttributeValue("status","2");
							accountManager.saveObject(accountBo);
							result.put("BindFlag", "1");
						}
						
					}
					else
					{
								recordBo.setAttributeValue("STATUS", "04");// ״̬��ʧ�ܣ�
								recordBo.setAttributeValue("TRANSLOGID", sLogId);// ������־���
								recordBo.setAttributeValue("UPDATETIME",
										new TimeTool().getsCurrentMoment());// ��Ӹ���ʱ��
								recordManager.saveObject(recordBo);

								accountBo.setAttributeValue("STATUS", "6");// �Ƿ���֤(��ʧ��)
								accountBo.setAttributeValue("UPDATETIME",
										new TimeTool().getsCurrentMoment());
								accountManager.saveObject(accountBo);
								result.put("BindFlag", "2");
								
					}
			} else {
				result.put("BindFlag", "2");
			}
			
			return result;
		} catch (HandlerException e) {
			if(!sCertID.equals("")){
				RollbackRealName(jbo,sUserID);
			}
			throw e;
		} catch (Exception e) {
			if(!sCertID.equals("")){
				RollbackRealName(jbo,sUserID);
			}
			e.printStackTrace();
			throw new HandlerException("bindingcardapply.error");
		}

	}

	/**
	 * �ı�ԭ���Ѱ����п���״̬��ԭ״̬ -->ʧЧ
	 * 
	 * @param jbo
	 *            JBOFactory
	 * @param tx
	 *            JBOTransaction
	 * @param sUserID
	 *            �û�ID
	 * @throws HandlerException
	 */
	private void changeOriginalCardStatus(JBOFactory jbo, JBOTransaction tx,
			String sUserID) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_info", tx);
			BizObjectQuery query = m
					.createQuery("userid=:userid order by inputtime desc");
			query.setParameter("userid", sUserID);

			List<BizObject> list = query.getResultList(true);
			if (list != null) {
				for (Iterator<BizObject> it = list.iterator(); it.hasNext();) {
					BizObject o = it.next();
					o.setAttributeValue("STATUS", "5");
					o.setAttributeValue("UPDATETIME",
							new TimeTool().getsCurrentMoment());
					m.saveObject(o);
				}
			}
		} catch (Exception e) {
			throw new HandlerException("changeoriginalcardstatus.error");
		}
	}

	/**
	 * 
	 * @param jbo
	 *            JBOFactory
	 * @param recordManager
	 *            BizObjectManager
	 * @param recordBo
	 *            BizObject
	 * @param sUserID
	 *            �û����
	 * @param sSerialNo
	 *            �ʺ���ˮ
	 * @param sAccountBelong
	 *            ��������
	 * @param amount
	 *            ���׽��
	 * @param sInputTime
	 *            ����ʱ��
	 * @param sStatus
	 *            ����״̬
	 * @param sTransType
	 *            ��������
	 * @param items
	 *            ����������ֵ��ֵ��
	 * @return
	 * @throws HandlerExceptionsaveTransRecord
	 */
	private String saveTransRecord(JBOFactory jbo,
			BizObjectManager recordManager, BizObject recordBo, String sUserID,
			String sSerialNo, String sAccountBelong, double amount,
			String sInputTime, String sStatus, String sTransType,
			JSONObject items) throws HandlerException {
		try {
			String sAccountBelongName = items.containsKey(sAccountBelong) ? items
					.get(sAccountBelong).toString() : sAccountBelong;
			recordBo.setAttributeValue("USERID", sUserID);// �û����
			recordBo.setAttributeValue("DIRECTION", "P");// ��������(��)
			recordBo.setAttributeValue("AMOUNT", amount);// ���׽��
			recordBo.setAttributeValue("HANDLCHARGE", 0);// ������
			recordBo.setAttributeValue("TRANSTYPE", sTransType);// ��������(�󿨴��)
			recordBo.setAttributeValue("STATUS", sStatus);// ����״̬
			recordBo.setAttributeValue("INPUTTIME", sInputTime);// ����ʱ��
			recordBo.setAttributeValue("RELAACCOUNT", sSerialNo);// �����˻���ˮ��
			recordBo.setAttributeValue("RELAACCOUNTTYPE", "001");// ���׹����˻�����
			String sTransChannel  = null;
			
			sTransChannel = GeneralTools.getTransChannel(jbo,
					sAccountBelong, "ATTRIBUTE2");
			
			/*if(!("0302".equals(sAccountBelong))){
				sTransChannel = GeneralTools.getTransChannel(jbo,
						sAccountBelong, "ATTRIBUTE2");
			}else{
				sTransChannel = GeneralTools.getTransChannel(jbo,
						sAccountBelong, "ATTRIBUTE6");
			}
			*/
			
			recordBo.setAttributeValue("TRANSCHANNEL", sTransChannel);// ��������
			recordBo.setAttributeValue("REMARK", "" + "���п���|"
					+ sAccountBelongName);// ��ע

			recordManager.saveObject(recordBo);
			return recordBo.getAttribute("SERIALNO").toString();

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
	private String getPhoneTel(JBOFactory jbo, String sUserID)
			throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = m.createQuery("userid=:userid");
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				return o.getAttribute("PHONETEL").getString();
			} else {
				return "";
			}
		} catch (Exception e) {
			// throw new HandlerException("quaryphonetel.error");
			return "";
		}
	}


/**
 * ��ȡ�û���ʵ����
 * 
 * @param accountBo
 * @throws HandlerException
 */
private String getRealName(JBOFactory jbo, String sUserID)
		throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_detail");
			BizObjectQuery query = m.createQuery("userid=:userid");
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				return o.getAttribute("REALNAME").getString();
			} else {
				return "";
			}
		} catch (Exception e) {
			// throw new HandlerException("quaryphonetel.error");
			return "";
		}
	}

/**
 * �ع�ʵ����֤
 * 
 * @param accountBo
 * @throws HandlerException
 */
private void RollbackRealName(JBOFactory jbo, String sUserID)
		throws HandlerException {
		BizObjectManager ma;
		BizObjectManager mb;
		BizObjectManager mc;
		try {
			
			//���ʵ�������֤
			ma = jbo.getManager("jbo.trade.account_detail");
			BizObject o1=ma.createQuery("userid=:userid").setParameter("userid", sUserID).getSingleResult(true);
			o1.setAttributeValue("realname", null);
			o1.setAttributeValue("CERTID", null);
			ma.saveObject(o1);
			
			//�޸�ʵ����֤��־
			mb = jbo.getManager("jbo.trade.user_account");
			BizObject o2=mb.createQuery("userid=:userid").setParameter("userid", sUserID).getSingleResult(true);
			o2.setAttributeValue("USERAUTHFLAG", null);
			mb.saveObject(o2);
			
			//ɾ����֤��¼
			mc = jbo.getManager("jbo.trade.user_authentication");
			BizObject o3=mc.createQuery("userid=:userid").setParameter("userid", sUserID).getSingleResult(true);
			mc.deleteObject(o3);
			
		} catch (Exception e) {
			
		}
	}
}
