package com.amarsoft.p2ptrade.loan;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
/**
 * ��ȡ�ʺ�����͹�����Ϣ����
 * @author Mbmo
 *
 */
public class MyPendingInfoDetailHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1) throws HandlerException {
		
		return getResult(request);
	}

	@SuppressWarnings("unchecked")
	private JSONObject getResult(JSONObject request) {
		JSONObject result=new JSONObject();
		String userId = (String) request.get("userId");
		
		BizObject accountDetailB=getAccountDetailResult(userId);//�ʺ���������
		BizObject workDetailB=getWorkDetailResult(userId);//������Ϣ��������
		JSONObject accountDetailJ=new JSONObject();
		JSONObject workDetailJ=new JSONObject();
		try {
			accountDetailJ.put("realName", accountDetailB.getAttribute("realname")==null?"":accountDetailB.getAttribute("realname").toString());//��ʵ����
			accountDetailJ.put("certId", accountDetailB.getAttribute("certid")==null?"":accountDetailB.getAttribute("certid").toString());//֤������
			accountDetailJ.put("sexual", accountDetailB.getAttribute("sexual")==null?"":accountDetailB.getAttribute("sexual").toString());//�Ա�
			accountDetailJ.put("birthday", accountDetailB.getAttribute("borndate")==null?"":accountDetailB.getAttribute("borndate").toString());//��������
			accountDetailJ.put("marriage", accountDetailB.getAttribute("marriage")==null?"":accountDetailB.getAttribute("marriage").toString());//����״��
			accountDetailJ.put("afterworld", accountDetailB.getAttribute("afterworld")==null?"":accountDetailB.getAttribute("afterworld").toString());//������Ů
			accountDetailJ.put("education", accountDetailB.getAttribute("education")==null?"":accountDetailB.getAttribute("education").toString());//���ѧ��
			accountDetailJ.put("familyAdd", accountDetailB.getAttribute("FAMILYADD")==null?"":accountDetailB.getAttribute("FAMILYADD").toString());//��ס��ַ
			accountDetailJ.put("familyTel", accountDetailB.getAttribute("FAMILYTEL")==null?"":accountDetailB.getAttribute("FAMILYTEL").toString());//��ס�ص绰
			
			workDetailJ.put("employeeType", workDetailB.getAttribute("employeetype")==null?"":workDetailB.getAttribute("employeetype").toString());//ְҵ״̬
			workDetailJ.put("workCorp", workDetailB.getAttribute("workcorp")==null?"":workDetailB.getAttribute("workcorp").toString());//��λ����
			workDetailJ.put("department", workDetailB.getAttribute("department")==null?"":workDetailB.getAttribute("department").toString());//ְλ
			workDetailJ.put("salary", workDetailB.getAttribute("salary")==null?"":workDetailB.getAttribute("salary").toString());//������
			workDetailJ.put("paymentType", workDetailB.getAttribute("payment_type")==null?"":workDetailB.getAttribute("payment_type").toString());//����������ʽ
			workDetailJ.put("workNature", workDetailB.getAttribute("worknature")==null?"":workDetailB.getAttribute("worknature").toString());//��˾���
			workDetailJ.put("unitkind", workDetailB.getAttribute("unitkind")==null?"":workDetailB.getAttribute("unitkind").toString());//��˾��ҵ
			workDetailJ.put("corpScope", workDetailB.getAttribute("corp_scope")==null?"":workDetailB.getAttribute("corp_scope").toString());//��˾��ģ
			workDetailJ.put("startDate", workDetailB.getAttribute("start_date")==null?"":workDetailB.getAttribute("start_date").toString());//���ֵ�λ��������
			workDetailJ.put("prov", workDetailB.getAttribute("prov")==null?"":workDetailB.getAttribute("prov").toString());//ʡ
			workDetailJ.put("city", workDetailB.getAttribute("city")==null?"":workDetailB.getAttribute("city").toString());//��
			
		} catch (JBOException e) {
			e.printStackTrace();
		}
		result.put("accountDetail", accountDetailJ);
		result.put("workDetail", workDetailJ);
		return result;
	}

	private BizObject getAccountDetailResult(String userId) {
		BizObject r = null;
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		try {
			m=f.getManager("jbo.trade.account_detail");
			r=m.createQuery("userId=:userId").setParameter("userId", userId).getSingleResult(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return r;
	}

	private BizObject getWorkDetailResult(String userId) {
		BizObject r = null;
		JBOFactory f = JBOFactory.getFactory();
		BizObjectManager m;
		try {
			m=f.getManager("jbo.trade.customer_work");
			r=m.createQuery("customerid=:userId").setParameter("userId", userId).getSingleResult(false);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return r;
	}
	

}
