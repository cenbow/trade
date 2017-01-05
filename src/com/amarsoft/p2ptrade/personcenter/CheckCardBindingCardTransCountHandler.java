package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ����֤���ſ�Ƭ������ѯ���� (�ʺ��Ƿ�����ʹ�û���֤�У��ѳɹ����д��İ󿨴����Ƿ񳬹����ƴ���)
 * ��������� 
 * 		UserID:�û���� 
 * 		AccountNo:�˻���(����) 
 * ����������ɹ���־ 
 * 
 */
public class CheckCardBindingCardTransCountHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return checkCardBindingCardTransCount(request);
	}

	/**
	 * ��ѯ�����п�
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject checkCardBindingCardTransCount(JSONObject request)
			throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}
		if (request.get("AccountNo") == null || "".equals(request.get("AccountNo"))) {
			throw new HandlerException("accountno.error");
		}
		String sUserID = request.get("UserID").toString();
		String sAccountNo = request.get("AccountNo").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			
			//��ѯ��Ƭ��ǰ�Ƿ񱻰󶨻�������֤��
			BizObjectManager accountManager= jbo
					.getManager("jbo.trade.account_info");
			BizObjectQuery accountQuery = accountManager
					.createQuery("accountno=:accountno and ((status = :status1) or (status=:status2 and accountbelong <> '0302' and updatetime is not null) or (status=:status3 and accountbelong='0302'))");
			accountQuery.setParameter("accountno", sAccountNo).setParameter("status1",
					"2").setParameter("status2", "1").setParameter("status3", "1");
			BizObject accountBo = accountQuery.getSingleResult(false);
			if(accountBo != null){
				throw new HandlerException("bindcardisinuse.error");
			}

			//У��ÿ����ѳɹ����Ĵ����Ƿ񳬹�����
			BizObjectManager m = jbo
					.getManager("jbo.trade.transaction_record");

		//	String sRelaSerialNo = getRelaAccountSerialNo(jbo,sUserID,sAccountNo);
			BizObjectQuery query = m
					.createQuery("select count(*) as v.count from o where o.userid=:userid1 and o.relaaccount in (select ai.serialno from jbo.trade.account_info ai where ai.userid=:userid2 and ai.accountno=:accountno) and o.transtype like :transtype and o.status=:status");
			query.setParameter("userid1", sUserID).setParameter("userid2", sUserID).setParameter("accountno", sAccountNo).setParameter("transtype", "107%").setParameter("status", "10");
			BizObject o = query.getSingleResult(false);
			if (o != null) {
				double count = Double.parseDouble(o.getAttribute("count").toString());
				if(count >= 20){
					throw new HandlerException("bindcardcountlimit.error");
				}
			} 
			return null;
		} catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("getbindingbankallcardtime.error");
		}
	}

	private String getRelaAccountSerialNo(JBOFactory jbo,String sUserID,String AccountNo) throws HandlerException {
		BizObjectManager m;
		try {
			String sSerialNo = null;
			m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m
					.createQuery("select SERIALNO from o where userid=:userid and accountno=:accountno");
			query.setParameter("userid", sUserID).setParameter("accountno", AccountNo);
			BizObject o = query.getSingleResult(false);
			if(o != null){
				sSerialNo = o.getAttribute("SERIALNO").toString();
			}else{
				sSerialNo = "";
			}
			return sSerialNo;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("getrelaserialno.error");
		}
		
	}

}
