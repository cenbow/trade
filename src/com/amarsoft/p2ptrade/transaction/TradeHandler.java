package com.amarsoft.p2ptrade.transaction;

import java.sql.SQLException;
import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.sql.Connection;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.web.service.imp.transclass.JsptoTrans;

	/*
	 *      p2pAPP �� �Ŵ�  ���״���  ��װ��
	 *      ����ʱ  ���뽻�׼�¼  ������־
	 *      ��Ӧʱ ��д  ���½��׼�¼  
	 *      ���½�����־��Ӧ����  ����ʱ�� ����״̬
	 */
	public abstract class TradeHandler extends JSONHandler{
		   protected JBOTransaction tx = null;
           protected String logidString = null;
           protected String transserialno = null;
		@Override
		public Object createResponse(JSONObject request, Properties arg1) throws HandlerException {
			return tradeCenter(request,arg1);
		}
            
		private Object tradeCenter(JSONObject request,Properties arg1) throws HandlerException {
			JSONObject  result = new JSONObject();
			
			JBOFactory jbo = JBOFactory.getFactory();
			
			try {
				beforeTrans(request, jbo);
				//��������ʹ��
				try {
					tx  = jbo.createTransaction();
				} catch (JBOException e1) {
					e1.printStackTrace();
					throw new HandlerException("transrun.err");
				}				


				try {
					//Ͷ�ʽ��ײ���
					requestObject(request, jbo) ;
					result.put("chk", "S");
//					//������Ŀ��Ͷ����ж��Ƿ��Զ��ſ�
//					double tlDouble = 0;
//					tlDouble = Double.parseDouble(request.get("tlDouble").toString());
//					tlDouble = GeneralTools.round(tlDouble, 2);

					
					tx.commit();
//					if(tlDouble==0){
//						Connection conn = null;
//						try {
//							conn = ARE.getDBConnection("als");
//						} catch (SQLException e) {
//							e.printStackTrace();
//						}
//						
//						/****************���ú�������ģ�⻹��ƻ�*******************/
//						JsptoTrans jt = new JsptoTrans();
//						jt.setMethod("DrawDown");//��������
//						jt.setObjectNo(request.get("ContractSerialNo").toString());//��ͬ��
//						jt.setUserID(request.get("UserID").toString());//����û�ID
//						jt.setConn(conn);//����
//						String sReturn = (String)jt.runPutout();
//						String [] s = sReturn.split("@");
//						result.put("return", s[0]);
//						if(!"true".equalsIgnoreCase(s[0])){//�ſ�ʧ��
//							//���һ��Ͷ��ʧ�ܻع�
//							responseObject(request, result, "", "", jbo);
//							result.put("chk", "F");
//						}else{//�ſ�ɹ�
//							//�����е�Ͷ������Ϊ�ɹ�
//							responseObject(request, result, "", "", jbo);
//							result.put("chk", "S");	
//						}
//						/****************���ú�������ģ�⻹��ƻ�end*******************/
//						try {
//							if(conn!=null)
//								conn.close();
//						} catch (SQLException e) {
//							e.printStackTrace();
//						}finally{
//							try {
//								if(conn!=null)
//								conn.close();
//							} catch (SQLException e) {
//								e.printStackTrace();
//							}
//						}
//					}else{
//						result.put("chk", "S");
//					}
				} catch (JBOException e) {
					try {
						tx.rollback();
					} catch (JBOException e1) {
						e1.printStackTrace();
						throw new HandlerException("transrun.err");
					}
					e.printStackTrace();
					throw new HandlerException("transrun.err");
				}
			
			} catch (HandlerException e) {
				try {
					tx.rollback();
				} catch (JBOException e1) {
					e1.printStackTrace();
					throw new HandlerException("transrun.err");
				}
				e.printStackTrace();
				throw e;
			} 				
			return result;
		}


		protected void beforeTrans(JSONObject request, JBOFactory jbo)throws HandlerException {
			
		}

	//		/**
//		 * @throws HandlerException 
//		 *  		 */
//		private JSONObject responseupdeal(JSONObject request, JSONObject traderesponse, String logidString, String transserialno, JBOFactory jbo,JBOTransaction transaction) throws HandlerException {
//			// TODO Auto-generated method stub
//			JSONObject  result = new JSONObject();
//			JSONObject  jsonObject = new JSONObject();
//			String ret_codeString =  (String) traderesponse.get("ret_code");
//			try {
//				if ("0000".equals(ret_codeString)) {
//					//���ں����Ѽ��ˣ��������ƽ̨������������쳣��Ҳ����ȫ���ع������������ύ����
//					
//					jsonObject = (JSONObject) JSONValue.parse((String) traderesponse.get("info_content"));
//					System.out.println("jsonObject    responseup  :  "+jsonObject);
//					if (jsonObject.get("lDrawDownPayMentSchedules")!=null) {
//						jsonObject.put("array", jsonObject.get("lDrawDownPayMentSchedules"));
//					}
//					jsonObject.put("Methed", traderesponse.get("interface_id"));
//					jsonObject.put("interface_reqser", traderesponse.get("interface_reqser"));
//					jsonObject.put("ret_msg", traderesponse.get("ret_msg"));
//					jsonObject.put("ret_code", traderesponse.get("ret_code"));
//					jsonObject.remove("lDrawDownPayMentSchedules");
//				    jsonObject.put("RootType", "030");
//					//���½�����־
//					updateLog(jbo, request,jsonObject,logidString,transaction); 
//					//�յ���Ӧ ������Ӧ����߼�
//					result =(JSONObject) responseObject(request, jsonObject ,logidString,transserialno, jbo);
//				}else {
//					jsonObject.put("ret_code", (String) traderesponse.get("ret_code"));
//					jsonObject.put("ret_msg",(String) traderesponse.get("ret_msg"));
//					jsonObject.put("Methed", traderesponse.get("interface_id"));
//					jsonObject.put("interface_reqser", traderesponse.get("interface_reqser"));
//				    jsonObject.put("RootType", "010");
//					//���½�����־
//					updateLog(jbo, request,jsonObject,logidString,transaction); 
//					//���쳣���ع����� "core.response.fail"
//					String  errCode = (String)traderesponse.get("ret_code")==null?"":(String)traderesponse.get("ret_code");
//					if(errCode.length()==4)
//					throw new HandlerException(errCode);
//					else
//					throw new HandlerException("core.response.fail");
//				}
//			} catch (HandlerException e) {
//				//updateLogerr(jbo, request,jsonObject,logidString,transaction); 
//				e.printStackTrace();
//				throw e;
//			} 
//			
//			return result;
//		}
//
//	/**
//	 * @throws HandlerException 
//	 * 	 
//	 * ���뽻����־
//	 *  
//	 */
//	private String interlog(JSONObject traderequest,String transserialno ,JBOFactory jbo,JBOTransaction transaction) throws HandlerException{
//		try {
//			BizObjectManager m = jbo.getManager("jbo.trade.acct_transaction_log", transaction);
//			BizObject bo = m.newObject();
//			String sTransCode = "";
//			if("3".equals(traderequest.get("PayType"))){
//				sTransCode = "1040";
//			}else if("0".equals(traderequest.get("PayType"))){
//				sTransCode = "1060";
//			}else{
//				sTransCode = "1030";
//			}
//			
//			bo.setAttributeValue("TRANSCODE", sTransCode);// ���״���
//			bo.setAttributeValue("REQUEST", traderequest.toJSONString());// ������
//			bo.setAttributeValue("TRANSSERIALNO", transserialno);// ������ˮ
//			m.saveObject(bo);
//			transaction.commit();
//			String sLogid = bo.getAttribute("LOGID").getValue()==null?"":bo.getAttribute("LOGID").getString();
//			return sLogid ;
//		} catch (JBOException e) {
//			e.printStackTrace();
//			throw new HandlerException("transrun.err");
//		}
//	}
//		
//	/*
//	 * ���½�����־
//	 * updateTraction(jbo, request,jsonObject,logidString); 
//	 * */
//	private  void updateLog(JBOFactory jbo ,JSONObject request ,JSONObject traderesponse, String logid,JBOTransaction transaction)
//			throws HandlerException {
//		try {
//			//���½�����־
//			BizObjectManager m = jbo.getManager("jbo.trade.acct_transaction_log",transaction);
//            BizObjectQuery bizObjectQuery =m.createQuery("LOGID = :LOGID");
//            bizObjectQuery.setParameter("LOGID",logid);
//            BizObject bizObject = bizObjectQuery.getSingleResult(true);
//            String sMethod = (String)request.get("Method")==null?"":(String)request.get("Method");
//            
//            if("REPaymentConsult".equals(sMethod)){
//            	sMethod = "1041";
//            }else if("PaymentConsult".equals(sMethod)){
//            	sMethod = "1031";
//            }else if("Payment".equals(sMethod)){
//            	sMethod = "1030";
//            }else if("REPayment".equals(sMethod)){
//            	sMethod = "1040";
//            }else{
//            	sMethod = "1060";
//            }
//            
//		    bizObject.setAttributeValue("STATUS",traderesponse.get("ret_code"));// ���ؽ�����
//		    bizObject.setAttributeValue("TRANSDATE",StringFunction.getToday());// ��������
//		    bizObject.setAttributeValue("TRANSTIME",StringFunction.getNow());// ����ʱ��
//		    bizObject.setAttributeValue("TRANSCODE", sMethod);// ���״��� 
//			m.saveObject(bizObject);
//			
//			transaction.commit();
//		
//		} catch (JBOException e) {
//			e.printStackTrace();
//			throw new HandlerException("transrun.err");
//		}
//	}
//
//	/*
//	 * ���½�����־
//	 * updateTraction(jbo, request,jsonObject,logidString); 
//	 * */
//	private  void updateLogerr(JBOFactory jbo ,JSONObject request ,JSONObject traderesponse, String logid,JBOTransaction transaction)
//			throws HandlerException {
//		try {
//			//���½�����־
//			BizObjectManager m = jbo.getManager("jbo.trade.acct_transaction_log",transaction);
//            BizObjectQuery bizObjectQuery =m.createQuery("LOGID = :LOGID");
//            bizObjectQuery.setParameter("LOGID",logid);
//            BizObject bizObject = bizObjectQuery.getSingleResult(true);
//            String sMethod = (String)request.get("Method")==null?"":(String)request.get("Method");
//            
//            if("REPaymentConsult".equals(sMethod)){
//            	sMethod = "1041";
//            }else if("PaymentConsult".equals(sMethod)){
//            	sMethod = "1031";
//            }else if("Payment".equals(sMethod)){
//            	sMethod = "1030";
//            }else if("REPayment".equals(sMethod)){
//            	sMethod = "1040";
//            }else{
//            	sMethod = "1060";
//            }
//		    bizObject.setAttributeValue("STATUS","9999");// ���ؽ�����
//		    bizObject.setAttributeValue("TRANSDATE",StringFunction.getToday());// ��������
//		    bizObject.setAttributeValue("TRANSTIME",StringFunction.getNow());// ����ʱ��
//		    bizObject.setAttributeValue("TRANSCODE", sMethod);// ���״��� 
//			m.saveObject(bizObject);
//			
//			transaction.commit();
//		
//		} catch (JBOException e) {
//			e.printStackTrace();
//			throw new HandlerException("transrun.err");
//		}
//	}
//	
//	//		���ɺ��㽻����ˮ��
//	private  String gettransserialno (JBOFactory jbo) throws HandlerException{
//		String sTransserialno ="";
//		try {
//		BizObjectManager bm = jbo.getManager("jbo.trade.dual");
//		BizObjectQuery query = bm.createQuery("select  DUMMY  from o ");
//		BizObject bizObject = query.getSingleResult(false);
//		 sTransserialno = bizObject.getAttribute("DUMMY").getValue()==null?"":bizObject.getAttribute("DUMMY").getString();
//		 return sTransserialno;
//		} catch (JBOException e) {
//			e.printStackTrace();
//			throw new HandlerException("transrun.err");
//		}
//	}
//	
	/**
	 * @return �绰����
	 * @throws HandlerException 
	 */
	protected String getPhoneTel(String useidString ,JBOFactory jbo) throws HandlerException {
		// TODO Auto-generated method stub
		String PhoneTel = "";
		BizObjectManager manager;
		try {
			manager = jbo.getManager("jbo.trade.user_account");
			BizObjectQuery query = manager.createQuery("select PHONETEL from o where  USERID = :USERID");
			query.setParameter("USERID",useidString);
			BizObject boBizObject = query.getSingleResult(false);
			PhoneTel = boBizObject.getAttribute("PHONETEL").getValue()==null?"":boBizObject.getAttribute("PHONETEL").getString();
		} catch (JBOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			throw new HandlerException("common.emptymobile");
		}
		return PhoneTel;
	}
	
	protected abstract  Object requestObject(JSONObject request,JBOFactory jbo)  throws HandlerException;
	protected abstract  Object responseObject(JSONObject request,JSONObject response,String logid ,String transserialno,JBOFactory jbo)  throws HandlerException;


}

