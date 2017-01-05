package com.amarsoft.p2ptrade.invest;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amarsoft.app.accounting.util.ACCOUNT_CONSTANTS;
import com.amarsoft.app.accounting.util.NumberTools;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.util.DataConvert;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.tools.pdf.ModelParser;
import com.amarsoft.tools.pdf.imp.PDFCreator;
import com.amarsoft.tools.pdf.imp.XMLModelParser;
import com.amarsoft.tools.pdf.model.DocModel;

/**
 * �����ķ���ͬPDF
 * ��������� SubContractNo,UserID:
 * ��������� 
 */
public class CreatePDFHandler implements Runnable{
	
	String contractno = "";
	String userID = "";
	JBOFactory jbo = null;
	
	public CreatePDFHandler(String contractno,String userID, JBOFactory jbo){
		this.contractno = contractno;
		this.userID = userID;
		this.jbo = jbo;
	}
	
	public Object CreatePDF() throws HandlerException {
		return CreatePDFFile(contractno,userID,jbo);
	}

	/**
	 * �����ķ���ͬPDF
	 * 
	 * @param request
	 * @return 
	 * @throws HandlerExceptioncreateResponse
	 */
	@SuppressWarnings("unchecked")
	public String CreatePDFFile(String contractno,String userID,JBOFactory jbo) throws HandlerException {
		   
			String result="success";
			
			try{
				//aUserList :Ͷ������ϢID��ƽ̨name����ʵname�����֤֤��
				ArrayList<String> aUserList = new ArrayList<String>();
				//usertmpList Ͷ����ID
				ArrayList<String> usertmpList = new ArrayList<String>();
				//userNametmpList:��ʵ����
				ArrayList<String> userNametmpList = new ArrayList<String>();
				//userCertIDtmpList:���֤��
				ArrayList<String> userCertIDtmpList = new ArrayList<String>();
				//usernametmpList:ƽ̨����
				ArrayList<String> usernametmpList = new ArrayList<String>();
				ArrayList<String> userInvestMoneyList = new ArrayList<String>();
				//������
				deleteData(contractno,jbo);
				//ȡ���û���Ϣ
				aUserList = getUserList(contractno,jbo);
				//�����ļ��Ҳ����ļ�
				if(aUserList==null||aUserList.size()==0){
					throw new HandlerException("business.showhtml.fail");
				}
				for(int i=0;i<aUserList.size();i++){
					String tmp[] = aUserList.get(i).split("@",-1);
					String sSubContractNo = tmp[0];
					String sUserID = tmp[1];
					String sCertID = tmp[2];
					String sRealName = tmp[3];
					String sUserName = tmp[4];
					String sMoney = tmp[5];
					usertmpList.add(sUserID);
					userNametmpList.add(sRealName);
					userCertIDtmpList.add(sCertID);
					usernametmpList.add(sUserName);
					userInvestMoneyList.add(sMoney);
					//Ͷ���˺�ͬ
					CreateContractDoc(contractno,sSubContractNo,sUserID,jbo,userID,"Invest",null,null,null,null);
				}
				//����˺�ͬ
				CreateContractDoc(contractno,contractno,userID,jbo,userID,"Borrow",usernametmpList,userNametmpList,userCertIDtmpList,userInvestMoneyList);
			}catch(HandlerException he){
				he.printStackTrace();
				throw new HandlerException("business.showhtml.fail");
			}
			
			

		return result;
	}
	//ɾ������
	private static void deleteData(String contractno,JBOFactory jbo) throws HandlerException {
		try{
			BizObjectManager m = jbo.getManager("jbo.trade.contract_s_record");
			BizObjectQuery query = m.createQuery("delete from o where relativeno=:contractid and relativetype='002' and contracttype='002'");
			query.setParameter("contractid",contractno);
			query.executeUpdate();
		}catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
	}
	
	
	//ȡ��Ͷ������Ϣ
	private static ArrayList<String> getUserList(String contractno,JBOFactory jbo) throws HandlerException {
		ArrayList<String> aUserList = new ArrayList<String>();
		
		try{
			BizObjectManager m = jbo.getManager("jbo.trade.user_contract");
			BizObjectQuery query = m.createQuery("select o.subcontractno,o.investsum,o.userid,ad.certid,ad.realname,ua.username from o,jbo.trade.account_detail ad" +
					",jbo.trade.user_account ua " +
					" where  o.contractid=:contractid and o.status ='1' and o.relativetype='002' and o.userid = ad.userid and ua.userid=o.userid");
			query.setParameter("contractid",contractno);
			List<BizObject> list = query.getResultList(false);
			if(list!=null&&list.size()>0){
				for (int i = 0; i < list.size(); i++) {
					BizObject o2 = list.get(i);
					String sSubContractNo = o2.getAttribute("subcontractno").getValue()==null?"":
						o2.getAttribute("subcontractno").getString();//�Ӻ�ͬ��
					String sUserID = o2.getAttribute("userid").getValue()==null?"":
						o2.getAttribute("userid").getString();//�û���
					String sCertID = o2.getAttribute("certid").getValue()==null?"":
						o2.getAttribute("certid").getString();//�û���
					String sRealName = o2.getAttribute("realname").getValue()==null?"":
						o2.getAttribute("realname").getString();//�û���
					String sUserName = o2.getAttribute("username").getValue()==null?"":
						o2.getAttribute("username").getString();//�û���
					String sInvestSum = NumberTools.numberFormat((o2.getAttribute("investsum").getValue()==null?0:
						o2.getAttribute("investsum").getDouble()), ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION);//Ͷ�ʽ��
					aUserList.add(sSubContractNo+"@"+sUserID+"@"+sCertID+"@"+sRealName+"@"+sUserName+"@"+sInvestSum);
				}
			}else{
				throw new HandlerException("business.showhtml.fail");
			}
			
		}catch(JBOException e){
			e.printStackTrace();
			throw new HandlerException("default.database.error");
		}catch(HandlerException he){
			he.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
		
		
		return aUserList;
	}
	
	private static String CreateContractDoc(String contractno, String subcontractno,String sUserID,JBOFactory jbo,String userID,String contractType,
			ArrayList<String> usertmpList,ArrayList<String> userNametmpList,ArrayList<String> userCertIDtmpList,ArrayList<String> userInvestMoneyList) throws HandlerException{
		String result = "success";
		String sSerialnoForContract="";
		try {
			sSerialnoForContract = insertContractRecord( contractno,  subcontractno, sUserID, jbo, userID);
			HashMap<String,Object> responseParams = getResponseParams(sUserID,jbo,userID,subcontractno,contractno,contractType,usertmpList,userNametmpList,userCertIDtmpList);
			ArrayList<HashMap<String,String>> arrayMap = getPaymentSch(jbo,subcontractno,contractType,contractno);
			ArrayList<HashMap<String,String>> investMap = ((ArrayList<HashMap<String, String>>)responseParams.get("investMap"));
			//����ģ�ͽ�����
			ModelParser modelParser = new XMLModelParser();
			//���Ҫ�滻�ı�ǩֵ
			String str = getStr(responseParams,arrayMap,investMap,userInvestMoneyList,contractType);
			HashMap hm = new HashMap();
			//hm.put("test1", "����1");
			//��ϵģ�棬�����ĵ�ģ�Ͷ���
			DocModel docModel = modelParser.parse(str,  hm);
			//�����ĵ�������
			PDFCreator pc = new PDFCreator();
			//�ĵ�·��
			String subfolderPath = ARE.getProperty("subFolder").toString();
			String folderPath = ARE.getProperty("PDFPath").toString();
			
			if(subfolderPath.endsWith("/")){
				subfolderPath = subfolderPath.substring(0,subfolderPath.length()-1);
			}
			
			if(!subfolderPath.startsWith("/")){
				subfolderPath = "/"+subfolderPath;
			}
			
			if(folderPath.endsWith("/")){
				folderPath =folderPath.substring(0,folderPath.length()-1);;
			}
			
			File file = new File(folderPath);
			if(!file.exists()){
				file.mkdir(); 
			}
			
			String filePath = folderPath+subfolderPath+"/"+subcontractno+".pdf";
			
			//����pdf�ĵ�
			pc.create(docModel, new java.io.FileOutputStream(filePath));
			
		   //�����¼
			updateContractRecord( sSerialnoForContract,"3", subfolderPath+"/"+subcontractno+".pdf","",jbo);
			
		} catch (Exception e) {
			e.printStackTrace();
			try{
				updateContractRecord( sSerialnoForContract,"1", "",e.toString(),jbo);
			}
			catch(Exception ex){
				ex.printStackTrace();
			}
			throw new HandlerException("business.showhtml.fail");
		}
		return result;
	}
	
	private static String  insertContractRecord(String contractno, String subcontractno,String sUserID,JBOFactory jbo,String userID)throws HandlerException{
		try{
			BizObjectManager m = jbo.getManager("jbo.trade.contract_s_record");
			BizObject o = m.newObject();
			o.setAttributeValue("CONTRACTNO", subcontractno);//��ͬ���
			o.setAttributeValue("CONTRACTTYPE", "002");//��ͬ����
			o.setAttributeValue("RELATIVENO", contractno);//�������
			o.setAttributeValue("RELATIVETYPE", "002");//�����������
			o.setAttributeValue("LOANUSERID", userID);//����˱��
			o.setAttributeValue("INVESTUSERID", sUserID);//Ͷ���˱��
			o.setAttributeValue("SIGNTIME", StringFunction.getToday()+" "+StringFunction.getNow());//ǩ��ʱ��
			o.setAttributeValue("SIGNUSERID", sUserID);//ǩ����
			o.setAttributeValue("STATUS", "0");//��ͬ״̬
			
			m.saveObject(o);
			return o.getAttribute("SERIALNO").getString();
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
	}
	//�����¼
	private static void updateContractRecord(String serialno,String status,String sURL,String remark,JBOFactory jbo)throws HandlerException{
		try{
			BizObjectManager m = jbo.getManager("jbo.trade.contract_s_record");
			BizObject o = m.createQuery("serialno=:serialno").setParameter("serialno", serialno).getSingleResult(true);
			if(o!=null){
				o.setAttributeValue("STATUS", status);
				o.setAttributeValue("remark", remark);
				o.setAttributeValue("SAVEFILE", sURL);//����·��
				m.saveObject(o);
			}
			
		}catch (Exception e) {
			e.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
	}
	
	//��ѯ����PDF��Ҫ����
	/**
	 * @param sUserID Ͷ���� userID �����
	 * **/
	private static HashMap<String,Object> getResponseParams(String sUserID,JBOFactory jbo,String userID,String subcontractno,String contractno,String contractType
			,ArrayList<String> usertmpList,ArrayList<String> userNametmpList,ArrayList<String> userCertIDtmpList)throws HandlerException{
		 HashMap<String,Object> responseParams = new  HashMap<String,Object>();
		 //��ȡ�����˱��չʾ��Ϣ
		 ArrayList<HashMap<String,String>> investMap = new ArrayList<HashMap<String,String>>();
		 String sLenderName = "";
		 String sLenderCard = "";
		 String sBorrowName ="" ;
		 String sBorrowCard = "";
		// String sFundSource = "";
		 String sFundSourceDesc = "";
		 String sLoanRate ="";
		 String sLoanTerm = "";
		 String sRepaymentMethod = "";
		 String sPutoutDate = "";
		 String sMaturityDate = "";
		 String sGuaranorPerson ="";
		 String sGuaranorIDCard ="";
		 String sInvestsum2 = "";
		 String sInvestsum3 = "";
		 String sInvestsum = "";
		 String sBusinesssum2 = "";
		 String sBusinesssum = "";
		 String sBorrowUserName = "";
		 String sLenderUserName = "";
		 String mortgageperson = "";
		 String MORTGAGEIDCARD = "";
		 String MORTGAGEORGID ="";
		 String GUARANTORORGID = "";
		 String feerate = "";
		 try{
			 //�����
			 	BizObjectManager m = jbo.getManager("jbo.trade.account_detail");
				BizObjectQuery query = m.createQuery("select o.certid,o.realname,li.loanrate,li.loanterm,li.putoutdate," +
						"li.maturitydate,li.repaymentmethod,la.GUARANTORPERSON,la.GUARANTORIDCARD,la.FUNDSOURCEDESC,li.businesssum,ua.username, " +
						" la.mortgageperson,la.GUARANTORORGID,la.MORTGAGEIDCARD,la.MORTGAGEORGID,afli.feerate from o,jbo.trade.acct_loan li,jbo.trade.loan_apply la,jbo.trade.user_account ua,jbo.trade.acct_fee_loan_info afli" +
						"  where  o.userid=:userID  and o.userid=li.customerid and o.userid = ua.userid " +
						"and li.contractserialno=:contractno " +
						"and afli.feecode = '0001'  and afli.ISINUSE = '1' " +
						"and afli.loanserialno = li.baserialno " +
						"and la.precontractno = li.contractserialno");
				query.setParameter("userID",userID);
				query.setParameter("contractno",contractno);
				BizObject o = query.getSingleResult(false);
				if(o!=null){
					sBorrowName = o.getAttribute("realname").getValue()==null?"":o.getAttribute("realname").getString();
					sBorrowCard = o.getAttribute("certid").getValue()==null?"":o.getAttribute("certid").getString();
					sLoanTerm = Integer.toString(o.getAttribute("loanterm").getValue()==null?0:o.getAttribute("loanterm").getInt());
					sLoanRate = NumberTools.numberFormat((o.getAttribute("loanrate").getValue()==null?0:o.getAttribute("loanrate").getDouble()), ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION);
					sPutoutDate = o.getAttribute("putoutdate").getValue()==null?"":o.getAttribute("putoutdate").getString();
					sMaturityDate = o.getAttribute("maturitydate").getValue()==null?"":o.getAttribute("maturitydate").getString();
					sBorrowUserName = o.getAttribute("username").getValue()==null?"":o.getAttribute("username").getString();
					feerate = o.getAttribute("feerate").getValue()==null?"":o.getAttribute("feerate").getString();
					
					sRepaymentMethod = o.getAttribute("repaymentmethod").getValue()==null?"":o.getAttribute("repaymentmethod").getString();
					if("RPT000010".equals(sRepaymentMethod)){
						sRepaymentMethod = "�ȶϢ";
					}else if("RPT000040".equals(sRepaymentMethod)){
						sRepaymentMethod = "���»�Ϣ���ڻ���";
					}else if("RPT000020".equals(sRepaymentMethod)){
						sRepaymentMethod = "�ȶ��";
					}else if("RPT000045".equals(sRepaymentMethod)){
						sRepaymentMethod = "������Ϣ���ڻ���";
					}else if("RPT000050".equals(sRepaymentMethod)){
						sRepaymentMethod = "һ���Ի�����Ϣ����";
					}
					
					sFundSourceDesc = o.getAttribute("FUNDSOURCEDESC").getValue()==null?"":o.getAttribute("FUNDSOURCEDESC").getString(); 
					/*if("personal".equals(sFundSource)){
						sFundSource = "���˴���";
					}else if("company".equals(sFundSource)){
						sFundSource = "��ҵ����";
					}*/
					
					MORTGAGEIDCARD = o.getAttribute("MORTGAGEIDCARD").getValue()==null?"":o.getAttribute("MORTGAGEIDCARD").getString();
					MORTGAGEORGID = o.getAttribute("MORTGAGEORGID").getValue()==null?"":o.getAttribute("MORTGAGEORGID").getString();
					mortgageperson = o.getAttribute("mortgageperson").getValue()==null?"":o.getAttribute("mortgageperson").getString(); 
					GUARANTORORGID = o.getAttribute("GUARANTORORGID").getValue()==null?"":o.getAttribute("GUARANTORORGID").getString(); 
					sGuaranorPerson = o.getAttribute("GUARANTORPERSON").getValue()==null?"":o.getAttribute("GUARANTORPERSON").getString(); 
					sGuaranorIDCard = o.getAttribute("GUARANTORIDCARD").getValue()==null?"":o.getAttribute("GUARANTORIDCARD").getString(); 
					sBusinesssum = StringFunction.numberToChinese(NumberTools.round((o.getAttribute("businesssum").getValue()==null?0:o.getAttribute("businesssum").getDouble()), ACCOUNT_CONSTANTS.MONEY_PRECISION));
					sBusinesssum2 = DataConvert.toMoney(NumberTools.round((o.getAttribute("businesssum").getValue()==null?0:o.getAttribute("businesssum").getDouble()), ACCOUNT_CONSTANTS.MONEY_PRECISION));
				
				}
				
				//Ͷ����
				if("Invest".equals(contractType)){
					BizObjectManager m1 = jbo.getManager("jbo.trade.account_detail");
					BizObjectQuery query1 = m1.createQuery("select o.certid,o.realname,uc.investsum,ua.username from o,jbo.trade.user_contract uc,jbo.trade.user_account ua" +
								" where  o.userid=:userID  and o.userid=uc.userid and uc.contractid=:contractno and uc.subcontractno=:subcontractno and uc.status='1' and ua.userid=o.userid");
					query1.setParameter("userID",sUserID);
					query1.setParameter("contractno",contractno);
					query1.setParameter("subcontractno",subcontractno);
					BizObject o1 = query1.getSingleResult(false);
					if(o1!=null){
						sLenderUserName = o1.getAttribute("username").getValue()==null?"":o1.getAttribute("username").getString();
						sLenderName = o1.getAttribute("realname").getValue()==null?"":o1.getAttribute("realname").getString();
						sLenderCard = o1.getAttribute("certid").getValue()==null?"":o1.getAttribute("certid").getString();
						sInvestsum = StringFunction.numberToChinese(NumberTools.round((o1.getAttribute("investsum").getValue()==null?0:o1.getAttribute("investsum").getDouble()), ACCOUNT_CONSTANTS.MONEY_PRECISION));
						sInvestsum2 = DataConvert.toMoney(NumberTools.round((o1.getAttribute("investsum").getValue()==null?0:o1.getAttribute("investsum").getDouble()), ACCOUNT_CONSTANTS.MONEY_PRECISION));
						HashMap<String,String> lendMap = new HashMap<String,String>();
						lendMap.put("username", sLenderUserName);
						lendMap.put("realname", sLenderName);
						lendMap.put("certid", sLenderCard);
						investMap.add(lendMap);
					}
					
				}else if("Borrow".equals(contractType)){
					
					sInvestsum = sBusinesssum;
					sInvestsum2 = sBusinesssum2;
					subcontractno = contractno;
					
					for(int i=0;i<usertmpList.size();i++){
						
						if(i==usertmpList.size()-1){
							sLenderUserName = sLenderUserName + usertmpList.get(i);
							sLenderName = sLenderName + userNametmpList.get(i);
							sLenderCard = sLenderCard + userCertIDtmpList.get(i);
						}else{
							sLenderUserName = sLenderUserName + usertmpList.get(i) + ",";
							sLenderName = sLenderName + userNametmpList.get(i) + ",";
							sLenderCard = sLenderCard + userCertIDtmpList.get(i) + ",";
						}
						
					}
					HashMap<String,String> lendMap = new HashMap<String,String>();
					lendMap.put("username", sLenderUserName);
					lendMap.put("realname", sLenderName);
					lendMap.put("certid", sLenderCard);
					investMap.add(lendMap);
					
				}
		    responseParams.put("investMap", investMap);//�����˼���
			 responseParams.put("mortgageperson", mortgageperson);//������Ѻ��
			 responseParams.put("lendername", sLenderName);//Ͷ��������
			 responseParams.put("lenderid", sLenderUserName);//Ͷ����ID
			 responseParams.put("lendercard", sLenderCard);//Ͷ�������֤ID
			 responseParams.put("borrowname", sBorrowName);//���������
			 responseParams.put("borrowid", sBorrowUserName);//�����ID
			 responseParams.put("borrowcard", sBorrowCard);//��������֤ID
			 responseParams.put("fundsourcedesc", sFundSourceDesc);//�����;
			 responseParams.put("investsum", sInvestsum);//���������д��
			 responseParams.put("investsum2", sInvestsum2);//�������Сд��
			 responseParams.put("investsum3", sInvestsum3);//�������Сд��
			 responseParams.put("loanrate", sLoanRate);//���������
			 responseParams.put("loanterm", sLoanTerm);//�������
			 responseParams.put("RepaymentMethod", sRepaymentMethod);//���ʽ
			 responseParams.put("putoutdate", sPutoutDate);//��Ϣ��
			 responseParams.put("maturitydate", sMaturityDate);//������
			 responseParams.put("SubContractNo", subcontractno);//Ͷ�����Ӻ�ͬ
			 responseParams.put("guarantorperson", sGuaranorPerson);//��������  ��
			 responseParams.put("guarantoridcard", sGuaranorIDCard);//���������֤
			 responseParams.put("guarantororgid", GUARANTORORGID);//������Ӫҵִ��
			 responseParams.put("mortgageidcard", MORTGAGEIDCARD);//��Ѻ�����֤
			 responseParams.put("mortgageorgid", MORTGAGEORGID);//��Ѻ��Ӫҵִ��
			 responseParams.put("feerate", feerate);//���ս����
		 }catch(Exception e){
			 e.printStackTrace();
				throw new HandlerException("business.showhtml.fail");
		 }
		 return responseParams;
	}
	
	//ȡ����ƻ�
	private static ArrayList<HashMap<String,String>> getPaymentSch(JBOFactory jbo,String subcontractno,String contractType,String contractno) throws HandlerException{
		ArrayList<HashMap<String,String>> arrayMap = new ArrayList<HashMap<String,String>>();
		try{
			if("Invest".equals(contractType)){
				BizObjectManager m = jbo.getManager("jbo.trade.income_schedule");
				BizObjectQuery query = m.createQuery("select o.SeqId,o.PayDate,o.PayCorpusAmt,o.PayInteAmt from o  where o.subcontractno=:subcontractno order by seqid ");
				query.setParameter("subcontractno",subcontractno);
				List<BizObject> list = query.getResultList(false);
				if(list!=null&&list.size()>0){
					for (int i = 0; i < list.size(); i++) {
						BizObject o = list.get(i);
						int SeqId = o.getAttribute("SeqId").getValue()==null?0:o.getAttribute("SeqId").getInt();
						String PayDate = o.getAttribute("PayDate").getValue()==null?"":o.getAttribute("PayDate").getString();
						double dPayCorpusAmt = NumberTools.round(o.getAttribute("PayCorpusAmt").getValue()==null?0:o.getAttribute("PayCorpusAmt").getDouble(), ACCOUNT_CONSTANTS.MONEY_PRECISION);
						double dPayInteAmt = NumberTools.round(o.getAttribute("PayInteAmt").getValue()==null?0:o.getAttribute("PayInteAmt").getDouble(), ACCOUNT_CONSTANTS.MONEY_PRECISION);
						double dActualSum = NumberTools.round( dPayCorpusAmt+dPayInteAmt ,ACCOUNT_CONSTANTS.MONEY_PRECISION);
						HashMap<String,String> contMap = new HashMap<String,String>();
						contMap.put("SeqId", String.valueOf(SeqId));
						contMap.put("PayDate", PayDate);
						contMap.put("PayCorpusAmt", NumberTools.numberFormat(dPayCorpusAmt,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						contMap.put("PayInteAmt", NumberTools.numberFormat(dPayInteAmt,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						contMap.put("ActualSum", NumberTools.numberFormat(dActualSum,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						arrayMap.add(contMap);
					}
				}
			}else if("Borrow".equals(contractType)){

				BizObjectManager m = jbo.getManager("jbo.trade.acct_payment_schedule");
				BizObjectQuery query = m.createQuery("select o.SeqId,o.PayDate,o.PayCorpusAmt,o.PayInteAmt from o  where o.objectno=:subcontractno order by seqid ");
				query.setParameter("subcontractno",contractno);
				List<BizObject> list = query.getResultList(false);
				if(list!=null&&list.size()>0){
					for (int i = 0; i < list.size(); i++) {
						BizObject o = list.get(i);
						int SeqId = o.getAttribute("SeqId").getValue()==null?0:o.getAttribute("SeqId").getInt();
						String PayDate = o.getAttribute("PayDate").getValue()==null?"":o.getAttribute("PayDate").getString();
						double dPayCorpusAmt = NumberTools.round(o.getAttribute("PayCorpusAmt").getValue()==null?0:o.getAttribute("PayCorpusAmt").getDouble(), ACCOUNT_CONSTANTS.MONEY_PRECISION);
						double dPayInteAmt = NumberTools.round(o.getAttribute("PayInteAmt").getValue()==null?0:o.getAttribute("PayInteAmt").getDouble(), ACCOUNT_CONSTANTS.MONEY_PRECISION);
						double dActualSum = NumberTools.round( dPayCorpusAmt+dPayInteAmt ,ACCOUNT_CONSTANTS.MONEY_PRECISION);
						HashMap<String,String> contMap = new HashMap<String,String>();
						contMap.put("SeqId", String.valueOf(SeqId));
						contMap.put("PayDate", PayDate);
						contMap.put("PayCorpusAmt", NumberTools.numberFormat(dPayCorpusAmt,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						contMap.put("PayInteAmt", NumberTools.numberFormat(dPayInteAmt,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						contMap.put("ActualSum", NumberTools.numberFormat(dActualSum,ACCOUNT_CONSTANTS.SPACE_FILL,ACCOUNT_CONSTANTS.MONEY_PRECISION));
						arrayMap.add(contMap);
					}
				}
			
			}
			
		}catch(Exception e){ 
			e.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
		return arrayMap;
	}
	
	//����PDF����
	private static String getStr(HashMap<String, Object> responseParams,ArrayList<HashMap<String,String>> arrayMap,ArrayList<HashMap<String, String>> investMap,
			ArrayList<String> userInvestMoneyList,String contractType) {
		int lengths = responseParams.get("guarantorperson").toString().length();
		String sss = lengths==0?"":"��";
		String str1 = lengths==0?"":"������һ��";
		int length= responseParams.get("mortgageperson").toString().length();
		String ss = length==0?"":"��";
		String str2 = length==0?"":"�������� ��";
		String str3 = (str1.length()==0&&str2.length()==0)?"":"��";
		String str = "";
		str+="<?xml version=\"1.0\" encoding=\"GBK\"?>";
		str+="<doc>";
		str+="	<global>";
		str+="		<font id=\"defaultFont\" family=\"STSongStd-Light\" charset=\"UniGB-UCS2-H\" size=\"10\"></font>";
		str+="		<font id=\"headerfooter\" family=\"STSongStd-Light\" charset=\"UniGB-UCS2-H\" size=\"7\" color=\"#c9c9c9\"";
		str+="></font>";
		str+="		<headerfooter fontid=\"headerfooter\" border=\"\" align=\"\" content1=\"                                         \" content2=\"\"></headerfooter>";
		str+="		<page marginLeft=\"-1\" marginRight=\"-1\" marginTop=\"-1\" marginBottom=\"-1\" lineHeight=\"16\"></page>";
		str+="	</global>";
		str+="	<body>";


		str+="		<p fontSize=\"30\" fontColor=\"#000000\" align=\"1\" >���Э����  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"2\" idleft=\"10\" > Э���ţ� "+responseParams.get("SubContractNo")+"</p>";
		/*str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >�׷��������ˣ��� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >֤�����루���֤���룩�� "+responseParams.get("lendercard")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��      ����"+responseParams.get("lendername")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��������Ͷ����Eƽ̨�û�����"+responseParams.get("lenderid")+" </p>";
		str+="		<br/>";*/
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >�׷��������ˣ��� </p>";
		str+="		<br/>";
		str+="		<table colWidths=\"4,4,4,4,4\" borderWidth=\"1\">";
		str+="          <cell><p >ƽ̨�û���</p></cell>";
		str+="          <cell><p >��ʵ����</p></cell>";
		str+="          <cell><p >���֤����</p></cell>";
		str+="          <cell><p >������</p></cell>";
		str+="          <cell><p >�������</p></cell>";
		
        for (int i = 0; i < investMap.size(); i++) { 
        	HashMap<String,String> jsonString1 = investMap.get(i);
        	String[] uns = jsonString1.get("username").toString().split("\\,",-1);
        	String[] rns = jsonString1.get("realname").toString().split("\\,",-1);
        	String[] cis = jsonString1.get("certid").toString().split("\\,",-1);
        	//String[] its = jsonString1.get("investsum").toString().split("!");
        	//String[] lts = jsonString1.get("loanterm").toString().split("\\,");
        	for(int kk=0;kk<uns.length;kk++){
        		str+="          <cell><p >"+uns[kk]+"</p></cell>";
                str+="          <cell><p >"+rns[kk]+"</p></cell>";
             	str+="          <cell><p >"+cis[kk]+"</p></cell>";
             	
             	if("Borrow".equals(contractType)){
             		str+="          <cell><p >"+DataConvert.toMoney(userInvestMoneyList.get(kk))+"Ԫ</p></cell>";
             	}else{
             		str+="          <cell><p >"+responseParams.get("investsum2")+"Ԫ</p></cell>";
             	}
             	
             	str+="          <cell><p >"+responseParams.get("loanterm")+"����</p></cell>";
        	}
        	
        }
       
        
		str+="		</table>";
		str+="		<br/>";
		///
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >�ҷ�������ˣ��� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	֤�����루���֤���룩�� "+responseParams.get("borrowcard")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��      ����"+responseParams.get("borrowname")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >�񱾽����û����� "+responseParams.get("borrowid")+"</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	������ƽ̨��վ����  ��������Ͷ����Eƽ̨    </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��ַ��     http://www.houbank.cn        </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		�ͷ��绰��400-090-6588  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >���䣺csh001@qilerongrong.com.cn </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	���������չ�����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	���ƣ���������������Ϣ�������޹�˾ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >���䣺   bjbrtxyxgs@163.com</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >			���ڣ�  </p>";
		
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	1���׷���ָ�����Э���顷���µĳ����ˣ�����ƽ̨��վע�ᡣ������Ϊ�����л����񹲺͹����ɹ涨�ľ�����ȫ����Ȩ��������������Ϊ�������ܶ�����ʹ�ͳе���Э�����µ�Ȩ�����������Ȼ�ˣ���ŵ�Ա�Э���漰�Ľ�������ȫ��֧��ʹ���Ȩ�������������ʽ�Ϊ��Ϸ����ã���ŵ�������ṩ������ʵ���Ϸ�����Ч����������Ϣ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	2���ҷ���ָ�����Э���顷���µĽ���ˣ�����ƽ̨��վע�ᡣ�����Ϊ�����л����񹲺͹����ɹ涨�ľ�����ȫ����Ȩ��������������Ϊ�������ܶ�����ʹ�ͳе���Э�����µ�Ȩ�����������Ȼ�ˣ���ŵ�������ṩ������ʵ���Ϸ�����Ч����������Ϣ��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	3��������ָ��������Ͷ����Eƽ̨������̲���ڵļ����߹ɷ����޹�˾���»�����Ͷ����ƽ̨����̲���ڵļ����߹ɷ����޹�˾ӵ��http://www.houbank.cn�����¼�ơ�ƽ̨��վ�����ĺϷ���ӪȨ��Ϊ����������ҵ����ȫ��Ч�Ľ���ƽ̨��Ϊ�����ṩ��Ϣ��ѯ����Ϣ����</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	4��������ָ�����л����񹲺͹���������ע��������������ι�˾��Ϊ��Э�����µķ��չ����ˡ��������������й���ķ��ս�Ϊ������ƽ̨��վ�Ƽ��ڱ���ƽ̨���ʵ����н���˵�ծ���ձ�Э��Լ���ķ��ձ����ƶ��ṩ��߶���Ѻ������ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	5���ҷ��н�����󣬼׷���ͬ���ṩ���ҷ�ͬ��ͨ������ƽ̨�ͽ��������׷����������ϵ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	6��Ϊ�ҷ����б�Э�����µ�ȫ�����񣬵�������׷��ṩ�������α�֤����Э���һ�������Ϣ����û�б�֤���Ѻ��������Э���������һ��Լ׷���Ч���������µĵ����ļ�����Э��"+str1+str3+str2+"��  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	������Э��һ��ǩ������Э�飬��ͬ�������У�  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		1����������Ϣ  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		1.1 �����Ϣ </p>";
		str+="		<br/>";

		str+="		<table colWidths=\"6,6\" borderWidth=\"1\">";
		str+="          <cell><p >�����;</p></cell>";
		str+="          <cell><p>"+responseParams.get("fundsourcedesc")+"</p></cell>";
		str+="          <cell><p>���������д��</p></cell>";
		str+="          <cell><p>"+responseParams.get("investsum")+"</p></cell>";
		str+="          <cell><p>�������Сд��</p></cell>";
		str+="          <cell><p>"+responseParams.get("investsum2")+"Ԫ</p></cell>";
		str+="          <cell><p>���������</p></cell>";
		str+="          <cell><p>"+responseParams.get("loanrate")+"%</p></cell>";
		str+="          <cell><p>�������</p></cell>";
		str+="          <cell><p>"+responseParams.get("loanterm")+"����</p></cell>";
/*		str+="          <cell><p>ÿ�ڳ�����Ϣ����</p></cell>";
		str+="          <cell><p>"+arrayMap.get(0).get("InteAmt")+"</p></cell>";*/
		str+="          <cell><p>��������</p></cell>";
		str+="          <cell><p>"+responseParams.get("loanterm")+"��</p></cell>";
		str+="          <cell><p>���ʽ</p></cell>";
		str+="          <cell><p>"+responseParams.get("RepaymentMethod")+"</p></cell>";
		str+="          <cell><p>��Ϣ��</p></cell>";
		str+="          <cell><p>"+responseParams.get("putoutdate")+"</p></cell>";
		str+="          <cell><p>������</p></cell>";
		str+="          <cell><p>"+responseParams.get("maturitydate")+"</p></cell>";
		str+="          <cell><p>������</p></cell>";
		str+="          <cell><p> 	(  "+sss+"   )1����֤�ˣ���֤��ŵ������Э�鸽��һ     (  "+ss+"  )2����Ѻ�ˣ���Ѻ����������Э�鸽����</p></cell>";
		str+="		</table>";
		
		
		
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		1.2 ����ƻ��� </p>";
		str+="		<br/>";
		
		str+="		<table colWidths=\"4,4,4,4,4\" borderWidth=\"1\">";
		str+="          <cell><p >��������</p></cell>";
		str+="          <cell><p >������</p></cell>";
		str+="          <cell><p >���ڱ���</p></cell>";
		str+="          <cell><p >������Ϣ</p></cell>";
		str+="          <cell><p >�ϼ�</p></cell>";
		
        for (int i = 0; i < arrayMap.size(); i++) { 
        	HashMap<String,String> jsonString = arrayMap.get(i);
        	str+="          <cell><p >"+jsonString.get("SeqId").toString()+"</p></cell>";
            str+="          <cell><p >"+jsonString.get("PayDate").toString()+"</p></cell>";
         	str+="          <cell><p >"+jsonString.get("PayCorpusAmt").toString()+"</p></cell>";
         	str+="          <cell><p >"+jsonString.get("PayInteAmt").toString()+"</p></cell>";
         	str+="          <cell><p >"+jsonString.get("ActualSum").toString()+"</p></cell>";
        }
       
        
		str+="		</table>";
		
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2������Ȩ��������</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1 �׷���Ȩ��������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.1 �׷�Ӧ���ݱ����Ľ��׹������ߵ��ȷ�Ͻ����������;�����𡢽�������ʡ�������޵�����������Ľ��׹����ڴ���ָ���ҷ��������Ƽ����ڱ���ƽ̨�Ϸ�����������ɼ׷����ߵ��ȷ�Ͻ�����Ϊ�׷��ҷ������������ȷ�ϡ�����ǩ��󣬽��Э�鼴������������Ч��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.2 �׷���֤�������ڳ�����ʽ���Դ�Ϸ����׷��Ǹ��ʽ�ĺϷ������ˣ���������˶��ʽ�������Ϸ������ⷢ�����飬�ɼ׷������������ге����Ρ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.3 �׷�ͨ������ƽ̨���߽��ձ�Э��󣬼���Ϊ���ɳ�������Ȩ��������ͬ�ڽ����ϸ�������Ľ�������ʽ��ɼ׷��ڱ����������˻���ת���ҷ��ڱ����������˻��С���ת��Ͼ���Ϊ�ſ�ɹ������ռ�Ϊ�ſ��ա�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.4 �׷���������������Լ������Ϣ���档</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.5 ���ҷ�ΥԼ���׷���ȨҪ������ṩ���ѻ�õ��ҷ������Ϣ��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.6 ��������Э����������ͬ�⣬�׷����Ը����Լ�����Ը���б�Э����������ҷ�ծȨ��ת�ã�������ת�ú�ʱ֪ͨ��������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.7 �׷�Ӧ���н����������������ô����Ŀ��ܵ�˰��������ṩ���۴��ɷ���</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.8 ���ҷ���ƽ̨���漰��ʽ��ʱ����ÿ�ڻ�������Գ���Ӧ�����ı�����Ϣ�����ڷ�Ϣ�ģ��׷�ͬ�ⰴ���������ռ�ҷ���ƽ̨ȫ�������Ŀ��������ȡ���</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	    2.1.9 �׷��ڴ���ί�б�����ȡ�ҷ�Ӧ���׷��ı�Ϣ�������Э��Լ�����з��䣬�����ҷ�ÿ�ʻ���Ӧ�黹���ڱ���ƽ̨�������˻��У��ɱ������ձ�Э��Լ������֧�����������˵��տ��˻�����Ϣ����峥ǰ���������κη�ʽ����������ί�С�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.10 �׷�����ͨ��ƽ̨���ҷ���ɻ��������Э�飬�����ý����ҷ����κ�ֱ�ӻ��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.11 �׷�ͬ�ⰴ�ձ�Э���еķ��ս𵣱������ô���������𣬽���Э�����µ����ڷ�Ϣת�ø�������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.1.12 �ҷ����ձ�Э��Լ����ǰ����ģ��׷���Ȩ��ȡ��ǰ����ΥԼ��ΥԼ����㷽������Э��4.3����</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2 �ҷ���Ȩ��������  </p>";
		str+="		<br/>";
		

		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1 �ҷ�Ӧ�е����¸�������ֱ�����峥ծ��Ϊֹ��   </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1.1 ���������׷������������Ϣ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1.2 ������������֧��ƽ̨����ѣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1.3 ��������򶡷�֧�����չ���ѣ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1.4 �緢�����ڻ���ҷ���֧�����ڷ�Ϣ����Ϣ���㷽ʽ����Э��5.1���� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.1.5 �緢����ǰ����ҷ��谴��Э��Լ����׷�֧����ǰ����ΥԼ��ΥԼ����㷽ʽ����Э��4.3����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.2 �ҷ�Ӧ���ݱ�Э���Լ����ʱ�������չ�����֧�����ս𣬷��ս�Ϊ������5%��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.3 �ҷ�Ӧ����Э��Լ����;ʹ�ý�����Ų�á� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.4 �ҷ�Ӧȷ�����ṩ����Ϣ�����ϵ���ʵ��׼ȷ���Ϸ�����Ч�������������ṩ�����Ϣ��������Ҫ��ʵ��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.5 �ҷ�����ת�ñ�Э�����µ��κ�Ȩ������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.6 �ڼ׷���ծȨת�ú��ҷ���Ӧ���ձ�Э���Լ����ծȨ������֧��ÿ��Ӧ����Ϣ��������δ�ӵ�ծȨת��֪ͨΪ�ɾܾ����л������� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.7 �ҷ�ͬ�⣺�ҷ�ΥԼ��������Ȩ��׷��ṩ���ѻ�õ��ҷ�����������Ϣ������������Ȩ��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.8 �ҷ�Ӧ�������֧��ƽ̨����ѣ�  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.8.1 ��ƽ̨����ѡ���ָ�����Ϊ�ҷ�����ṩƽ̨�ƽ顢��������ǰ��������������������ͨ��ϵ�з�������ҷ�֧���������ı��ꣻ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.8.2 ��Э�������ҷ��������֧��ƽ̨�����Ϊ������1����ƽ̨���������Ϣ�տ�ʼ��ȡ�����ҷ�������ϱ�Э������Ȩ���������չ����˰��շ��ձ����ƶ�������⸶���ƽ̨�����֮��ֹ��֧����ʽΪ�� 2   ����1�����ɹ�ʱһ���Խ��ɣ�2�����ձ�Э�黹������ڻ����շ���֧����3�����ҷ��ͱ�������Э��ȷ���� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.2.8.3 ���ҷ��ͱ���Э��һ�µ���ƽ̨�����ʱ�����辭����Э���κ�������ͬ�⡣ </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3 ������Ȩ��������     </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3.1 �׷��ṩ���󣬱���Ӧ���ñʽ��ֱ�ӻ������ҷ�ָ���˻�������׷��ʽ���Դ�Ϸ���������¸ý����Ϊ��ȡ����˸��ҷ�����������ʧ�ģ��׷�Ӧ���е�ΥԼ���Ρ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3.2 ����Э������Լ���⣬����Ӧ�Լ׷����ҷ�����Ϣ����Э�����ݱ��ܣ����κ�һ��ΥԼ���������Ȩ������Ҫ�󣨰����������ڹ����졢�������ء�˰�񡢽��ڼ�ܻ����ȣ���������Ȩ��¶�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3.3 ������Ȩ���ݱ�Э����ȡ��Ӧ��ƽ̨����ѣ��շѱ�׼��2.2.8.2����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3.4 ��Э�����ȷ�ϲ�ͬ����������뱾Э���йص����������ļ��͵�����Ϣ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		2.3.5 ������Ϊ�ҷ����׷��ṩ���е��Ӽ�ƽ̨���񣬱��������ҷ��ͱ�֤�˵ĸ������á��ҷ��Ļ��������������κ���ʽ�ĳ�ŵ��Ҳ���Լ׷����ʽ���Դ�ĺϷ��ԡ���ȫ�������κ���ʽ�ĳ�ŵ��  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3������   </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3.1 �ҷ�Ӧ�ڱ�Э��Լ���Ļ����պͻ�����������׷��黹�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3.2 �ҷ���ÿ�ڻ���Ӧ��������˳���峥��     </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		��1��ΥԼ�𣻣�2�����ڷ�Ϣ����3����Ϣ����4�����𣻣�5�����չ���ѣ���6��ƽ̨����ѡ�   </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3.3 ����ʱ�䶨�����£���Ϣ��Ϊ�ҷ��������𡣻�����Ϊ�������ϸ�����������ҷ�Ӧ�����������ڡ�  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3.4 �ҷ���Ȩ�����ڻ����մ��ҷ��ڱ��������˻��н��ʽ�ת���׷��ڱ����������˻������������˻��������˻���</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		3.5 ���ҷ��ڱ��������˻��е��ʽ�����֧������Ӧ������ʱ���ҷ���Ȩ���������ҷ��ڱ����˻��󶨵������˻��д��۲�   </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4����ǰ����   </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.1 ��Э�����½��ò�����ǰ�������ǰ����Ӧ���黹ȫ����Ϣ���ҷ�������ǰ���Ӧ�����������ɱ�����Ϊ������ǰ����������� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.2 �ҷ�������ǰ����ģ�Ӧ�����ڱ���ƽ̨�������˻��д�������ʽ𣬲�������˳������峥Ӧ�������1����ǰ����ΥԼ��2��Ӧ����Ϣ����3��ʣ�౾�𣻣�4�����չ���ѣ���5��ƽ̨����ѡ������˻�������ʱ���δ�յ��������ģ���Ϊ�ҷ�δ�����ǰ�������룬�ҷ���Ӧ������ԭԼ����� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.3 ��ǰ����ΥԼ��ļƷѷ�ʽΪ��ΥԼ��ʣ�౾������������/360��ʣ��������50%��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.4 �����֧������Э��Լ�������ƽ̨����ѡ�ƽ̨����ѵļ��㷽ʽΪ��ƽ̨����ѣ�ʣ�౾���ƽ̨����ѷ���(1%)�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.5 �򶡷�֧������Э��Լ������ķ��չ���ѡ����չ���ѵļ��㷽ʽΪ�����չ���ѣ�ʣ�౾������չ���ѷ���(2%)��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		4.6 �������������ǰ����ʱ����δ��������Ӧ������ģ���Ӧ�Ƚ����������ڿ���󷽿���ǰ��� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		5�����ڻ���</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		5.1 �ҷ����ڻ���ģ�Ӧ��׷�֧�����ڷ�Ϣ��ֱ���峥���֮�ա����ڷ�Ϣ���㷽ʽΪ�����ڷ�Ϣ=���ڿ����еĽ������������ʡ�1.5��360��ʵ���������������ڷ�Ϣ���Ƹ�����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		5.2 ���������ڻ���ʱ���仹�����������峥ȫ������Ӧ������ģ������Ӧ������˳��֧��Ӧ�������1��Ӧ��δ���ķ��չ�����ܺͣ���2��Ӧ��δ����ƽ̨������ܺͣ���3�����ڷ�Ϣ����4����Ϣ����5���������У����ڷ�Ϣ����Ϣ�򱾽�Ļ���˳��Ӧ������Ƿ��ʱ�䰴�Σ��ڳ����� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		5.3 �ҷ����ڻ���ģ��׷��������붡��ǩ���ġ�������Ȩί���顷����Ȩ�������ҷ�����Ƿ����ա���ʾ��׷����������Ȩ����������תί�����������������ٻ�ȡ�׷����ҷ�֮�Ͽɡ� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		6�������ǰ���� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		���ҷ���������֧���κ�һ�ڻ����90�졢���������������ϣ������ڣ����ۼ����ڴ��������ϣ������ڣ������ڽ��ɹ�������ӱܡ�ʧ����ܾ�����Ƿ����ʵ����Ӫ״���������ض񻯡�����ת�ƲƲ��ȶ�����Ϊʱ���׷���Ȩ�������������ǰ���ڲ���ʹ�տ�Ȩ�������ں��ҷ�Ӧ�����峥��Э�����µ����ڷ�Ϣ����Ϣ��Ӧ��δ����ȫ�����𡢷��չ���ѡ�ƽ̨������Լ����ݱ�Э��������������á� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7�����ձ���</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.1 Ϊ��֤�ҷ����б�Э�����µ�Ȩ�����񣬵�������׷��ṩ�������α�֤�������Ե������ṩ�ı�֤��ŵ����Э��"+str1+str3+str2+"��Լ��Ϊ׼��   </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.2 �ҷ�Ӧ��������  "+responseParams.get("feerate")+"%����չ����˽��ɷ��ս𣬷��չ����˿����ķ��ս�ר���˻������³ơ����ս��˻�����Ϊ��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		         �˻����ƣ���������������Ϣ�������޹�˾����׼���� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		         �˺ţ�801110801421014798</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		        �����У�  ��̲���ڵļ�������������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.3 �����������һ�ڻ�����δ�ܰ���������Ϣ����10��������ʱ�����չ�����Ӧ��������ķ��ս���д����� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.4 ���չ����˴�Ϊ����ծ�������ȡ�����Ϊ����ծ���׷��Ȩ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.5 ���ս�����ڷ��չ����˵������ʽ𣬷��չ����˶Է��ս��ӵ�й���ʹ��Ȩ����������������Ӫ���峥����ծ������ڷ��ս���Ŀ������������κ���;�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		7.6 �����������չ�����֧���ķ��չ����Ϊ������2�������չ��������Ϣ�տ�ʼ��ȡ�����ҷ�������ϱ�Э������Ȩ������֮��ֹ������Ѱ�[��]���ᡣ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		          ��������������������Ϣ�������޹�˾           </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		          �˺� : 801100701421013360           </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		         ��������:��̲���ڵļ�����Ӫҵ��          </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		8��ΥԼ����  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.1 ������Ӧ�ϸ����б�Э�飬�Ǿ�����Э��һ�»����ձ�Э��Լ�����κ�һ�����ñ��������Э�顣 </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.2 �ҷ����ý������������������Ӫ����������ķ��루������������Ͷ�ʣ������Ʊ�������Ʊ�������ڻ��Ƚ��ڲ�Ʒ��ת���ȣ�������������Ϊ���׷�����ǰ�ջؽ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.3 ���������κ�һ�����εģ��������ҷ�ΥԼ��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.3.1 �ҷ�Υ�����ڱ�Э���������κγ�ŵ�ͱ�֤�ģ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.3.2 �ҷ����κβƲ�����û�ա����á���⡢��Ѻ������ȿ���Ӱ������Լ�����Ĳ����¼����Ҳ��ܼ�ʱ�ṩ��Ч���ȴ�ʩ�ģ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.3.3 �ҷ��Ĳ���״������Ӱ������Լ�����Ĳ����仯���Ҳ��ܼ�ʱ�ṩ��Ч���ȴ�ʩ�ġ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.4 ��������8.3���������ζ������ҷ�ΥԼ�ģ�����ݼ׷������������ж��ҷ����ܷ���ΥԼ�¼��ģ�������ͬ�⣬�׷���Ȩί�б�����ȡ�����κ�һ�����ȼô�ʩ��  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.4.1 �����ݻ���ȡ������ȫ���򲿷ֽ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.4.2 �����ѷ��Ž��ȫ����ǰ���ڣ������Ӧ������������Ӧ����� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.4.3 �����Э�飻</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.4.4 ��ȡ���ɡ������Լ���Э��Լ���������ȼô�ʩ��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.5 �κ�һ��ΥԼ��ΥԼ��Ӧ�е���ΥԼ��������������ɵ���ʧ�����ã��������Ϸѡ����չ���ѡ�ƽ̨����ѡ��Լ��������ʦ�ѵȡ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 8.6 �ҷ�ͬ�Ⲣ��Ȩ�������ҷ������ڼ�¼¼������ϵͳ��</p>";
		
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		9�����÷��ɼ������� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >      ��Э���ǩ�������С���ֹ�����;������л����񹲺͹����ɣ���Լ���ɱ������ڵص�����Ժ��Ͻ��   </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		10��֪ͨ</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >  10.1 �Ա�Э�鶩��֮���𣬱�Э����������������������������Ϣ���������������֪ͨ��������������������ϵ�˵���Ч�����Ϣ����ס�ء��ֻ����롢�������䡢�����˻��ȡ������κ�һ������ʱ�ṩ���������Ϣ����������ʧ��������Ӧ�ɸ÷����ге��� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >  10.2 ��Э�������κ�һ�����ݱ�Э��Լ��������֪ͨ��������ļ���Ӧ��������ʽ��������ί�б���ͨ��ָ�������������������ڹ�����Ե������ݷ�ʽ������������������</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		11����������  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > ����Ӧ�����ڱ�Э�鼰�丽����ͬ���ļ���ǩ�������й����л�õ��йر�Э�����µ������Լ���˵������йص��κ��ļ������ϻ���Ϣ��Ϊ������Ϣ��������¶��ʹ�ã��������ȵõ���������������ͬ�⡣ </p>";
		
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		12������  </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		 12.1 ��Э����õ����ı���ʽ�Ƴɣ���ί�б������������뱾Э���й������ļ��������Ϣ���������Ͽɸ���ʽ��Э��Ч����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		 12.2 ��Э�����µĸ����Ͳ���Э�鹹�ɱ�Э�鲻�ɷָ��һ���֡�����Э����κ�һ��Ĳ��ֻ�ȫ�����϶�Ϊ��Ч���߲�����ִ������������Э�������κ��������Ч�Ի�ִ��������ǰ�ù涨�����Σ�����Ӧ��ʱ����Э�̣���ɱ�Ҫ��Э�飬�Խ����϶�Ϊ��Ч����ִ�����ĸ������û�Ϊ����޶ȵ����պ�ͬĿ�ĺ;�����涨���������  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		��������ǩ�����У�����˫����Ϣ�ɱ���ϵͳ�Զ���ɲ���������˫��ͨ�����ߵ����ʽȷ�ϱ�Э��Ķ����������ĵ���ǩ������дǩ�����߸��¾���ͬ�ȵķ���Ч����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		 �׷���"+responseParams.get("lendername")+"  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		 �ҷ���"+responseParams.get("borrowname")+"  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		 ������  ��������Ͷ����Eƽ̨  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		��������������������Ϣ�������޹�˾</p>";
		str+="		<br/>";
		//����һ����֤��ŵ��
		if(((String) responseParams.get("guarantorperson")).length()!=0){
		str+="		<br/>";
		str+="		<br/>";	
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		����һ�� </p>";
		str+="		<br/>";	
		str+="		<br/>";	
		str+="		<p fontSize=\"30\" fontColor=\"#000000\" align=\"1\" >��֤��ŵ�� </p>";
		str+="		<br/>";	
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >"+responseParams.get("guarantorperson")+"���³ơ���֤�ˡ���֪Ϥ����ƽ̨��վ���Ϊ "+responseParams.get("SubContractNo")+"�����Э���顷�����¼�ƽ��Э�飩��ծȨ�ˣ��������ˣ���ϸ������Э�飩 ��"+responseParams.get("borrowname")+"(���³ơ�����ˡ���ǩ���ı��Ϊ  "+responseParams.get("SubContractNo")+"�ġ����Э���顷����ԸΪ�ý��Э�����½����֮ծ���ṩ�������α�֤��</p>";
		str+="	    <p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 1����֤��ΧΪ�����Э���顷���µ�ȫ��ծ�񣬰�����������ΥԼ�����ڷ�Ϣ����Ϣ�����𡢷��չ���ѡ�ƽ̨����ѡ�ʵ��ծȨ�ķ��á�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 2����֤��ȷ�ϣ���ծ����ΥԼʱ����������֤��ŵ���涨�ı�֤��Χ�е������������Ρ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 3������֤��ŵ��������Э����Ϊ�κ�ԭ��������Ĳ�����������Ч����Ч��������Ч�򱻳������������Ӱ�챣֤����Լ���� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > 4������֤��ŵ����Ч�ڼ�Ϊ���Э������ծ���������޽���֮�������ꡣ</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5����֤�������ͱ�֤���£� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.1 ��֤��Ϊ����ע�Ტ�Ϸ������ķ�����ҵ��������֯����Ȼ�ˣ��߱�ǩ�������б���֤���������ȫ����Ȩ����������Ϊ������ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.2 ��֤���Ķ�����ȫ�˽���Э�鼰����֤��ŵ�����������ݣ�ǩ������б�֤��ŵ��ϵ���ڱ�֤�˵���ʵ��˼��ʾ���Ѿ������³̼������ڲ������ļ�Ҫ��ȡ�úϷ�����Ч����Ȩ���Ҳ���Υ���Ա�֤����Լ�������κ�Э�顢��ͬ�����������ļ��� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.3 ��֤���Ѿ�ȡ��ǩ�������б���֤��ŵ�������һ���й���׼�ļ�����ɡ��������ߵǼǣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.4 ����֤��ǩ�𱾱�֤��ŵ������Ȩǩ���˾����Ϸ�����Ч�Ĺ�˾��Ȩ��</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.5 ��֤��Ϊ��˾�ģ��ṩ�ñ�֤�Ѿ����չ�˾�³̵Ĺ涨�ɶ��»���߹ɶ��ᡢ�ɶ�������ͨ�����乫˾�³̶Ե������ܶ��������������޶�涨�ģ�����֤��ŵ�����µ���δ�����涨���޶</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.6 ��֤�������ƽ̨��վ�ṩ���й��ļ���������׼ȷ����ʵ����������Ч�ģ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.7 ��֤�˽��ܶ������չ����˶��й�������Ӫ�������ļල��飬�������㹻��Э������ϣ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.8 ����֤�˷�������Ӱ�챣֤�˲���״������Լ�������������֤��Ӧ��������֪ͨ�������չ����ˣ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.9 ��֤�˷������Ʊ�������������ˣ������ˣ���ס������Ӫ��Χ��ע���ʱ����˾�³̵ȹ��̵Ǽ��������ġ�Ӧ���ڱ����7��������������֪ͨ����ƽ̨��վ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.10 ��֤�˳�ŵ�����˲��ܰ����Э��Լ�����黹��Ϣ����֤�˽�����������Ϣ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.11 ����֤��ŵ�����µ������ͱ�֤����������Ч�ģ��䱣֤���β���ծȨ�˸��������κο��ݡ����޻��Żݻ��ӻ���ʹȨ������Ӱ�죬Ҳ���򡶽��Э���顷���޸ġ����䡢���ʱ��Ϊ��֤���ظ������� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	6 �ñ�֤��ŵ���ǡ����Э���顷����ɲ��֣�����ͬ�ȷ���Ч����</p>";
		str+="		<br/>";	
		str+="		<br/>";	
		str+="		<br/>";
		if(((String) responseParams.get("guarantororgid")).length()!=0){
		str+="		<table colWidths=\"50\"  borderWidth=\"1\">";
		str+="          <cell>";
		str+="                <table colWidths=\"1,4,5\" borderWidth=\"1\" padding=\"0\">";
		str+="                      <cell><p align=\"1\">\n\n����\n\n��֤\n\n����</p></cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\" height=\"100\">";
		str+="                      <cell><p align=\"1\">��Ȼ��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n\n��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\">";
		str+="                      <cell><p align=\"1\">���˻�������֯��</p></cell>";
		str+="                      <cell><p>��λ�����£���\n\n\n���������ˣ������ˣ�/��Ȩ������\n��ǩ�£���"+responseParams.get("guarantorperson")+"\n"+responseParams.get("guarantoridcard")+"\n"+responseParams.get("guarantororgid")+"\n"+responseParams.get("putoutdate")+"��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                </table>";
		str+="          </cell>";
		//str+="			<cell colspan=\"3\"><p align=\"1\">ǩ�����ڣ� "+responseParams.get("putoutdate")+"</p></cell>";
		str+="		</table>";
		  }else{
	    str+="		<table colWidths=\"50\"  borderWidth=\"1\">";
		str+="          <cell>";
		str+="                <table colWidths=\"1,4,5\" borderWidth=\"1\" padding=\"0\">";
		str+="                      <cell><p align=\"1\">\n\n����\n\n��֤\n\n����</p></cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\" height=\"100\">";
		str+="                      <cell><p align=\"1\">��Ȼ��</p></cell>";
		str+="                      <cell><p>��ǩ�£���"+responseParams.get("guarantorperson")+"\n"+responseParams.get("guarantoridcard")+"\n"+responseParams.get("putoutdate")+"\n��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\">";
		str+="                      <cell><p align=\"1\">���˻�������֯��</p></cell>";
		str+="                      <cell><p>��λ�����£���\n\n\n���������ˣ������ˣ�/��Ȩ������\n��ǩ�£���\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                </table>";
		str+="          </cell>";
		//str+="			<cell colspan=\"3\"><p align=\"1\">ǩ�����ڣ� "+responseParams.get("putoutdate")+"</p></cell>";
		str+="		</table>"; 
		  }
		}
		
		//����������Ѻ������
		if(((String) responseParams.get("mortgageperson")).length()!=0){
		str+="		<br/>";
		str+="		<br/>";	
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		��������</p>";
		str+="		<br/>";	
		str+="		<br/>";	
		str+="		<p fontSize=\"30\" fontColor=\"#000000\" align=\"1\" >�� Ѻ �� �� �� </p>";
		str+="		<br/>";	
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >   "+responseParams.get("mortgageperson")+" ���³ơ���Ѻ�����ˡ���֪Ϥ����ƽ̨��վ���Ϊ "+responseParams.get("SubContractNo")+"�����Э���顷�����¼�ƽ��Э�飩��ծȨ�ˣ��������ˣ���ϸ������Э���飩 �� "+responseParams.get("borrowname")+"(���³ơ�����ˡ���ǩ���ı��Ϊ  "+responseParams.get("SubContractNo")+" �ġ����Э���顷����ԸΪ�ý��Э�����½����֮ծ���ṩ��Ѻ������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >   1����Ѻ������ΧΪ�����Э���顷���µ�ȫ��ծ�񣬰�����������ΥԼ�����ڷ�Ϣ����Ϣ�����𡢷��չ���ѡ�ƽ̨����ѡ�ʵ��ծȨ�ķ��á� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >   2����Ѻ������ȷ�ϣ���ծ����ΥԼʱ����������Ѻ�������涨�ĵ�����Χ�Ե�Ѻ��е��������Ρ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >   3����Ѻ������������Э����Ϊ�κ�ԭ��������Ĳ�����������Ч����Ч��������Ч�򱻳������������Ӱ���Ѻ��������Լ����  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >   4����Ѻ��������Ч�ڼ�Ϊ���Э������ծ���������޽���֮�������ꡣ </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5����Ѻ�������ڵ�Ѻ��������Ч���������͵������£�  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.1 ��Ѻ������Ϊ����ע�Ტ�Ϸ������ķ�����ҵ��������֯����Ȼ�ˣ��߱�ǩ�������е�Ѻ�������������ȫ����Ȩ����������Ϊ������</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.2 ��Ѻ�������Ķ�����ȫ�˽���Э�鼰��Ѻ���������������ݣ�ǩ������е�Ѻ������ϵ���ڵ�Ѻ�����˵���ʵ��˼��ʾ���Ѿ������³̼������ڲ������ļ�Ҫ��ȡ�úϷ�����Ч����Ȩ���Ҳ���Υ���Ա���˾��Լ�������κ�Э�顢��ͬ�����������ļ��� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.3 ��Ѻ�������Ѿ�ȡ��ǩ�������е�Ѻ�����������һ���й���׼�ļ�����ɡ��������ߵǼǣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.4 �����Ѻ������ǩ���Ѻ����������Ȩǩ���˾����Ϸ�����Ч�Ĺ�˾��Ȩ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.5 ��Ѻ������Ϊ��˾�ģ��ṩ�õ�Ѻ�����Ѿ����չ�˾�³̵Ĺ涨�ɶ��»���߹ɶ��ᡢ�ɶ�������ͨ�����乫˾�³̶Ե�Ѻ�������ܶ�����Ѻ�������������޶�涨�ģ���Ѻ���������µ�Ѻ����δ�����涨���޶</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.6 ��Ѻ�����������ƽ̨��վ�ṩ���й��ļ���������׼ȷ����ʵ����������Ч�ģ�      </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.7 ��Ѻ�����˽��ܶ������չ����˶��й�������Ӫ�������ļල��飬�������㹻��Э������ϣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.8 ����Ѻ�����˷�������Ӱ�쵣���˲���״������Լ�������������Ѻ������Ӧ��������֪ͨ�������չ����ˣ� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.9 ��Ѻ�����˷������Ʊ�������������ˣ������ˣ���ס������Ӫ��Χ��ע���ʱ����˾�³̵ȹ��̵Ǽ��������ģ�Ӧ���ڱ����7��������������֪ͨ����ƽ̨��վ�� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.10 ��Ѻ�����˳�ŵ�����˲��ܰ����Э��Լ�����黹��Ϣ����Ѻ�����˽�����������Ϣ������Ѻ������δ������Լ�����������չ�������Ȩ���õ�Ѻ�������峥��Ϣ����ط��ã� </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >5.11 ��Ѻ���������µ������͵�������������Ч�ģ��䵣�����β���ծȨ�˸��������κο��ݡ����޻��Żݻ��ӻ���ʹȨ������Ӱ�죬Ҳ������Э������޸ġ����䡢���ʱ��Ϊ��Ѻ�������ظ�������</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	6.�õ�Ѻ�������ǽ��Э�������ɲ��֣�����ͬ�ȷ���Ч����</p>";
		str+="		<br/>";	
		str+="		<br/>";	
		if(((String) responseParams.get("mortgageorgid")).length()!=0){
		str+="		<table colWidths=\"50\"  borderWidth=\"1\">";
		str+="          <cell>";
		str+="                <table colWidths=\"1,4,5\" borderWidth=\"1\" padding=\"0\">";
		str+="                      <cell><p align=\"1\">\n\n����\n\n��Ѻ\n\n����</p></cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\" height=\"100\">";
		str+="                      <cell><p align=\"1\">��Ȼ��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n\n��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\">";
		str+="                      <cell><p align=\"1\">���˻�������֯��</p></cell>";
		str+="                      <cell><p>��λ�����£���\n\n\n���������ˣ������ˣ�/��Ȩ������\n��ǩ�£���"+responseParams.get("mortgageperson")+"\n"+responseParams.get("mortgageidcard")+"\n"+responseParams.get("mortgageorgid")+"\n"+responseParams.get("putoutdate")+"\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                </table>";
		str+="          </cell>";
		//str+="			<cell colspan=\"3\"><p align=\"1\">ǩ�����ڣ�"+responseParams.get("putoutdate")+"</p></cell>";
		str+="		</table>";
		 }else{
		str+="		<table colWidths=\"50\"  borderWidth=\"1\">";
		str+="          <cell>";
		str+="                <table colWidths=\"1,4,5\" borderWidth=\"1\" padding=\"0\">";
		str+="                      <cell><p align=\"1\">\n\n����\n\n��Ѻ\n\n����</p></cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\" height=\"100\">";
		str+="                      <cell><p align=\"1\">��Ȼ��</p></cell>";
		str+="                      <cell><p>��ǩ�£���"+responseParams.get("mortgageperson")+"\n"+responseParams.get("mortgageidcard")+"\n"+responseParams.get("putoutdate")+"\n��</p></cell>";
		str+="                      <cell><p>��ǩ�£���\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                      <cell>";
		str+="		<table colWidths=\"10\">";
		str+="                      <cell><p align=\"1\">���˻�������֯��</p></cell>";
		str+="                      <cell><p>��λ�����£���\n\n\n���������ˣ������ˣ�/��Ȩ������\n��ǩ�£���\n\n��</p></cell>";
		str+="                </table>";
		str+="                      </cell>";
		str+="                </table>";
		str+="          </cell>";
		//str+="			<cell colspan=\"3\"><p align=\"1\">ǩ�����ڣ�"+responseParams.get("putoutdate")+"</p></cell>";
		str+="		</table>";
		 }
		}
		//Э�鸽����������������Ȩί����
		if(!"Borrow".equals(contractType)){
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >		Э�鸽��������</p>";
		str+="		<br/>";	
		str+="		<br/>";	
		str+="		<p fontSize=\"30\" fontColor=\"#000000\" align=\"1\" >������Ȩί����  </p>";
		str+="		<br/>";	
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��Ȩ�ˣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	������"+responseParams.get("lendername")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >֤�����루���֤���룩��"+responseParams.get("lendercard")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >��������Ͷ����Eƽ̨�û�����"+responseParams.get("lenderid")+"</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	����Ȩ�ˣ���������������Ϣ�������޹�˾  </p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" > ������Ȩ������������Ͷ����Eƽ̨ǩ����Ӱ桶���Э���顷��Э�����ţ�"+responseParams.get("SubContractNo")+"�����������Э���顷�еĽ���ˣ����¼�ơ�����ˡ��������Ȩ�˵�ȫ���ʽ����Ȩ����Ȩ����Ȩ�˿���ʱ���ֻ����š��绰���ź��������ʼ��������Ϸ���ʽ��ʾ��߸��������л���������Ȩ��ͬ�Ⲣȷ�ϱ���Ȩ������Ȩ���ڣ��ɽ�����ʾ������߸��Ȩ��תί���豻��Ȩ��ȷ�ϵĵ����������ˡ�</p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	��Ȩ��֪����ͬ�⣬����Ȩ������Ȩ����ƽ̨���ߵ����ȷ�ϡ�ʱ��Ч�� </p>";
		str+="		<br/>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	��Ȩ�ˣ�</p>";
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	��   ����"+responseParams.get("lendername")+"</p>";
		str+="		<br/>";	
		str+="		<p fontSize=\"10\" fontColor=\"#000000\" firstLineIndent=\"-10\" align=\"0\" idleft=\"10\" >	���ڣ� "+responseParams.get("putoutdate")+"</p>";
		str+="		<br/>";	
		}
		
	
		str+="	</body>";
		str+="</doc>";
		
		return str;
	}

	@Override
	public void run() {		
		try {
			CreatePDF();
		} catch (HandlerException e) {
			e.printStackTrace();
		}
	}	
}