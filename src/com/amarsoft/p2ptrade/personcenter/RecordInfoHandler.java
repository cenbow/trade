package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.ql.Parser;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ���������ѯ 
 * ���������  
 * 			UserID���˻����
 *			SerialNo����ˮ��
 * ���������
 * 			SerialNo����ˮ��
 * 			CreateTime������ʱ��
 * 			TransTType��������ϸ
 * 			Direction������
 * 			Balance�����
 * 			Amount����������
 */
public class RecordInfoHandler extends JSONHandler {	
	JSONObject result = new JSONObject();

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {

		Parser.registerFunction("getitemname");
		return getRecordInfo(request);

	}

	/**
	 * ��ȡ��¼
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject getRecordInfo(JSONObject request)
			throws HandlerException {
		
		//������ʼ��
		if (request.get("UserID") == null || "".equals(request.get("UserID")))
			throw new HandlerException("common.emptyuserid");

		if(!request.containsKey("SerialNo"))
			throw new HandlerException("common.emptyserialno");
			
		String SerialNo = request.get("SerialNo").toString();		
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.transaction_record");

			BizObjectQuery query = m.createQuery(" userid=:userid and serialno=:serialno");
			query.setParameter("userid", sUserID).setParameter("serialno", SerialNo);
	
			BizObject o = query.getSingleResult(false);
			if(o!=null){
				//������ˮ��
				String sSerialNo = o.getAttribute("SERIALNO").toString();
				//���״���ʱ��
				String sInputTime = o.getAttribute("INPUTTIME").toString();
				//���׽��
				double amount = Double.parseDouble(o.getAttribute("AMOUNT").toString()==null ? "0" : o.getAttribute("AMOUNT").toString());
				double in = 0.0;
				double out = 0.0;
				//���׷���
				String direction = o.getAttribute("DIRECTION").toString();
				//��ע˵��
				String remark = o.getAttribute("REMARK").toString();
				if("P".equals(direction)){
					 out = amount;
				}else{
					 in = amount;
				}
				//�˻����
				String balance = o.getAttribute("BALANCE").toString()==null?"":o.getAttribute("BALANCE").toString();
				//��������
				String transtype = o.getAttribute("TRANSTYPE").toString()==null?"":o.getAttribute("TRANSTYPE").toString();
				//����״̬
				String status = o.getAttribute("status").toString()==null?"":o.getAttribute("status").toString();
				//���ű��
				String sRelaAccount = o.getAttribute("RELAACCOUNT").toString()==null?"":o.getAttribute("RELAACCOUNT").toString();
				System.out.println("sRelaAccount*****************"+sRelaAccount);
				//Ͷ�ʹ����ĺ�ͬ�� ��ݺ�
				String sTRANSACTIONSERIALNO = o.getAttribute("TRANSACTIONSERIALNO").toString()==null?"":o.getAttribute("TRANSACTIONSERIALNO").toString();
				
				//������ʾ�Ľ���״̬  status ="10,01,03,04";//�ɹ��������������С�ʧ��
				String sStatusName = "";
				if("10".equals(status)){
					sStatusName = "�ɹ�";
				}else if ("01".equals(status)){
					sStatusName = "������";
				}else if ("03".equals(status)){
					sStatusName = "������";
				}else if ("04".equals(status)){
					sStatusName = "ʧ��";
				}
				
				//������ʾ�Ľ�������
				String sTransType = null;
				if("1010,1011,1012,1013,1015,1050".contains(transtype))
					sTransType = "��ֵ";
				else if("1020,1025".contains(transtype))
					sTransType = "����";
				else if("3010".contains(transtype))
					sTransType = "Ͷ��";
				else if("3050".contains(transtype))
					sTransType = "��������";
				else 
					sTransType = "����";
				
				//��ѯ���п���Ϣ
				if(!"".equals(sRelaAccount)){
					BizObjectManager m1 = jbo.getManager("jbo.trade.account_info");

					BizObjectQuery query1 = m1.createQuery(" select accountname,accountno,getitemname('BankNo',accountbelong) as v.bankname from o where serialno=:serialno and status='2'");
					query1.setParameter("userid", sUserID).setParameter("serialno", sRelaAccount);
			
					BizObject o1 = query1.getSingleResult(false);
					if(o1!=null){
						//�˻���
						String accountname = o1.getAttribute("accountname").toString();
						//�˻�����
						String accountno = o1.getAttribute("accountno").toString();
						//��������
						String bankname = o1.getAttribute("bankname").toString();
						
						result.put("AccountName", accountname);
						result.put("AccountNo", accountno);
						result.put("BankName", bankname);
					}
				}
				
				//��ΪͶ���Լ�������صĽ��ף���ѯ������Ŀ��Ϣ
				if("3010,3050".contains(transtype)){
					
					// ///==============�滻Ͷ�����ͽ�����������Ŀ��Ϣ��ȡ����====hhcai
					// 2015/03/18===================///////////
					String projectserialno;
					try {
						projectserialno = sTRANSACTIONSERIALNO.split("@")[0];
					} catch (Exception e) {
						// TODO: handle exception
						projectserialno = "";
					}
					if (projectserialno.length() != 0) {						
						BizObjectManager m1 = jbo
								.getManager("jbo.trade.project_info");
						BizObjectQuery query1 = m1
								.createQuery(" select o.projectname,o.loanterm from o where o.serialno=:serialno");
						query1.setParameter("serialno", projectserialno);

						BizObject o1 = query1.getSingleResult(false);
						if (o1 != null) {
							result.put("projectname", o1.getAttribute("projectname")==null?"":o1.getAttribute("projectname").toString());
							result.put("loanterm", o1.getAttribute("loanterm")==null?"":o1.getAttribute("loanterm").toString()+"����");
						}
					}
					
					/////==============�滻Ͷ�����ͽ�����������Ŀ��Ϣ��ȡ����====hhcai 2015/03/18===================///////////
					
					//�滻����
					/****
					BizObjectManager m1 = jbo.getManager("jbo.trade.project_info");

					BizObjectQuery query1 = m1.createQuery(" select o.projectname,o.enddate,o.endtime,uc.updatetime from o,jbo.trade.user_contract uc where o.contractid=:contractid and uc.userid=:userid and o.serialno=uc.projectid");
					query1.setParameter("userid", sUserID).setParameter("contractid", sTRANSACTIONSERIALNO);
			
					BizObject o1 = query1.getSingleResult(false);
					if(o1!=null){
						//��Ŀ����
						String projectname = o1.getAttribute("projectname").toString();
						//��������
						String enddate = o1.getAttribute("enddate").toString();
						//����ʱ��
						String endtime = o1.getAttribute("endtime").toString();
						//Ͷ��ʱ��
						String updatetime = o1.getAttribute("updatetime").toString();
						
						result.put("projectname", projectname);
						result.put("enddate", enddate);
						result.put("endtime", endtime);
						result.put("investtime", updatetime);
					}				
					***/
				}
				
				result.put("SerialNo", sSerialNo);
				result.put("InputTime", sInputTime);
				result.put("AmountIn", in);
				result.put("AmountOut", out);
				result.put("Balance", balance);
				result.put("TransType", sTransType);
				result.put("Status", sStatusName);
				result.put("Remark", remark);
			}
			return result;
		}
		catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("queryhistory.error");
		}
	}
}