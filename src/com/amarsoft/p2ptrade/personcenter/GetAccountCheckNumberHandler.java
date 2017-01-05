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
 * ����ѯ����
 * ���������
 * 		UserID:�˻����
 *		AccountNo:�˻���
 * ���������
 * 		CheckNumber:��У�����
 *
 */
public class GetAccountCheckNumberHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getAccountCheckNumber(request);
		
	}
	  
	/**
	 * ����ѯ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getAccountCheckNumber(JSONObject request)throws HandlerException {
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
		if (request.get("AccountNo") == null || "".equals(request.get("AccountNo"))) {
			throw new HandlerException("accountno.error");
		}
		
		String sUserID = request.get("UserID").toString();//�û����
		String sAccountNo = request.get("AccountNo").toString();//�˻���
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query = m.createQuery("userid=:userid and accountno=:accountno and status=:status");
			query.setParameter("userid", sUserID).setParameter("accountno", sAccountNo).setParameter("status", "1");
			BizObject o = query.getSingleResult(false);
			if(o != null){
				int checkNumber =  o.getAttribute("CHECKNUMBER").getInt();
				JSONObject result = new JSONObject();
				result.put("CheckNumber", String.valueOf(checkNumber));
				return result;
			}else{
				throw new HandlerException("getaccountchecknumber.error");
			}
		}catch(HandlerException e){
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("getaccountchecknumber.error");
		}
	}
}
