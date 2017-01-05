package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �ҵĽ��-�������󷢲�
 * ���������
 *      UserID:�û�ID
 *      Title���������
 *      LoanType:��������
 *      TermYear:��������_��
 *      TermMonth:��������_��
 *      TermDay:��������_��
 *      TermDay:��������_��
 *      TermDay:��������_��
 *      Direction:����˵��
 *      
 * ��������� 
 * 	    STATUS:�ɹ���ʶ
 *      �������
 */
public class ReleaseLoanRequireHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		 
		return saveLoanUserRequire(request);
		 
	} 
	
	/**
	 * �������󷢲�
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject saveLoanUserRequire(JSONObject request)throws HandlerException {
		/*if(request.get("RealName")==null || "".equals(request.get("RealName"))){
			throw new HandlerException("realname.error");
		}
		
		if(request.get("Title")==null || "".equals(request.get("Title"))){
			throw new HandlerException("title.error");
		}
		
		if(request.get("NoteType")==null || "".equals(request.get("NoteType"))){
			throw new HandlerException("notetype.error");
		}
		
		if(request.get("NoteText")==null || "".equals(request.get("NoteText"))){
			throw new HandlerException("notetext.error");
		}
		
		if("1".equals(request.get("NoteType").toString())){
		if(request.get("Email")==null || "".equals(request.get("Email"))){
			throw new HandlerException("email.error");
		}
		}
		
		if("2".equals(request.get("NoteType").toString())){
			if(request.get("PhoneNo")==null || "".equals(request.get("PhoneNo"))){
				throw new HandlerException("phoneno.error");
			}
		}*/
		
		if(request.get("Title")==null || "".equals(request.get("Title"))){
			throw new HandlerException("title.error");
		}
		
		/*String sRealName= request.get("RealName").toString();
		String sTitle = request.get("Title").toString();
		String sNoteType = request.get("NoteType").toString();
		String sNoteText = request.get("NoteText").toString();
		String sEmail = "";
		String sPhoneNo = "";
		if("1".equals(sNoteType)){
		    sEmail = request.get("Email").toString();
			sPhoneNo = "";
		}else if("2".equals(sNoteType)){
			sEmail = "";
			sPhoneNo = request.get("PhoneNo").toString();
		}*/
		
		String sTitle = request.get("Title").toString();
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
        JBOTransaction tx = null;
		try{  
			tx = jbo.createTransaction();
            BizObjectManager m = jbo.getManager("jbo.trade.application",tx);
            BizObject bo = m.newObject();
//            bo.setAttributeValue("SERIALNO", "011");//��ˮ��sRealName
//            bo.setAttributeValue("RealName", sRealName);//����
//            bo.setAttributeValue("Title", sTitle);//��ν
//            bo.setAttributeValue("NoteType", sNoteType);//��������
//            bo.setAttributeValue("NoteText", sNoteText);//����
//            bo.setAttributeValue("Email", sEmail);//����
//            bo.setAttributeValue("PhoneNo", sPhoneNo);//�ֻ�
            bo.setAttributeValue("Title", sTitle);//�������
            m.saveObject(bo);
            tx.commit();
            result.put("param", "ok");
		    return result;
		}
		catch(Exception e){
			try {
				if(tx != null){
					tx.rollback();
				}
			} catch (JBOException e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			throw new HandlerException("saveuserrequire.error");
		}
	}
}