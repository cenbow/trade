package com.amarsoft.p2ptrade.loan;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/*
 *	�û���ͥ��Ϣ ��֤
 * */
public class FamilyConfirmHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return createJsonObject(request);
	}	
	
	private JSONObject createJsonObject(JSONObject request) throws HandlerException{
		JSONObject result = new JSONObject();
		String prov = (String)request.get("prov0");//ʡ��
		String city = (String)request.get("city0");//����
		String sex = (String)request.get("sex");//�Ա�
		String borndate = (String)request.get("borndate");//��������
		String familyadd = (String)request.get("familyadd");//ְҵ����
		String familytel = (String)request.get("familytel");//������
		String iddoc1 = (String)request.get("iddoc1");//���֤����1
		String iddoc2 = (String)request.get("iddoc2");//���֤����2
		String userid = (String)request.get("userid");//���֤����2
		try {

			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.account_detail");
			BizObjectQuery query = m.createQuery("userid=:userid");
			query.setParameter("userid", userid);
			
			BizObject o = query.getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("prov", prov);
				o.setAttributeValue("city", city);
				o.setAttributeValue("SEXUAL", sex);
				o.setAttributeValue("borndate", borndate);
				o.setAttributeValue("familyadd", familyadd);
				o.setAttributeValue("familytel", familytel);
				m.saveObject(o);
				result.put("flag", "success");
			}else{
				result.put("flag", "error");
			}
		}catch(Exception e){
			e.printStackTrace();
		}	
		return result;
	}
}
