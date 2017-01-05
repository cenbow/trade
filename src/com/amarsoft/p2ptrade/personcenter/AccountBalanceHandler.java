package com.amarsoft.p2ptrade.personcenter;

import java.util.HashMap;
import java.util.Properties;

import com.amarsoft.account.common.ObjectBalanceUtils;
import com.amarsoft.account.common.ObjectConstants;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.account.QueryUserAccountInfoHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
/**
 * ����ѯ����
 * ���������
 * 		UserID:�˻����
 * 		WithBalance : �Ƿ��ѯ���
 * ���������
 * 		UsableBalance:�˻��������
 * 		FrozenBalance:�˻�������
 *
 */
public class AccountBalanceHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getAccountBalance(request);
		
	}
	  
	/**
	 * ����ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getAccountBalance(JSONObject request)throws HandlerException {
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
		
		String sUserID = request.get("UserID").toString();//�û����
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.user_account");
			
			BizObjectQuery query = m.createQuery("select phonetel,UserAuthFlag,TransPWD,PhoneAuthFlag from o where userid=:userid");
			query.setParameter("userid", sUserID);
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				JSONObject obj = new JSONObject();
				obj.put("RootType", "010");
				
				//�޸�����ȡ����
				double usableBalance = 0.0;
				double frozenBalance = 0.0;
				
				if(request.containsKey("WithBalance") && "false".equalsIgnoreCase(request.get("WithBalance").toString())){
					
				}
				else{
					HashMap<String,Double> balances = ObjectBalanceUtils.ObjectBalanceUtils(sUserID, ObjectConstants.OBJECT_TYPE_001);
					if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_001))
						usableBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_001); //��ѯ�������
					
					if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_002))
						frozenBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_002); //��ѯ�������
				}
			
				/*	
			 		double usableBalance = Double.parseDouble(o.getAttribute("USABLEBALANCE").toString() == null?"0":o.getAttribute("USABLEBALANCE").toString());
					double frozenBalance = Double.parseDouble(o.getAttribute("FROZENBALANCE").toString() == null?"0":o.getAttribute("FROZENBALANCE").toString());
				*/
				String sRealName = getRealName(jbo,sUserID);
				String phone = getCardPhone(jbo,sUserID);
				if(phone.length()==11)
					phone = phone.substring(7);
				obj.put("cardphone", phone);
				obj.put("RealName", sRealName);
				obj.put("phoneTel", o.getAttribute("phonetel").toString() == null?"":o.getAttribute("phonetel").toString());
				obj.put("TransPWDFlag", o.getAttribute("TRANSPWD").getString()==null?"Y":"N");
				obj.put("UserAuthFlag", o.getAttribute("UserAuthFlag").toString() == null?"1":o.getAttribute("UserAuthFlag").toString());
				obj.put("PhoneAuthFlag", o.getAttribute("PhoneAuthFlag").toString() == null?"":o.getAttribute("PhoneAuthFlag").toString());
				obj.put("UsableBalance", GeneralTools.numberFormat(usableBalance, 0, 2));
				obj.put("FrozenBalance", GeneralTools.numberFormat(frozenBalance, 0, 2));
				
				QueryUserAccountInfoHandler user = new QueryUserAccountInfoHandler();
				JSONObject oo = user.getBindingBankCard(request);
				obj.put("BankStatus", oo.get("Status"));
				return obj;
			}
			else{
				throw new HandlerException("common.usernotexist");//�û�������
			}
		}catch(HandlerException e){
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryaccountbalance.error");
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
	 * ��ȡ����Ԥ���ֻ�����
	 * 
	 * @param accountBo
	 * @throws HandlerException
	 */
	private String getCardPhone(JBOFactory jbo, String sUserID) throws HandlerException {
			BizObjectManager m;
			try {
				m = jbo.getManager("jbo.trade.account_info");
				BizObjectQuery query = m.createQuery(" status='2' and userid=:userid");
				query.setParameter("userid", sUserID);
				BizObject o = query.getSingleResult(false);
				if (o != null) {
					return o.getAttribute("PHONENO").getString();
				} else {
					return "";
				}
			} catch (Exception e) {
				// throw new HandlerException("quaryphonetel.error");
				return "";
			}
	}
}
