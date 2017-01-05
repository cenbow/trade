package com.amarsoft.p2ptrade.account;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.MessageDigest;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.TimeTool;

/**
 * �û�ע�ύ��
 * ���������
 * 		userid:	�û�id
 * 		old_password:   ��ʼ����
 * 		new_password:	������	
 *      confirm_password  ȷ������
 *      question		��ȫ����
 *      answer			�����
 * ���������
 * 		SuccessFlag:�Ƿ�ע��ɹ�	S/F
 *
 */
public class CheckBankcardHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		//��ȡ����ֵ
		String userid = (String) request.get("userid");
		if(userid == null || userid.length() == 0){
			throw new HandlerException("common.emptyuserid");
		}
		JSONObject result = new JSONObject();
		//����Ƿ��쳣
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m1 = jbo.getManager("jbo.trade.account_info");
			BizObjectQuery query1 = m1.createQuery("select ISRETURNCARD from o where userid=:userid");
			query1.setParameter("userid", userid);
			System.out.println("111111111112222222222222222");
			List<BizObject> list = query1.getResultList(false);
			
			if(list != null){
			//	JSONArray array = new JSONArray();
				for(int i=0;i<list.size();i++){
					BizObject bizObj = list.get(i);
					if(bizObj!=null){
			//			JSONObject obj = new JSONObject();
						System.out.println("-----"+bizObj.getAttribute("ISRETURNCARD").getString());
						if("1".equals(bizObj.getAttribute("ISRETURNCARD").getString())){
							result.put("result1", "EXISTS");
			//				array.add(obj);
						}
					}
				}
			//	result.put("array", array);
			}
			getWithDrawCountByDate(result,userid);
			return result;
		}
		catch(JBOException je){
			je.printStackTrace();
			throw new HandlerException("default.database.error");
		}
	}
	
	  private void getWithDrawCountByDate(JSONObject result,
	            String sUserID) throws HandlerException {
	        double count = 0;
	        JBOFactory jbo = JBOFactory.getFactory();
	        // ��ȡ��ǰʱ��
	        TimeTool tool = new TimeTool();
	        String transdate = tool.getsCurrentDate();
	        // �޶�У��
	        try {
	            BizObjectManager manager;
	            manager = jbo.getManager("jbo.trade.transaction_record");
	            BizObjectQuery query = manager
	                    .createQuery("select count(*) as v.count from o where userid=:userid and inputtime like :inputtime and transtype =:transtype and status<>'04'");
	            query.setParameter("userid", sUserID)
	                    .setParameter("inputtime", transdate+"%")
	                    .setParameter("transtype", "1020");
	            BizObject o = query.getSingleResult(false);
	            if (o != null) {
	                count = Double.parseDouble(o.getAttribute("count").toString());
	            } else {
	                count = 0;
	            }
	            if (count >= ARE.getProperty("WithDrawCountByDate", 3)) {// �������ִ����Ѵﵽ���
	                result.put("is5", "T");
	                //throw new HandlerException("withdraw.limitcount.error");
	            }else{
	                result.put("is5", "F");
	                
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new HandlerException("getwithdrawcount.error");
	        }
	    }
}
