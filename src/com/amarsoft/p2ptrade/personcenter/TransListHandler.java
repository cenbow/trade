package com.amarsoft.p2ptrade.personcenter;

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
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ������ϸ��ѯ���� 
 * ��������� 
 * 			UserID:�˻���� 
 * 	
 * 			���Ǳ��������
 * 			PageSize��ÿҳ������;
 *			CurPage����ǰҳ;
 *			StartDate����ʼ��������
 *			EndDate����ֹ��������
 *			TransType���������� 10����ֵ 20 ���� 30���տ�  40 ���� 50 Ͷ�� 60 �ſ�
 * ������������б� 
 * 			SerialNo:��ˮ�ţ���ȷ���� 
 * 			TransTime:ʱ��
 * 			TransTypeCode:���� 
 * 			TransTypeName:�������� 
 * 			InAmount:���� 
 * 			OutAmount:֧�� 
 * 			Balance:�˻���� 
 * 			Remark:��ע
 */
public class TransListHandler extends JSONHandler {
	private int pageSize = 10;
	private int curPage = 0;
	private String sStartDate;
	private String sEndDate;
	private String sTransType;
	
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		return getTransList(request);

	}

	/**
	 * ��ȡ������ϸ��ѯ�б�
	 * 
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getTransList(JSONObject request) throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}

		if(request.containsKey("PageSize"))
			this.pageSize = Integer.parseInt(request.get("PageSize").toString());
		if(request.containsKey("CurPage"))
			this.curPage = Integer.parseInt(request.get("CurPage").toString());
		if(request.containsKey("StartDate"))
			this.sStartDate = request.get("StartDate").toString();
		if(request.containsKey("EndDate"))
			this.sEndDate = request.get("EndDate").toString();
		if(request.containsKey("TransType"))
			this.sTransType = request.get("TransType").toString();
		
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			
			JSONObject statusObj = GeneralTools.getItemName(jbo, "TransCode");//��ȡ���н������͵���ֵ˵��
			
			BizObjectManager m = jbo
					.getManager("jbo.trade.transaction_record");
			String sQuerySql = getQuerySql();
			BizObjectQuery query = m.createQuery(sQuerySql);
			query.setParameter("userid", sUserID);
			if(sTransType != null && !("".equals(sTransType))){
				if(sTransType.equals("10")){//��ֵ(1010,1011,1012,1050)
					query.setParameter("transtype1", "1010");
					query.setParameter("transtype2", "1011");
					query.setParameter("transtype3", "1012");
					query.setParameter("transtype4", "1050");
				}else if(sTransType.equals("20")){//����(1020)
					query.setParameter("transtype", "102%");
				}else if(sTransType.equals("30")){//�տ�(1090)
					query.setParameter("transtype", "1090");
				}else if(sTransType.equals("40")){//����(1030,1032,1040)
					query.setParameter("transtype1", "1030");
					query.setParameter("transtype2", "1032");
					query.setParameter("transtype3", "1040");
				}else if(sTransType.equals("50")){//Ͷ��(1061)
					query.setParameter("transtype", "1061");
				}else if(sTransType.equals("60")){//�ſ�(1060)
					query.setParameter("transtype", "1060");
				}
			}else{
				query.setParameter("transtype1", "1070");
				query.setParameter("transtype2", "1071");
				query.setParameter("transtype3", "1080");
			}
			if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
				query.setParameter("startdate", sStartDate + "00:00:00");
				query.setParameter("enddate", sEndDate + "23:59:59" );
			}
			
			int firstRow = curPage * pageSize;
			if(firstRow < 0){
				firstRow = 0;
			}
			int maxRow = pageSize;
			if(maxRow <= 0){
				maxRow = 10;
			}
			query.setFirstResult(firstRow);
			if(request.containsKey("PageSize")){
				query.setMaxResults(maxRow);
			}
			
			int totalAcount = query.getTotalCount();
			int temp = totalAcount % pageSize;
			int pageCount = totalAcount / pageSize;
			if(temp != 0){
				pageCount += 1;
			}
			
			List<BizObject> list = query.getResultList(false);

			JSONArray array = new JSONArray();
			if (list != null) {
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					JSONObject obj = new JSONObject();

					String sUpdateTime = o.getAttribute("UPDATETIME").toString();// ����ʱ�䣨����ʱ�䣩

					double realAmount = Double.parseDouble(null == (o
							.getAttribute("ACTUALAMOUNT").toString()) ? "0" : o
							.getAttribute("ACTUALAMOUNT").toString());// ʵ�ʵ��ʽ��
					
					// ����������ֵ
					String sTransTypeCode = o.getAttribute("TRANSTYPE").toString();
					// ����������ֵ˵��
					String sTransTypeName  = statusObj.containsKey(sTransTypeCode)?statusObj.get(sTransTypeCode).toString():sTransTypeCode;
					
					if(sTransTypeName.contains("��ֵ")){//���ֳ�ֵ��ʽ������Ϊ��ֵ
						sTransTypeName = "��ֵ";
					}else if(sTransTypeName.contains("����")){//����������ʽ������Ϊ����
						sTransTypeName = "����";
					}else if(sTransTypeName.contains("����")){//���ֻ�����ʽ������Ϊ����
						sTransTypeName = "����";
					}else if(sTransTypeName.contains("�տ�") || sTransTypeName.contains("����")){//�����տ���ʽ������Ϊ�տ�
						sTransTypeName = "�տ�";
					}else if(sTransTypeName.contains("�����")){//���ֻ�����ʽ������Ϊ����
						sTransTypeName = "�ſ�";
					}
					
					String sDirection = o.getAttribute("DIRECTION").toString() == null ?"":o.getAttribute("DIRECTION").toString();//��������
					
					if(sDirection.equals("R")){//����
						obj.put("InAmount", String.valueOf(realAmount));// ������
						obj.put("OutAmount", "");// ֧�����
					}else{//֧��
						obj.put("InAmount", "");// ������
						obj.put("OutAmount", String.valueOf(realAmount));// ֧�����
					}
					
					String sBalance = o.getAttribute("BALANCE").toString() == null ?"0":o.getAttribute("BALANCE").toString();
					String sRemark = o.getAttribute("Remark").toString() == null ?"":o.getAttribute("Remark").toString();
					obj.put("TransTime", sUpdateTime);// ʱ��
					obj.put("TransTypeCode", sTransTypeCode);// ����code
					obj.put("TransTypeName", sTransTypeName);// ����name
					obj.put("Balance", String.valueOf(Double.parseDouble(sBalance)));// ���
					obj.put("Remark", sRemark);// ��ע

					array.add(obj);
				}
			}
			JSONObject result = new JSONObject();
			result.put("RootType", "030");// ������ʽΪ�б�
			result.put("TotalAcount", String.valueOf(totalAcount));// �������´�����
			result.put("PageCount", String.valueOf(pageCount));
			result.put("array", array);
			return result;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("queryusertranslist.error");
		}
	}

	private String getQuerySql(){
		String sQuerySql = "userid=:userid and status = '10'";
		if(sTransType != null && !("".equals(sTransType))){
			if(sTransType.equals("10")){//��ֵ(1010,1011,1012,1050)
				sQuerySql = sQuerySql + " and transtype in (:transtype1,:transtype2,:transtype3,:transtype4)";
			}else if(sTransType.equals("20")){//����(1020)
				sQuerySql = sQuerySql + " and transtype like :transtype";
			}else if(sTransType.equals("30")){//�տ�(1090)
				sQuerySql = sQuerySql + " and transtype = :transtype";
			}else if(sTransType.equals("40")){//����(1030,1032,1040)
				sQuerySql = sQuerySql + " and transtype in (:transtype1,:transtype2,:transtype3)";
			}else if(sTransType.equals("50")){//Ͷ��(1061)
				sQuerySql = sQuerySql + " and transtype  = :transtype";
			}else if(sTransType.equals("60")){//�ſ�(1060)
				sQuerySql = sQuerySql + " and transtype  = :transtype";
			}
		}else{
			sQuerySql = sQuerySql + " and transtype not in (:transtype1,:transtype2,:transtype3)";
		}
		if(sStartDate !=null && !("".equals(sStartDate)) && sEndDate != null && !("".equals(sEndDate))){
			sQuerySql = sQuerySql + " and updatetime between :startdate and :enddate";
		}
		sQuerySql = sQuerySql + " order by updatetime desc,serialno desc";
		return sQuerySql;
	}
	
}
