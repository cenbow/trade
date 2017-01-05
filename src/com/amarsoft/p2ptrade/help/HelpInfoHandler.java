package com.amarsoft.p2ptrade.help;
/**
 * ������ϸ
 * ���������
 * 		Serialno ��ˮ��
 * ���������
 * 		title  ����
 * 		content ����
 */
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class HelpInfoHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		try{
			BizObjectManager manager = JBOFactory.getBizObjectManager("jbo.trade.ti_help_info");
			
			if(request.containsKey("Serialno")==false)
				throw new HandlerException("request.invalid");
			BizObjectQuery query = manager.createQuery("select title,content from o where serialno =:serialno");
			query.setParameter("serialno", request.get("Serialno").toString());
			BizObject obj = query.getSingleResult(false);
			JSONObject result = new JSONObject();
			if(obj==null){
				throw new HandlerException("���޽��");
			}
			else{
				result.put("title", obj.getAttribute("title").getString());
				result.put("content", obj.getAttribute("content").getString());
				
			}
			return result;
		}
		catch(JBOException je){
			je.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}

}
