package com.amarsoft.p2ptrade.personcenter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ��ֵ��ʷ��¼��ѯ���� 
 * ��������� 
 * 		UserID:�˻���� 
 * 
 *   	���Ǳ��������
 * 		PageSize��ÿҳ������;
 *		CurPage����ǰҳ;
 *		StartDate����ʼ��������
 *		EndDate����ֹ��������
 *		TransStatus������״̬  
 * ������������б� 
 * 		SerialNo:��ˮ�ţ���ȷ���� 
 * 		InputTime:����ʱ��
 * 		Amount:��ֵ��� 
 * 		HandlCharge:������ 
 * 		RealCharge:ʵ�ʵ��˽�� 
 * 		TranMethod:���׷�ʽ
 * 		Status:״̬ 
 * 		Remark:��ע
 */ 
public class ChargeHistoryListHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	private String sTransStatus;
	private String sStartDate;
	private String sEndDate;
	private String sDates;
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getChargeHistoryList(request);
	}

	/**
	 * ��ȡ��ʷ��ֵ��¼
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getChargeHistoryList(JSONObject request)
			throws HandlerException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		Calendar cal = Calendar.getInstance();
		
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}

		if(request.containsKey("PageSize"))
			this.pageSize = Integer.parseInt(request.get("PageSize").toString());
		if(request.containsKey("CurPage"))
			this.curPage = Integer.parseInt(request.get("CurPage").toString());
		if(request.containsKey("Dates"))
			this.sDates = request.get("Dates").toString();
		if(request.containsKey("StartDate"))
			this.sStartDate = request.get("StartDate").toString();
		if(request.containsKey("EndDate"))
			this.sEndDate = request.get("EndDate").toString();
		if(!"".equals(sDates)){
			this.sEndDate = sdf.format(cal.getTime());
			this.sStartDate = getStartDate(sDates, cal, sdf);
		}
		if(request.containsKey("TransStatus"))
			this.sTransStatus = request.get("TransStatus").toString();
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo
					.getManager("jbo.trade.transaction_record");
			String sQuerySql = getQuerySql();
			BizObjectQuery query = m.createQuery(sQuerySql);
			query.setParameter("transtype1", "1010");//�������ͣ���ֵ
			query.setParameter("transtype2", "1011");
			query.setParameter("transtype3", "1012");
			query.setParameter("transtype4", "1050");
			query.setParameter("transtype5", "1013");
			query.setParameter("userid", sUserID);
			
			if(sTransStatus != null && !("".equals(sTransStatus))){
				if(sTransStatus.equals("010")){//��ֵ�ɹ�
					query.setParameter("status", "10");
				}else if(sTransStatus.equals("001")){//��ֵʧ��
					query.setParameter("status1", "04");
					query.setParameter("status2", "20");
					query.setParameter("status3", "00");
				}else if(sTransStatus.equals("100")){//��ֵ��
					query.setParameter("status1", "04");
					query.setParameter("status2", "20");
					query.setParameter("status3", "10");
					query.setParameter("status4", "00");
				}else if(sTransStatus.equals("110")){//�ɹ�+������
					query.setParameter("status1", "04");
					query.setParameter("status2", "20");
					query.setParameter("status3", "00");
				}else if(sTransStatus.equals("011")){//�ɹ�+ʧ��
					query.setParameter("status1", "04");
					query.setParameter("status2", "20");
					query.setParameter("status3", "10");
					query.setParameter("status4", "00");
				}else if(sTransStatus.equals("101")){//ʧ��+������
					sQuerySql = sQuerySql + " and status <> :status";
					query.setParameter("status", "10");
				}
			}
			if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
				query.setParameter("startdate", sStartDate + " 00:00:00");
				query.setParameter("enddate", sEndDate + " 23:59:59");
			}
			
			//��ҳ
			int totalAcount = query.getTotalCount();
    		int pageCount = (totalAcount + pageSize - 1) / pageSize;
    		if (curPage > pageCount)
    			curPage = pageCount;
    		if (curPage < 1)
    			curPage = 1;
    		query.setFirstResult((curPage - 1) * pageSize);
    		query.setMaxResults(pageSize);
    		
			List<BizObject> list = query.getResultList(false);
			JSONArray array = new JSONArray();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();

					String sSerialNo = o.getAttribute("SERIALNO").toString();// ��ˮ��
					String sInputTime = o.getAttribute("INPUTTIME").toString();// ����ʱ��
					String sTranstype = o.getAttribute("TRANSTYPE").toString();// ����ʱ��
					String sTranMethod = null;
					if(sTranstype.equals("1012")){//����
						sTranMethod = "����";
					}else{
						sTranMethod = "���п�";
					}

					double amount = Double.parseDouble(null == (o
							.getAttribute("AMOUNT").toString()) ? "0" : o
							.getAttribute("AMOUNT").toString());// ���׽��
					double handlCharge = Double.parseDouble(null == (o
							.getAttribute("HANDLCHARGE").toString()) ? "0" : o
							.getAttribute("HANDLCHARGE").toString());// ������
//					double realCharge = amount - handlCharge;// ʵ�ʵ��ʽ��
					
					double realCharge = Double.parseDouble(null == (o
							.getAttribute("ACTUALAMOUNT").toString()) ? "0" : o
							.getAttribute("ACTUALAMOUNT").toString());// ʵ�ʵ��ʽ��

					String sStatus = o.getAttribute("STATUS").toString();
					String sStatusName = null;
					//״̬ȷ��
					if (sStatus.equals("10")) {
						sStatusName = "�ɹ�";
					} else if (sStatus.equals("04") || sStatus.equals("20") || sStatus.equals("00")) {
						sStatusName = "ʧ��";
					} else {
						sStatusName = "��ֵ��";
					}
					String sRemark = o.getAttribute("REMARK").toString();

					obj.put("SerialNo", sSerialNo);// ��ˮ��
					obj.put("InputTime", sInputTime);// ����ʱ��
					obj.put("Amount", String.valueOf(amount));// ��ֵ���
					obj.put("HandlCharge", String.valueOf(handlCharge));// ������
					obj.put("RealCharge", String.valueOf(realCharge));// ʵ�ʵ��˽��
					obj.put("TranType", sTranstype);// ��������
					obj.put("TranMethod", sTranMethod);// ���׷�ʽ
					obj.put("Status", sStatus);// ����״̬code
					obj.put("StatusName", sStatusName);// ����״̬name
					obj.put("Remark", sRemark);// ��ע
					array.add(obj);
				}
			}
			
			JSONObject result = new JSONObject();
			result.put("RootType", "030");
			result.put("TotalAcount", String.valueOf(totalAcount));// �������´�����
			result.put("PageSize", String.valueOf(pageSize));
			result.put("CurPage", String.valueOf(curPage));
			result.put("array", array);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("querychargehistory.error");
		}
	}
	
	/**
	 * ��ȡʱ��ڵ���Ϣ
	 * @param sDates
	 * @return
	 */
	private String getStartDate(String sDates,Calendar cal,SimpleDateFormat sdf ){
		if("0".equals(sDates)){
			cal.roll(Calendar.DAY_OF_MONTH, -7);
			sStartDate  = sdf.format(cal.getTime());
		}else if("1".equals(sDates)){
			cal.roll(Calendar.MONTH, -1);
			sStartDate  = sdf.format(cal.getTime());
		}else if("2".equals(sDates)){
			cal.roll(Calendar.MONTH, -2);
			sStartDate  = sdf.format(cal.getTime());
		}else if("3".equals(sDates)){
			cal.roll(Calendar.MONTH, -3);
			sStartDate  = sdf.format(cal.getTime());
		}else if("4".equals(sDates)){
			cal.roll(Calendar.MONTH, -6);
			sStartDate  = sdf.format(cal.getTime());
		}
		return sStartDate;
	}
	
	
	private String getQuerySql(){
		String sQuerySql = "transtype in (:transtype1,:transtype2,:transtype3,:transtype4,:transtype5) and userid=:userid";
		if(sTransStatus != null && !("".equals(sTransStatus))){
			if(sTransStatus.equals("010")){//��ֵ�ɹ�
				sQuerySql = sQuerySql + " and status = :status";
			}else if(sTransStatus.equals("001")){//��ֵʧ��
				sQuerySql = sQuerySql + " and status in (:status1,:status2,:status3)";
			}else if(sTransStatus.equals("100")){//��ֵ��
				sQuerySql = sQuerySql + " and status not in (:status1,:status2,:status3,:status4)";
			}else if(sTransStatus.equals("110")){//�ɹ�+������
				sQuerySql = sQuerySql + " and status not in (:status1,:status2,:status3)";
			}else if(sTransStatus.equals("011")){//�ɹ�+ʧ��
				sQuerySql = sQuerySql + " and status in (:status1,:status2,:status3,:status4)";
			}else if(sTransStatus.equals("101")){//ʧ��+������
				sQuerySql = sQuerySql + " and status <> :status";
			}
		}
		if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
			sQuerySql = sQuerySql + " and inputtime between :startdate and :enddate";
		}
		sQuerySql = sQuerySql + " order by serialno desc";
		return sQuerySql;
	}
}
