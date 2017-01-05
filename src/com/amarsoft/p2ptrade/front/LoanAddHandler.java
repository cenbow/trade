package com.amarsoft.p2ptrade.front;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.account.RegisterHandler;

/**
 * �������󷢲�
 * ���������
 *      UserID:�û�ID
 *      loantype:��������
 *      ocupation:ְҵ
 *      
 * ��������� 
 * 	    flag:�ɹ���ʶ
 *      �������
 */
public class LoanAddHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return loanJoin(request);
	}

	//��Ӵ�����Ϣ
	public JSONObject loanJoin(JSONObject request) throws HandlerException{
		JSONObject result = new JSONObject();
		String prov = (String)request.get("prov");//ʡ��
		String city = (String)request.get("city");//����
		String fundsource = (String)request.get("fundsource");//������;
		String jobtype = (String)request.get("jobtype");//ְҵ����
		String businesssum = (String)request.get("businesssum");//������
		String sex = (String)request.get("sex");//�Ա�
		String username = (String)request.get("username");//����
		String userid = (String)request.get("userid");//�û�ID
		String phone = (String)request.get("phone");//�ֻ�����
		String applyway = (String)request.get("applyway");//����
		String usertype = (String)request.get("usertype");//�û�����
		String monthincome = (String)request.get("monthincome");//������
		String recommenderid = (String)request.get("recommenderid");//�Ƽ��û����
		String isrecommend = (String)request.get("isrecommend");//�Ƿ��Ƽ�
		String incometype = (String)request.get("incometype");//���ʷ�����ʽ
		String projectname = (String)request.get("projectname")==null?"":(String)request.get("projectname");//�������
		String fundsourcedesc = (String)request.get("fundsourcedesc")==null?"":(String)request.get("fundsourcedesc");//����˵��
		try {
			//ע��
			if("1".equals(isrecommend)){//�Ƽ����ж��û���������
				
			}else if(userid==null || userid.length()<1){
				JSONObject register = (JSONObject)new RegisterHandler().createResponse(request, null);
				userid = (String)register.get("userid");
			}
			
			
			
			//��ȡ�û����
			
			//�������
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager loanManger = jbo.getManager("jbo.trade.loan_apply");
			BizObject o = loanManger.newObject();
			o.setAttributeValue("fundsource", fundsource);
			o.setAttributeValue("businesssum", businesssum);
			o.setAttributeValue("prov", prov);
			o.setAttributeValue("city", city);
			o.setAttributeValue("applyway", applyway);
			o.setAttributeValue("userid", userid);
			o.setAttributeValue("username", username);
			o.setAttributeValue("phone", phone);
			o.setAttributeValue("sex", sex);
			o.setAttributeValue("jobtype", jobtype);
			o.setAttributeValue("monthincome", monthincome);
			o.setAttributeValue("recommenderid", recommenderid);
			o.setAttributeValue("isrecommend", isrecommend);
			o.setAttributeValue("usertype", usertype);
			o.setAttributeValue("incometype", incometype);
			o.setAttributeValue("projectname", projectname);
			o.setAttributeValue("fundsourcedesc", fundsourcedesc);
			o.setAttributeValue("applystatus", "010");
			o.setAttributeValue("applytime", StringFunction.getTodayNow());
			loanManger.saveObject(o);
			o.setAttributeValue("loanno", o.getAttribute("applyno"));
			loanManger.saveObject(o);
			result.put("flag", "success");
			result.put("userid", userid);
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return result;
	}
}
