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

/**
 * �������Է���
 * ���������
 *      UserName:�û�����
 *      telphone:�ֻ�����
 *      
 * ��������� 
 * 	    flag:�ɹ���ʶ
 *      �������
 */
public class LoanNoteAddHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return loanJoin(request);
	}

	//��Ӵ���������Ϣ
	public JSONObject loanJoin(JSONObject request) throws HandlerException{
		JSONObject result = new JSONObject();
		String jobtype = (String)request.get("jobtype");//ְҵ����
		String monthincoming = (String)request.get("monthincoming");//������
		String businesssum = (String)request.get("businesssum");//������
		String sex = (String)request.get("sex");//�Ա�
		String username = (String)request.get("username");//����
		String telphone = (String)request.get("telphone");//�ֻ�����
		if(jobtype==null||jobtype.length()<1)
			throw new HandlerException("loannote.nojobtype");
		if(businesssum==null||businesssum.length()<1)
			throw new HandlerException("loannote.nobusinessum");
		if(sex==null||sex.length()<1)
			throw new HandlerException("loannote.nosex");
		if(username==null||username.length()<1)
			throw new HandlerException("loannote.nousername");
		if(telphone==null||telphone.length()<1)
			throw new HandlerException("loannote.notelphone");
		if(monthincoming==null||monthincoming.length()<1)
			throw new HandlerException("loannote.nomonthincoming");
		try {
			//�������
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager loanManger = jbo.getManager("jbo.trade.loan_note");
			BizObject o = loanManger.newObject();
			o.setAttributeValue("loansum", businesssum);
			o.setAttributeValue("username", username);
			o.setAttributeValue("telphone", telphone);
			o.setAttributeValue("sex", sex);
			o.setAttributeValue("jobtype", jobtype);
			o.setAttributeValue("monthincoming", monthincoming);
			o.setAttributeValue("status", "0");
			o.setAttributeValue("inputtime", StringFunction.getTodayNow());
			loanManger.saveObject(o);
			result.put("flag", "success");
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return result;
	}
}
