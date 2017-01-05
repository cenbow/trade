package com.amarsoft.p2ptrade.personcenter;

import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.ASValuePool;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;

/**
 * ��֤����� ��������� UserID:�û���� Amount:����� AccountNo:�˻���
 * NeedSendFlag:�ɹ��Ƿ���Ҫ���Ͷ�������(true -- >��Ҫ �� false -->����Ҫ) ��������� �ɹ���־
 * 
 */
public class CheckBindBankAmountHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return checkBindBankAmount(request);
	}

	/**
	 * ��֤�����
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject checkBindBankAmount(JSONObject request)
			throws HandlerException {
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("userid.error");
		}
		if (request.get("Amount") == null || "".equals(request.get("Amount"))) {
			throw new HandlerException("amount.error");
		}
		if (request.get("AccountNo") == null
				|| "".equals(request.get("AccountNo"))) {
			throw new HandlerException("accountno.error");
		}

		String sUserID = request.get("UserID").toString();// �û����
		String sAccountNo = request.get("AccountNo").toString();// �˻���
		String sNeedSendFlag = request.containsKey("NeedSendFlag") ? request
				.get("NeedSendFlag").toString() : "false";// �Ƿ���Ҫ���Ͷ���
		// �����
		double amount = 0d;
		try {
			amount = Double.parseDouble(request.get("Amount").toString());
		} catch (NumberFormatException e2) {
			throw new HandlerException("amount.error");
		}
		if (amount <= 0) {
			throw new HandlerException("amount.error");
		}

		JBOFactory jbo = JBOFactory.getFactory();
		JBOTransaction tx = null;
		try {
			tx = jbo.createTransaction();

			// ��ȡ���ʺ���ˮ��
			String sAccountSerialNo = getAccountSerialNo(jbo, sUserID,
					sAccountNo);

			BizObjectManager m = jbo
					.getManager("jbo.trade.transaction_record");

			BizObjectQuery query = m
					.createQuery("userid=:userid and transtype like :transtype and relaaccount=:relaaccount and status=:status order by inputtime desc");
			query.setParameter("userid", sUserID)
					.setParameter("transtype", "107%")
					.setParameter("relaaccount", sAccountSerialNo)
					.setParameter("status", "10");

			List<BizObject> list = query.getResultList(false);
			if (list != null && list.size() != 0) {
				BizObject o = list.get(0);

//				SimpleDateFormat sdf = new SimpleDateFormat(
//						"yyyy/MM/dd HH:mm:ss");
//				TimeTool tool = new TimeTool();
//				String sCurrentTime = tool.getsCurrentMoment();
//				String sInputTime = o.getAttribute("INPUTTIME").toString();
//
//				Date d1 = sdf.parse(sCurrentTime);
//				Date d2 = sdf.parse(sInputTime);
//				long diff = d1.getTime() - d2.getTime();
//				if (diff > (3 * 24 * 60 * 60 * 1000)) {// �������죬���ٱȽ�
//					changeCurrentCardStatus(jbo, sAccountSerialNo, "5");
//					throw new HandlerException("bindcardtimeout.error");
//				}

				double transAmount = Double.parseDouble(o
						.getAttribute("AMOUNT").toString());
				if (transAmount == amount) {// У�����
					// �����������͵��û�����
					String userIDUC = null, userSubIDUC = null, mainAccountFlag = null;
					BizObjectManager accountManager = jbo
							.getManager("jbo.trade.user_account",tx);
					BizObject accountBo = accountManager
							.createQuery("userid=:userid")
							.setParameter("userid", sUserID)
							.getSingleResult(true);
					if (accountBo != null) {
						//��Ӹ߷��ձ�־
						accountBo.setAttributeValue("LOCKFLAG", "1");
						accountBo.setAttributeValue("HIGHRISK", "1");
						accountManager.saveObject(accountBo);
						
						userIDUC = accountBo.getAttribute("UCUSERID")
								.getString();
						userSubIDUC = accountBo.getAttribute("UCSUBUSERID")
								.getString();
						if (userIDUC != null && userIDUC.length() > 0) {
							mainAccountFlag = "Y";
						} else if (userSubIDUC != null
								&& userSubIDUC.length() > 0) {
							mainAccountFlag = "N";
						} else {
							throw new HandlerException(
									"common.usercenter.usernotexist");
						}

					} else {
						throw new HandlerException("common.usernotexist");
					}
					ASValuePool params = new ASValuePool();

					params.setAttribute("UserID",
							"Y".equals(mainAccountFlag) ? userIDUC
									: userSubIDUC);
					params.setAttribute("MainAccountFlag", mainAccountFlag);
					
					accountBo.setAttributeValue("LOCKFLAG", "2");
					accountManager.saveObject(accountBo);

					changeCurrentCardStatus(jbo, tx, sAccountSerialNo, "2");
					

					if ("true".equals(sNeedSendFlag)) {// ���Ͷ�������
						String sPhoneTel = getPhoneTel(jbo, sUserID);
						if(sPhoneTel != null && !"".equals(sPhoneTel)){
							HashMap<String, Object> parameters = new HashMap<String, Object>();
							parameters.put("Date", new TimeTool().getsChDate());
							GeneralTools.sendSMS("P2P_YHKBGCG",
									getPhoneTel(jbo, sUserID), parameters);
						}
					}
					tx.commit();
					return null;
				} else {
					changeCheckNumber(jbo,sAccountSerialNo);//��Ӵ���У�����
					throw new HandlerException(
							"checkbindcark.amountunlikeness.error");
				}
			} else {
				throw new HandlerException("checkbindcark.nodata.error");
			}

		} catch (HandlerException e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			throw e;
		} catch (Exception e) {
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new HandlerException("checkbindcark.error");
		}
	}

	private String getAccountSerialNo(JBOFactory jbo, String sUserID,
			String sAccountNo) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery("userid=:userid and accountno=:accountno and status=:status");
			query.setParameter("userid", sUserID)
					.setParameter("accountno", sAccountNo)
					.setParameter("status", "1");
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				return o.getAttribute("SERIALNO").toString();
			} else {
				throw new HandlerException("getserialno.nodata.error");
			}
		} catch (JBOException e) {
			throw new HandlerException("getserialno.error");
		}
	}

	/**
	 * �ı�����������֤�����п���״̬
	 * 
	 * @param jbo
	 * @param tx
	 * @param sUserID
	 * @param sSerialNo
	 * @throws HandlerException
	 */
	private void changeCurrentCardStatus(JBOFactory jbo,JBOTransaction tx,
			String sSerialNo, String sStatus) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_info",tx);
			BizObjectQuery query = m
					.createQuery("serialno=:serialno and status=:status");
			query.setParameter("serialno", sSerialNo).setParameter("status",
					"1");
			BizObject o = query.getSingleResult(true);
			if (o != null) {
				o.setAttributeValue("STATUS", sStatus);
				o.setAttributeValue("UPDATETIME",
						new TimeTool().getsCurrentMoment());
				m.saveObject(o);
			}
		} catch (Exception e) {
			throw new HandlerException("changecurrentcardstatus.error");
		}
	}

	/**
	 * �ı�����������֤�����п���״̬
	 * 
	 * @param jbo
	 * @param tx
	 * @param sUserID
	 * @param sSerialNo
	 * @throws HandlerException
	 */
	private void changeCurrentCardStatus(JBOFactory jbo,
			String sSerialNo, String sStatus) throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery("serialno=:serialno and status=:status");
			query.setParameter("serialno", sSerialNo).setParameter("status",
					"1");
			BizObject o = query.getSingleResult(true);
			if (o != null) {
				o.setAttributeValue("STATUS", sStatus);
				o.setAttributeValue("UPDATETIME",
						new TimeTool().getsCurrentMoment());
				m.saveObject(o);
			}
		} catch (Exception e) {
			throw new HandlerException("changecurrentcardstatus.error");
		}
	}
	
	/**
	 * ��ȡ�Ѽ������Ĵ���
	 * 
	 * @param jbo
	 * @param tx
	 * @param sUserID
	 * @param sSerialNo
	 * @throws HandlerException
	 */
	private void changeCheckNumber(JBOFactory jbo, String sSerialNo)
			throws HandlerException {
		BizObjectManager m;
		try {
			m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery("serialno=:serialno and status=:status");
			query.setParameter("serialno", sSerialNo).setParameter("status",
					"1");
			BizObject o = query.getSingleResult(true);
			if (o != null) {
				int checkNumber = o.getAttribute("CHECKNUMBER").getInt();
				checkNumber += 1;
				if(checkNumber >= 5){
					o.setAttributeValue("STATUS", "5");
				}else{
					o.setAttributeValue("CHECKNUMBER", checkNumber);
				}
				m.saveObject(o);
			}
		} catch (Exception e) {
			throw new HandlerException("changechecknumber.error");
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
		try {
			BizObjectManager accountManager = jbo
					.getManager("jbo.trade.user_account");
			BizObject accountBo = accountManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			if (accountBo != null) {
				return accountBo.getAttribute("PHONETEL").toString();
			} else {
				throw new HandlerException("quaryphonetel.error");
			}
		} catch (Exception e) {
			throw new HandlerException("quaryphonetel.error");
		}
	}
}
