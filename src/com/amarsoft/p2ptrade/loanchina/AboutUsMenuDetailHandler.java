package com.amarsoft.p2ptrade.loanchina;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/*
 * @aboutUsMenuDetail �������ġ���������
 * ���룺
 * classify ģ����
 * 
 * 
 * �����
 * menu_array �����Ŀ��Ϣ ������Ŀ���itemno����Ŀ����itemname�Լ���Ӧ��Ŀ��ҳ������leftpagelist  JSONArray
 * culumn_array ������classify��ҳ������helplist  JSONArray
 * content_array ��ű���title������content1  JSONArray
 * result �������յ�JSON����
 * 
 */

public class AboutUsMenuDetailHandler extends JSONHandler{
	private String classify;
	//ע��sql���õ��ĺ���
		static{
			Parser.registerFunction("substr");
		}
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return selectGuideDetail(request);
	}

	private JSONObject selectGuideDetail(JSONObject request) throws HandlerException {
		JSONObject result = new JSONObject();
		//��ȡģ����
		if(request.containsKey("classify")){
			if (request.get("classify")!=null) {
				this.classify = request.get("classify").toString();	
			}
		}	
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.inf_catalog");
			//ȡ��Ŀ��Ϣ
			BizObjectQuery menu_query = m.createQuery("length(itemno)=6 and substr(itemno,0,3)=:itemno and isinuse='1' order by sortno");
			menu_query.setParameter("itemno", classify);
			List<BizObject> list = menu_query.getResultList(false);
			JSONArray menu_array = new JSONArray();//����Ŀ��Ϣ
			for(BizObject o : list){
				JSONObject obj = new JSONObject();
				//��ȡmenu
				String menu_itemno = o.getAttribute("itemno").toString()==null ? "" : o.getAttribute("itemno").toString();//��Ŀ���
				obj.put("itemno",menu_itemno);
				obj.put("itemname",o.getAttribute("itemname").toString()==null ? "" : o.getAttribute("itemname").toString());//��Ŀ����						
				//��ȡ����
				BizObjectQuery culumn_query = m.createQuery("length(itemno)=9 and substr(itemno,0,6)=:itemno and isinuse='1' order by sortno");
				culumn_query.setParameter("itemno", menu_itemno);
				List<BizObject> culumn_list =culumn_query.getResultList(false);
				JSONArray culumn_array = new JSONArray();//�ŷ�����Ϣ
				if(culumn_list.size()>0){//�з���	
					for(BizObject culumn_o : culumn_list){
						//�������
						String classifyno = culumn_o.getAttribute("itemno").toString()==null ? "" : culumn_o.getAttribute("itemno").toString();//������
						String classifyname = culumn_o.getAttribute("itemname").toString()==null ? "" : culumn_o.getAttribute("itemname").toString();//��������	
						
						//���ݷ����ѯ������Ϣ
						JSONObject helplist = getHelpList(classifyno);
						helplist.put("classify", classifyname);
						culumn_array.add(helplist);
					}
				}else{//�޷���
					//������Ŀ��ѯ������Ϣ
					JSONObject obj1 = getHelpList(menu_itemno);	
					obj1.put("classify", null);
					culumn_array.add(obj1);
				}
				//��¼ҳ����Ϣ
				obj.put("leftpagelist", culumn_array);
				//��¼�˵���Ϣ
				menu_array.add(obj);
			 }
			 result.put("menulist", menu_array);//�������յ�JSON����
			return result;
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}
	/**
	 * ��ѯ������Ϣ�б�
	 * */
	private JSONObject  getHelpList (String classify) throws JBOException{
		JSONObject object = new JSONObject();
		JBOFactory f = JBOFactory.getFactory();
		List<BizObject> list = f.getManager("jbo.trade.inf_help").createQuery("classify=:classify and status='Y'").setParameter("classify",classify).getResultList(false);
		JSONArray content_array = new JSONArray();//��������Ϣ
		for(BizObject o : list){
			JSONObject help = new JSONObject();
			help.put("title", o.getAttribute("title").toString());
			help.put("content1", o.getAttribute("content1").toString());
			content_array.add(help);
		}
		object.put("helplist", content_array);
		return object;
	}
}
