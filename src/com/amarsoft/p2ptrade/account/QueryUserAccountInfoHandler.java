package com.amarsoft.p2ptrade.account;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;

import com.amarsoft.account.common.ObjectBalanceUtils;
import com.amarsoft.account.common.ObjectConstants;
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
import com.amarsoft.p2ptrade.personcenter.BindingBankCardStatusHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;

/**
 * ��ѯ�û���ϸ��Ϣ
 * ���������
 * 		UserID					�û�ID
 * 		WithBindCard			��ѯ����Ϣ
 * 		WithBalance				�Ƿ��ѯ�����Ϣ��Ĭ��Ϊtrue
 * ���������
 * 		SuccessFlag				�ɹ���ʶ	S/F
 * 		NickName				�ǳ�
 * 		PhoneTel				�ֻ���
 * 		EMail					����
 * 		UsableBalance			�˻����ý��
 * 		FrozenBalance			������
 * 		HighRisk				�߷��տͻ���ʶ
 * 		PhoneAuthFlag			�ֻ���֤��ʶ
 * 		EMailAuthFlag			������֤��ʶ
 * 		UserAuthFlag			ʵ����֤��ʶ
 * 		InvestAuthFlag			�ʽ��������û���ʶ
 * 		UCUserID				�û�������ID
 * 		UCSubUserID				�û�������ID
 * 		TransPWDFlag			���������Ƿ�����	
 * 		SecurityQuestionFlag	��ȫ�����Ƿ�����
 * 		MobileChangeFlag		�����ֻ������ʶ
 * 		RetrievePasswordFlag	�����һ������ʶ
 * 		NewMobileNo				�����ֻ��������ֻ���
 * 		SecurityQuestion1		��ȫ����1
 * 		SecurityAnswer1			��ȫ��1
 * 		SecurityQuestion2		��ȫ����2
 * 		SecurityAnswer2			��ȫ��2
 * 		SecurityQuestion3		��ȫ����3
 * 		SecurityAnswer3			��ȫ��3
 * 		RealName    			��ʵ����
 * 		CertID					���֤��
 * 		Sexual					�Ա�
 * 		BornDate				��������
 * 		Education				���ѧ��
 * 		Marriage				����״��
 * 		City					����ʡ��
 * 		IndustryType			��ҵ����
 * 		Position				ְҵ
 * 		Income					������
 *		InComeLevel 			����ˮƽ
 * @author dxu
 *
 */
public class QueryUserAccountInfoHandler extends JSONHandler {

	static{
		Parser.registerFunction("sum");
		Parser.registerFunction("getitemname");
	}
	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String userID = (String) request.get("UserID");
		if(userID == null || userID.length() == 0){
			throw new HandlerException("queryuseracctinfo.emptyuserid");
		}
		
		JSONObject result = new JSONObject();
		try{
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager userAcctManager =jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = userAcctManager.createQuery("select * from o where UserID=:UserID");
			query.setParameter("UserID", userID);
			BizObject userAccount = query.getSingleResult(false);
			if(userAccount == null){
				throw new HandlerException("queryuseracctinfo.usernotexist");
			}
			int level =0;
			result.put("PhoneTel", toString(userAccount.getAttribute("PHONETEL").getValue()));
			result.put("UserName", toString(userAccount.getAttribute("USERNAME").getValue()));
			result.put("NickName", toString(userAccount.getAttribute("NickName").getValue()));
			result.put("EMail", toString(userAccount.getAttribute("EMAIL").getValue()));
			result.put("FrozenLockFalg", toString(userAccount.getAttribute("FrozenLockFalg").getValue()));
			getInvestAuthFlag(result, userID);
			//�޸�����ȡ����
			double usableBalance = 0.0;
			double frozenBalance = 0.0;
			if(request.containsKey("WithBalance") && "false".equalsIgnoreCase(request.get("WithBalance").toString())){
				//����ѯ������
			}
			else{
				HashMap<String,Double> balances = ObjectBalanceUtils.ObjectBalanceUtils(userID, ObjectConstants.OBJECT_TYPE_001);
				if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_001))
					usableBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_001); //��ѯ�������
				
				if(balances.containsKey(ObjectConstants.ACCOUNT_TYPE_002))
					frozenBalance = balances.get(ObjectConstants.ACCOUNT_TYPE_002); //��ѯ�������
			}
			
			
			result.put("UsableBalance", String.valueOf(usableBalance));
			result.put("FrozenBalance", String.valueOf(frozenBalance));
			
			/*result.put("UsableBalance", toString(userAccount.getAttribute("USABLEBALANCE").getValue()));
			result.put("FrozenBalance", toString(userAccount.getAttribute("FROZENBALANCE").getValue()));
			*/
			//
			result.put("HighRisk", toString(userAccount.getAttribute("HIGHRISK").getValue()));
			result.put("PhoneAuthFlag", toString(userAccount.getAttribute("PHONEAUTHFLAG").getValue()));
			result.put("EMailAuthFlag", toString(userAccount.getAttribute("EMAILAUTHFLAG").getValue()));
			result.put("UserAuthFlag", toString(userAccount.getAttribute("USERAUTHFLAG").getValue()));
			result.put("UCUserID", toString(userAccount.getAttribute("UCUSERID").getValue()));
			result.put("UCSubUserID", toString(userAccount.getAttribute("UCSUBUSERID").getValue()));
			result.put("TransPWDFlag", toString(userAccount.getAttribute("TRANSPWD").getValue()).length()>0?"Y":"N");
			result.put("SecurityQuestionFlag", toString(userAccount.getAttribute("SECURITYQUESTION").getValue()).length()>0?"Y":"N");
			result.put("SecurityQuestion1", toString(userAccount.getAttribute("SECURITYQUESTION").getValue()));
			result.put("SecurityAnswer1", toString(userAccount.getAttribute("SECURITYANSWER").getValue()));
			result.put("SecurityQuestion2", toString(userAccount.getAttribute("SECURITYQUESTION2").getValue()));
			result.put("SecurityAnswer2", toString(userAccount.getAttribute("SECURITYANSWER2").getValue()));
			result.put("SecurityQuestion3", toString(userAccount.getAttribute("SECURITYQUESTION3").getValue()));
			result.put("SecurityAnswer3", toString(userAccount.getAttribute("SECURITYANSWER3").getValue()));
			result.put("MobileChangeFlag", toString(userAccount.getAttribute("MOBILECHANGEFLAG").getValue()));
			result.put("RetrievePasswordFlag", toString(userAccount.getAttribute("RETRIEVEPASSWORDFLAG").getValue()));
			result.put("NewMobileNo", toString(userAccount.getAttribute("NEWMOBILENO").getValue()));
			result.put("EmergencyFlag", toString(userAccount.getAttribute("EmergencyPerson").getValue()).length()>0?"Y":"N");
			result.put("isvip", toString(userAccount.getAttribute("isvip").getValue()));
			
			BizObjectManager userAcctDetailManager =jbo.getManager("jbo.trade.account_detail");
			query = userAcctDetailManager.createQuery("select RealName,CertID,getitemname('Sex',Sexual) as v.Sexual,getitemname('IndustryType',INDUSTRIALTYPE) as v.INDUSTRIALTYPEN,BornDate,getitemname('EducationDegreeN',Education) as v.Education,getitemname('DistrictCode',prov) as v.prov, getitemname('DistrictCode',city) as v.city,INDUSTRIALTYPE,POSITION,INCOME,MARRIAGE,LIVESTATE,EMPLOYEETYPE,EMPLOYEETYPE,age from o where UserID=:UserID");
			query.setParameter("UserID", userID);
			BizObject userAcctDetail = query.getSingleResult(false);
			if(userAcctDetail != null){
				result.put("RealName", toString(userAcctDetail.getAttribute("REALNAME").getValue()));
				result.put("CertID", toString(userAcctDetail.getAttribute("CERTID").getValue()));
				String sCertID = toString(userAcctDetail.getAttribute("CERTID").getValue());
				if(sCertID!=null&&sCertID.length()>12){
					result.put("ShowCertID", toString(sCertID.replace(sCertID.substring(4, 12), "****")));
				}else{
					result.put("ShowCertID", "");
				} 
				result.put("Sexual", toString(userAcctDetail.getAttribute("SEXUAL").getValue()));
				result.put("INDUSTRIALTYPE", toString(userAcctDetail.getAttribute("INDUSTRIALTYPE").getValue()));
				result.put("BornDate", toString(userAcctDetail.getAttribute("BORNDATE").getValue()));
				result.put("Education", toString(userAcctDetail.getAttribute("EDUCATION").getValue()));
				result.put("City", toString(userAcctDetail.getAttribute("prov").getValue())+" "+toString(userAcctDetail.getAttribute("city").getValue()));
				result.put("IndustryType", toString(userAcctDetail.getAttribute("INDUSTRIALTYPEN").getValue()));
				result.put("Position", toString(userAcctDetail.getAttribute("POSITION").getValue()));
				result.put("InComeLevel", toString(userAcctDetail.getAttribute("INCOME").getValue()));
				result.put("Marriage", toString(userAcctDetail.getAttribute("MARRIAGE").getValue()));
				result.put("LiveState", toString(userAcctDetail.getAttribute("LIVESTATE").getValue()));
				result.put("EmployeeType", toString(userAcctDetail.getAttribute("EMPLOYEETYPE").getValue()));
				result.put("AGE", toString(userAcctDetail.getAttribute("age").getValue()));
				JSONObject o = getBindingBankCard(request);
				request.put("BankStatus", o.get("Status"));
				result.put("SuccessFlag", "S");
				if(request.containsKey("WithBindCard")){
					BindingBankCardStatusHandler  h = new BindingBankCardStatusHandler();
					result.put("CardResult", h.getBindingBankCard(request));
				}
				if("2".equals(result.get("UserAuthFlag")))
						level+=20;
				if("2".equals(result.get("PhoneAuthFlag")))
					level+=20;
				if("Y".equals(result.get("TransPWDFlag")))
					level+=20;
				if("2".equals(result.get("EMailAuthFlag")))
					level+=20;
				if("2".equals(result.get("BankStatus")))
					level+=20;
				result.put("level",level);
			}
			
			//������Ŀ����
		/*	 BizObjectManager m7 = jbo.getManager("jbo.trade.business_contract");
            BizObjectQuery query7 = m7.createQuery(
            		"select v.businesstype,v.productsubclass,sum(v.investsum) as v.investsum,sum(v.actualpayinteamt) as v.actualpayinteamt,count(v.subcontractno) as v.cnt "+
            		"from ( "+
            		"select bc.businesstype,uc.subcontractno,bc.productsubclass,sum(uc.investsum) as v.investsum,sum(id.actualpayinteamt) as v.actualpayinteamt "+
            		"from jbo.trade.business_contract bc,jbo.trade.user_contract uc "+
            		"left join jbo.trade.income_detail id on id.subcontractno=uc.subcontractno "+
            		"where uc.contractid=bc.serialno "+
            		"and uc.userid=:userid "+
            		"group by bc.businesstype,bc.productsubclass,uc.subcontractno "+
            		") "+
            		"group by v.productsubclass,v.businesstype");

				query7.setParameter("userid", userID);
				List<BizObject> list = query7.getResultList(false);
				JSONArray array = new JSONArray();
				if (list != null) {
					for (int i = 0; i < list.size(); i++) {
						JSONObject obj = new JSONObject();
						BizObject o = list.get(i);
						obj.put("businesstype", o.getAttribute("businesstype").getValue()==null?
								"":o.getAttribute("businesstype").getString());//��Ʒ���
						obj.put("productsubclass", o.getAttribute("productsubclass").getValue()==null?
								"":o.getAttribute("productsubclass").getString());//��Ʒ����
						obj.put("investsum", o.getAttribute("investsum").getValue()==null?
								"0":o.getAttribute("investsum").getDouble());//�ʲ�(Ԫ)
						obj.put("actualpayinteamt", o.getAttribute("actualpayinteamt").getValue()==null?
								"0":o.getAttribute("actualpayinteamt").getDouble());//����(Ԫ)
						obj.put("cnt", o.getAttribute("cnt").getValue()==null?
								"0":o.getAttribute("cnt").getInt());//��������
						array.add(obj);
					}
				}
			
			result.put("PRDARRAY", array);
			*/
			
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("queryuseracctinfo.error");
		}

		return result;
	}
	
	//��״̬
	public JSONObject getBindingBankCard(JSONObject request)
			throws HandlerException {
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}
		String sUserID = request.get("UserID").toString();

		try {
			JBOFactory jbo = JBOFactory.getFactory();

			BizObjectManager m = jbo.getManager("jbo.trade.account_info");

			BizObjectQuery query = m
					.createQuery("userid=:userid order by inputtime desc");
			query.setParameter("userid", sUserID);
			List<BizObject> list = query.getResultList(false);

			JSONObject obj = new JSONObject();
			if (list != null && list.size() != 0) {
				BizObject o = list.get(0);
				String sAccountNo = o.getAttribute("ACCOUNTNO")==null?"":o.getAttribute("ACCOUNTNO").toString();
				String sHideAccountNo = "";
				if(sAccountNo!=null && sAccountNo.length()>4)
					sHideAccountNo = "β��("
						+ sAccountNo.substring(sAccountNo.length() - 4,
								sAccountNo.length()) + ")";// sAccountNo.replace(sAccountNo.substring(4,
															// sAccountNo.length()-4),
															// "********");//���ؿ����м�Ĳ���
				String sStatus = o.getAttribute("STATUS")==null?"":o.getAttribute("STATUS").toString();

				String sSerilNo = o.getAttribute("SERIALNO")==null?"":o.getAttribute("SERIALNO").getString();
				String sAccountBelong = o.getAttribute("ACCOUNTBELONG").getString();

/*				if ("1".equals(sStatus)) {
					if (!"0302".equals(sAccountBelong) && (o.getAttribute("UPDATETIME").toString() == null
							|| "".equals(o.getAttribute("UPDATETIME")
									.toString()))) {
						sStatus = "6";
						changeCurrentCardStatus(jbo, sSerilNo, "6");
					}else if(CheckLastBindingCardLimit(jbo, sUserID, sSerilNo)){
						sStatus = "5";
						changeCurrentCardStatus(jbo, sSerilNo, "5");
					}
				}*/

				obj.put("Status", sStatus);// �Ƿ���֤
				obj.put("AccountNo", sAccountNo);// �˻���
				obj.put("HideAccountNo", sHideAccountNo);// ���غ���˻���
				obj.put("AccountName", toString(o.getAttribute("ACCOUNTNAME")));// �˻���
				obj.put("AccountBelong", sAccountBelong);// ��������
				obj.put("LimitAmount", String.valueOf(GeneralTools.numberFormat(o.getAttribute("LIMITAMOUNT").getDouble())));// �޶�
			} else {
				obj.put("Status", "1");// δ��֤
			}
			return obj;
		} catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("querybindingbankcard.error");
		}
	}
	
	private String toString(Object s){
		if(s == null) return "";
		else return s.toString();
	}
	
	
	//�ʽ���ˮ�û���ʶ
	private void getInvestAuthFlag(JSONObject result,String s){
		JBOFactory jbo = JBOFactory.getFactory();
		try {
			BizObjectManager m = jbo.getManager("jbo.trade.transaction_record");
			BizObjectQuery q = m.createQuery(" select count(AMOUNT) as  v.count from o where status='10' and TRANSTYPE not in ('050') and userid=:userid");
			q.setParameter("userid", s);
			BizObject o = q.getSingleResult(false);
			int count = 0;
			if(o!=null){
				count = o.getAttribute("count").getInt();
			}
			if(count>0)
				result.put("InvestAuthFlag", "Y");
			else
				result.put("InvestAuthFlag", "N");
		} catch (JBOException e) {
			e.printStackTrace();
		}
	}
	private int getCurrentAgeByBirthdate(String brithday) throws Exception {
	  try {
	   Calendar calendar = Calendar.getInstance();
	   SimpleDateFormat formatDate = new SimpleDateFormat("yyyy/mm/dd");
	   String currentTime = formatDate.format(calendar.getTime());
	   Date today = formatDate.parse(currentTime);
	   Date brithDay = formatDate.parse(brithday);
	 
	   return today.getYear() - brithDay.getYear();
	  } catch (Exception e) {
	   return 0;
	  }
	}
}
