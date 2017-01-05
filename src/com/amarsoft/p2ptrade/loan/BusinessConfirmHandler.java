package com.amarsoft.p2ptrade.loan;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

public class BusinessConfirmHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		return createJsonObject(request);
	}	
	
	private JSONObject createJsonObject(JSONObject request) throws HandlerException{
		JSONObject result=new JSONObject();
		String CustomerID=(String)request.get("CustomerID");//�ͻ����
		String Istenement=(String)request.get("Istenement");//�Ƿ�����ҵ 
		String Properties=(String)request.get("Properties");//��ҵ����
		String CreditReport=(String)request.get("CreditReport");//���ñ�����ʾ
		String LoanStartTime=(String)request.get("LoanStartTime");//������ʼʱ��
		String BuyHouseTime=(String)request.get("BuyHouseTime");//��������ʱ��
		double BuildSpace=Double.parseDouble(request.get("BuildSpace").toString().equals("")?"0":request.get("BuildSpace").toString());//�������
		double Sprice=Double.parseDouble(request.get("Sprice").toString().equals("")?"0":request.get("Sprice").toString());//��������۸�
		double LoanMoney=Double.parseDouble(request.get("LoanMoney").toString().equals("")?"0":request.get("LoanMoney").toString());//���������� 
		String HouseAdd=(String)request.get("HouseAdd");//������ַ 
		String Zip=(String)request.get("Zip");//�������� 
		String Iscarapply=(String)request.get("Iscarapply");//�����Ƿ�Ϊ������ 
		String Ispasstest=(String)request.get("Ispasstest");//�����Ƿ�ͨ�����
		String Isruncar=(String)request.get("Isruncar");//�Ƿ�ΪӪ�˳��� 
		String Carmodel=(String)request.get("Carmodel");//�����ͺ�
		String Insuremodel=request.get("Insuremodel").toString();//�����ѹ����� 
		String Insure1=(String)request.get("Insure1");//��ǿ�ձ���ֹ�� 
		String Insure2=(String)request.get("Insure2");//��ҵ�ձ���ֹ��
		String Insure3=(String)request.get("Insure3");//�����ձ���ֹ�� 
		String BuyCarTime=(String)request.get("BuyCarTime");//������ʱ��
		String Carlicense=(String)request.get("Carlicense");//���ƺ� 
		int 		CreditCards=Integer.parseInt(request.get("CreditCards").toString().equals("")?"0":request.get("CreditCards").toString());//���ÿ����� 
		double MaxLimit=Double.parseDouble(request.get("MaxLimit").toString().equals("")?"0":request.get("MaxLimit").toString());//�����
		double RecentlySixNo=Double.parseDouble(request.get("RecentlySixNo").toString().equals("")?"0":request.get("RecentlySixNo").toString());//�����������
		String UploadCreditReport=(String)request.get("UploadCreditReport");//�ϴ����֤
		String userid = (String)request.get("userid");
		String creditReportFile = request.get("creditReportFile")==null?"": request.get("creditReportFile").toString();
		String carIdentityFile = request.get("carIdentityFile")==null?"": request.get("carIdentity").toString();
		String houseIdentityFile = request.get("houseIdentityFile")==null?"":request.get("houseIdentity").toString();
		
		System.out.println(creditReportFile);
		System.out.println(carIdentityFile);
		System.out.println(houseIdentityFile);
		try {

			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.capital_info");
			BizObjectQuery query = m.createQuery("CustomerID=:userid");
			query.setParameter("userid", userid);
			
			BizObject o = query.getSingleResult(true);
			if(o==null){	
				o = m.newObject();
				o.setAttributeValue("CustomerID", userid);
			}
				o.setAttributeValue("Istenement", Istenement);
				o.setAttributeValue("Properties", Properties);
				o.setAttributeValue("CreditReport", CreditReport);
				o.setAttributeValue("LoanStartTime", LoanStartTime);
				o.setAttributeValue("BuyHouseTime", BuyHouseTime);
				o.setAttributeValue("BuildSpace", BuildSpace);
				o.setAttributeValue("Sprice", Sprice);
				o.setAttributeValue("LoanMoney", LoanMoney);
				o.setAttributeValue("HouseAdd", HouseAdd);
				o.setAttributeValue("Zip", Zip);
				o.setAttributeValue("Iscarapply", Iscarapply);
				o.setAttributeValue("Ispasstest", Ispasstest);
				o.setAttributeValue("Isruncar", Isruncar);
				o.setAttributeValue("Carmodel", Carmodel);
				o.setAttributeValue("Insuremodel", Insuremodel);
				o.setAttributeValue("Insure1", Insure1);
				o.setAttributeValue("Insure2", Insure2);
				o.setAttributeValue("Insure3", Insure3);
				o.setAttributeValue("BuyCarTime", BuyCarTime);
				o.setAttributeValue("Carlicense", Carlicense);
				o.setAttributeValue("CreditCards", CreditCards);
				o.setAttributeValue("MaxLimit", MaxLimit);
				o.setAttributeValue("RecentlySixNo", RecentlySixNo);
				o.setAttributeValue("UploadCreditReport", UploadCreditReport);
				m.saveObject(o);
				result.put("flag", "success");
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("savebusinessconfirm.error");
		}	
	return result;
	}
}
