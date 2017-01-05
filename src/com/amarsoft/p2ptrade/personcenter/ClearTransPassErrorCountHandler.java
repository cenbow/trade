package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/**
 * ������ս�����������¼
 * ���������
 * 		UserID:�˻����
 * ���������
 *
 */
public class ClearTransPassErrorCountHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return clearTransPassErrorCount(request);
	}
	  
	/**
	 * ������ս�����������¼
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject clearTransPassErrorCount(JSONObject request)throws HandlerException {
		//����У��
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("common.emptyuserid");
		}
		
		String sUserID = request.get("UserID").toString();//�û����
		
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m =jbo.getManager("jbo.trade.account_validlog");
			
			BizObjectQuery query = m.createQuery("delete from o where userid=:userid and validdate=:validdate and validtype='T' ");
			query.setParameter("userid", sUserID);
			query.setParameter("validdate", StringFunction.getToday());
			query.executeUpdate();
			
			return null;
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("cleartranspasserrorcount.error");
		}
	}
}
