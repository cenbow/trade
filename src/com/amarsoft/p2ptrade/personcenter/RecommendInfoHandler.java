package com.amarsoft.p2ptrade.personcenter;

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

public class RecommendInfoHandler extends JSONHandler{
	private int pageSize = 1 ;
	private int pageNo = 0 ;//��ǰҳ
	@Override
	public Object createResponse(JSONObject request, Properties params)
			throws HandlerException {
		
		return selectUserApplyInfo(request);
	}

	public JSONObject selectUserApplyInfo(JSONObject request){
		
		JSONObject result=new JSONObject();
		String userId=(String)request.get("userId");
		JBOFactory jbo=JBOFactory.getFactory();
		try {
			BizObjectManager mg = jbo.getManager("jbo.trade.loan_apply");
			BizObjectQuery query = mg.createQuery("userid=:userid");
			query.setParameter("userid", userId);
			//��ҳ
			int recordTotal = query.getTotalCount();
			JSONObject pageInfo = setPage(request, recordTotal);
			query.setFirstResult((pageNo - 1) * pageSize);
			query.setMaxResults(pageSize);
			List<BizObject>  applyInfoList = query.getResultList(false);
			JSONArray array=new JSONArray();
			for (int i = 0; i < applyInfoList.size(); i++) {
				BizObject o = applyInfoList.get(i);
				JSONObject obj=new JSONObject();
				//��������ţ�������⣬�����ȣ�Ͷ���Ʒ��Ͷ�����ڣ�Ͷ��״̬������
				obj.put("applyno",o.getAttribute("applyno").toString()==null ? "" : o.getAttribute("applyno").toString());//��ţ���������ҳ��ѯ
				obj.put("projectname",o.getAttribute("projectname").toString()==null ? "" : o.getAttribute("projectname").toString());//�������
				obj.put("businesssum",o.getAttribute("businesssum").toString()==null ? "" : o.getAttribute("businesssum").toString());//������
				obj.put("applytime",o.getAttribute("applytime").toString()==null ? "" : o.getAttribute("applytime").toString());//����ʱ��
				obj.put("productid",o.getAttribute("productid").toString()==null ? "" : o.getAttribute("productid").toString());//Ͷ���Ʒ���
				obj.put("applystatus",o.getAttribute("applystatus").toString()==null ? "" : o.getAttribute("applystatus").toString());//����״̬��ʾ
				obj.put("userId",o.getAttribute("userid").toString()==null ? "" : o.getAttribute("userid").toString());//����״̬��ʾ
				
				array.add(obj);
			}
			result.put("RootType", "020");// ������ʽΪ�б�
			result.put("array", array);
			result.put("pageInfo", pageInfo);
		} catch (JBOException e) {
			e.printStackTrace();
		}
		return result;
	}
	//���÷�ҳ��Ϣ
	private JSONObject setPage(JSONObject request,int recordCount){
		JSONObject pageInfo =new JSONObject();
		if(request.containsKey("pageSize"))
			this.pageSize = Integer.parseInt(request.get("pageSize").toString());
		if(request.containsKey("pageNo"))
			this.pageNo = Integer.parseInt(request.get("pageNo").toString());	
		System.out.println("pageSize-->>"+pageSize);
		int pageCount = (recordCount + pageSize - 1) / pageSize;
		if (pageNo > pageCount)
			pageNo = pageCount;
		if (pageNo < 1)
			pageNo = 1;
		pageInfo.put("recordCount", String.valueOf(recordCount));
		pageInfo.put("pageNo", String.valueOf(pageNo));
		pageInfo.put("pageSize", String.valueOf(pageSize));
		return pageInfo;
	}
}
