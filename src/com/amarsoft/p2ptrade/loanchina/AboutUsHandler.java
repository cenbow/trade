package com.amarsoft.p2ptrade.loanchina;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class AboutUsHandler extends JSONHandler{
	private String classify;
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		// TODO Auto-generated method stub
		return selectGuideDetail(request);
	}

	private JSONObject selectGuideDetail(JSONObject request) throws HandlerException{
		// TODO Auto-generated method stub
		
		JSONObject result = new JSONObject();
		if(request.containsKey("classify")){
			if (request.get("classify")!=null) {
				this.classify = request.get("classify").toString();	
			}
		}	
		
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.inf_catalog");
			
			//��ȡ����
			BizObjectQuery culumn_query = m.createQuery("length(itemno)=9 and substr(itemno,0,6)=:itemno");
			culumn_query.setParameter("itemno", classify);
			List<BizObject> culumn_list =culumn_query.getResultList(false);
			JSONArray culumn_array = new JSONArray();//�ŷ�����Ϣ
			if(culumn_list.size()>0){//�з���
				
				for(BizObject culumn_o : culumn_list){
					//�������
					String classifyno = culumn_o.getAttribute("itemno").toString()==null ? "" : culumn_o.getAttribute("itemno").toString();
					String classifyname = culumn_o.getAttribute("itemname").toString()==null ? "" : culumn_o.getAttribute("itemname").toString();	
					
					//���ݷ����ѯ������Ϣ
					JSONObject helplist = getHelpList(classifyno);
					helplist.put("classify", classifyname);
					culumn_array.add(helplist);
				}
			}else{//�޷���
				//������Ŀ��ѯ������Ϣ
				JSONObject obj1 = getHelpList(classify);	
				obj1.put("classify", null);
				culumn_array.add(obj1);
			}
			
		} catch (JBOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return null;
	}

	/**
	 * ��ѯ������Ϣ�б�
	 * */
	private JSONObject  getHelpList (String classify) throws JBOException{
		JSONObject object = new JSONObject();
		JBOFactory f = JBOFactory.getFactory();
		List<BizObject> list = f.getManager("jbo.trade.inf_help").createQuery("classify=:classify").setParameter("classify",classify).getResultList(false);
		JSONArray array = new JSONArray();
		for(BizObject o : list){
			JSONObject help = new JSONObject();
			help.put("title", o.getAttribute("title").toString());
			help.put("content1", o.getAttribute("content1").toString());
			array.add(help);
		}
		object.put("helplist", array);
		return object;//����һ���������һ��JSON����
	}
}
