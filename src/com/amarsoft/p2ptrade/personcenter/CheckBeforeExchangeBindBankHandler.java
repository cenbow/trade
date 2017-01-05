package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �û��������п�ǰ��֤
 * ��������� 
 * 		UserID:�û����
 * ��������� �ɹ���־ 
 * 
 */
public class CheckBeforeExchangeBindBankHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return checkBeforeExchangeBank(request);
	}

	/**
	 * �û��������п�ǰ��֤
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	private JSONObject checkBeforeExchangeBank(JSONObject request)
			throws HandlerException {

		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}

		try {
			String sUserID = request.get("UserID").toString();// �û����
			JBOFactory jbo = JBOFactory.getFactory();
			
			BizObjectManager accountManager = jbo.getManager(
					"jbo.trade.user_account");
			BizObject accountBo = accountManager.createQuery("userid=:userid")
					.setParameter("userid", sUserID).getSingleResult(true);
			double usableBalance = 0;// �˻��������
			double frozenBalance = 0;// �˻�������
			// ��ȡ�û������Ϣ
			if (accountBo != null) {
				usableBalance = Double.parseDouble(accountBo.getAttribute(
						"USABLEBALANCE").toString() == null ?"0":accountBo.getAttribute(
								"USABLEBALANCE").toString());
				frozenBalance = Double.parseDouble(accountBo.getAttribute(
						"FROZENBALANCE").toString() == null ?"0":accountBo.getAttribute(
								"FROZENBALANCE").toString());
			} else {
				throw new HandlerException("quaryaccountamount.nodata.error");
			}
			//�˻����У��
			if(usableBalance != 0 || frozenBalance!=0){
				throw new HandlerException("counthavebalance.error");
			}
			
			//Ͷ�ʺͻ���У��
//			BizObjectManager loanManager = jbo.getManager("jbo.trade.acct_loan");
//			BizObjectQuery loanQuery = loanManager
//					.createQuery("select count(1) as v.cnt from o ,jbo.trade.ti_contract_info tci ,jbo.trade.user_contract tua  "
//							+ "where tci.contractid = tua.contractid and serialno = tci.loanno and loanstatus in ('0','1') "
//							+ "and finishdate is null  and tua.userid =:userid ");
			
			BizObjectManager loanManager = jbo.getManager("jbo.trade.acct_loan");
			BizObjectQuery loanQuery = loanManager
					.createQuery("select count(1) as v.cnt from o ,jbo.trade.ti_contract_info tci ,jbo.trade.user_contract tua  "
							+ "where tci.contractid = tua.contractid and o.serialno = tci.loanno and o.loanstatus not  in ('10','20','30','80','91','92') "
							+ "  and tua.userid =:userid ");
			loanQuery.setParameter("userid", sUserID);
			
			

			BizObject loanManagerBo = loanQuery.getSingleResult(false);
			double count;
			if (loanManagerBo != null) {
				count = Double.parseDouble(loanManagerBo.getAttribute("cnt").toString());
			} else {
				count = 0.0;
			}
			if(count > 0){
				throw new HandlerException("haveunclearbus.error");
			}
			return null;
		}
		catch (HandlerException e) {
			throw e;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("checkbeforeexchangebindcark.error");
		}
	}
}
