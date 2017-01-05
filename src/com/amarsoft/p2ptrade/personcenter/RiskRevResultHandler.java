package com.amarsoft.p2ptrade.personcenter;
/*
 * 
 * */

import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.TimeTool;

public class RiskRevResultHandler extends JSONHandler {
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getRiskRevResult(request);
	}

	/**
	 * �������������ѯ
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getRiskRevResult(JSONObject request)
			throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("userid.error");
		}

		String sUserID = request.get("UserID").toString();

		if (request.get("reviewsArray") == null
				|| "".equals(request.get("reviewsArray"))) {
			throw new HandlerException("reviewsArray.error");
		}
		
		//�û���������ָ���ַ���
		String itemString = request.get("reviewsArray").toString();
		
		try {
			JBOFactory jboItem = JBOFactory.getFactory();
			JSONObject result = new JSONObject();

			BizObjectManager managerItem = jboItem
					.getManager("jbo.trade.inf_card_value");

			/*
			 * �û���������ָ��ѡ������
			 * ��ֺ������ڽ�ǰ̨���ݵ��á�#�����ӵ�ÿ��ָ���ѡ����
			 * */
			String[] itemArray = itemString.split("#");
			
			//JSONArray riskArray = new JSONArray();
			
			String modelID = ""; //ģ���
			int sum = 0; //��������ܷ�
			String sDate = StringFunction.getTodayNow();
			System.out.println("����ʱ�䣺"+sDate);
			for (int i = 0; i < itemArray.length; i++) {
				//��ֺ�������ǰ̨���ݵ��á�%�����ӵ�ģ��ţ�ָ��ţ���ֵ�ŷֱ���
				String[] optionArray = itemArray[i].split("%");
				
				//ͨ��ģ��ţ�ָ��ţ���ֵ�Ų�ѯ�û�ѡ���ÿ��ķ�ֵ
				BizObjectQuery queryItem = managerItem
						.createQuery("select icv.value "
								+ "from jbo.trade.inf_card_value icv "
								+ "where icv.modelid = :modelid and icv.itemno = :itemno and icv.valueid= :valueid");
				queryItem.setParameter("modelid", optionArray[0]); //ģ���
				queryItem.setParameter("itemno", optionArray[1]); //ָ���
				queryItem.setParameter("valueid", optionArray[2]); //��ֵ��

				modelID = optionArray[0];
				BizObject mScore = queryItem.getSingleResult(false); //�û�ѡ�����ķ�ֵ

				if (mScore != null) {

						JSONObject objOption = new JSONObject();
						objOption.put("Value", mScore.getAttribute("VALUE")
								.getValue() == null ? 0 : mScore
								.getAttribute("VALUE").getInt());
						sum += mScore.getAttribute("VALUE").getInt(); //��ֵ���ӵó��ܷ�
						
				}
			}
			
			//����������¼
			BizObjectManager mRecordManager = jboItem.getManager("jbo.trade.inf_card_record");
			BizObjectQuery query = mRecordManager.createQuery("status=:status and userid=:userid");
            query.setParameter("status", "Y");
			query.setParameter("userid", sUserID);
			BizObject recordAccount = query.getSingleResult(true);
			//�û���¼����ʱ������ʷ��¼״̬��Ϊ��N��
			if(recordAccount !=null){
				recordAccount.setAttributeValue("Status", "N");
				mRecordManager.saveObject(recordAccount);
			}
			
			//��¼�û�����������¼
            BizObject bo = mRecordManager.newObject();
            bo.setAttributeValue("ModelID", modelID); //ģ���
            bo.setAttributeValue("UserID", sUserID); //�û�ID
            bo.setAttributeValue("Score", sum); //�ܷ�
            bo.setAttributeValue("TestTime", sDate); //����ʱ��
            bo.setAttributeValue("Status", "Y"); //״̬
            mRecordManager.saveObject(bo);	
            
            
            
            
//          //����ģ��ź��û�ID��ѯ����ID
//            BizObjectQuery queryRecord = mRecordManager
//					.createQuery("select icr.testid "
//							+ "from jbo.trade.inf_card_record icr "
//							+ "where icr.modelid = :modelid and icr.userid= :userid and icr.status='Y'");
//            queryRecord.setParameter("Modelid", modelID); //ģ���
//            queryRecord.setParameter("UserID", sUserID); //�û�ID
//            
//           
//           BizObject testIDObj = queryRecord.getSingleResult(false);
//           String testID = testIDObj.getAttribute("TESTID").toString();
//           //System.out.println("���Ժţ�"+testID);
//           
//           BizObjectManager mDetailManager = jboItem.getManager("jbo.trade.inf_card_record_detail");
//	       BizObject boDetail = mDetailManager.newObject();
//           for(int j = 0; j < itemArray.length; j++){
//              	//��ֺ�������ǰ̨���ݵ��á�%�����ӵ�ģ��ţ�ָ��ţ���ֵ�ŷֱ���
//              	String[] optinArray = itemArray[j].split("%");
//              	
//              	//ͨ��ģ��ţ�ָ��ţ���ֵ�Ų�ѯ�û�ѡ���ÿ��ķ�ֵ
//   				BizObjectQuery querywItem = managerItem
//   						.createQuery("select icv.value "
//   								+ "from jbo.trade.inf_card_value icv "
//   								+ "where icv.modelid = :modelid and icv.itemno = :itemno and icv.valueid= :valueid");
//   				querywItem.setParameter("modelid", optinArray[0]); //ģ���
//   				querywItem.setParameter("itemno", optinArray[1]); //ָ���
//   				querywItem.setParameter("valueid", optinArray[2]); //��ֵ��
//
//   				BizObject mScore = querywItem.getSingleResult(false); //�û�ѡ�����ķ�ֵ
//
//   				//��¼�û�������������ʷ��¼
//   				boDetail.setAttributeValue("TestID", testID); //���Ժ�
//              	boDetail.setAttributeValue("ModelID", optinArray[0]); //ģ���
//              	boDetail.setAttributeValue("ItemNo", optinArray[1]); //ָ���
//              	boDetail.setAttributeValue("ValueID", optinArray[2]); //��ֵ��
//              	boDetail.setAttributeValue("UserID", sUserID); //�û�ID
//              	boDetail.setAttributeValue("Value", mScore.getAttribute("VALUE").getInt()); //��ֵ
//              	//System.out.println("���������뿪����������"+testID+"&&&&&&&" +molID+"&&&&&&&"+optinArray[1]+"&&&&&&&"+optinArray[2]+"&&&&&&&"+sUserID+"������"+mScore.getAttribute("VALUE").getInt());
//              	mDetailManager.saveObject(boDetail);              	
//              }
           
			result.put("sum", sum);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("riskreviews.error");
		}

	}
}
