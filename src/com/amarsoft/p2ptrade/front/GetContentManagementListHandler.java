package com.amarsoft.p2ptrade.front;

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
/**
 * �Ƽ�����Ŀ�б�
 * **/
public class GetContentManagementListHandler extends JSONHandler{
    static{
        Parser.registerFunction("count");
    }
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return proList(request);
	}
	
	public JSONObject proList(JSONObject request) throws HandlerException{
		JSONObject result = new JSONObject();
		JSONArray arry = new JSONArray();
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager loanManger = jbo.getManager("jbo.trade.content_management");
			BizObjectQuery q = loanManger.createQuery("select * from o order by loanterm");
			List<BizObject> list = q.getResultList(false);
			for(BizObject o : list){
			    JSONObject obj = new JSONObject();
			    String key="";
			    for(int i=0;i<o.getAttributeNumber();i++){
			        key=o.getAttribute(i).getName().toUpperCase();
			        if(key.contains("LOANTERM")){
			            obj.put("proNum",0);
			        }
			        obj.put(key,o.getAttribute(i).getValue()==null ? "" : o.getAttribute(i).getString());
			    }
				arry.add(obj);
			}
			result.put("filterList", arry);
		} catch (JBOException e) {
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}
		return result;
	}
	//��ȡ��Ӧ������ε���Ŀ����
	private int getProNumByLoanterm(JBOFactory jbo,String loanterm){
	    int proNum=0;
	    try {
	        //BizObjectQuery q = jbo.getManager("jbo.trade.project_info_listview").createQuery("select count(*) AS V.pronum from o ,jbo.trade.business_contract bc where o.contractid=bc.SERIALNO and loanterm=:loanterm and status in ('1','104','105','106')").setParameter("loanterm", loanterm);
	    	BizObjectQuery q = jbo.getManager("jbo.trade.project_info_listview").createQuery("select count(*) AS V.pronum from o  where o.remainamount>0 and loanterm=:loanterm and status in ('1','104','105','106')").setParameter("loanterm", loanterm);
	        
	    	BizObject r = q.getSingleResult(false);
	        proNum = r.getAttribute("pronum").getInt();
        } catch (JBOException e) {
            e.printStackTrace();
        }
	    return proNum;
	}
}
