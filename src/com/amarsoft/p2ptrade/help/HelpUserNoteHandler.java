package com.amarsoft.p2ptrade.help;

import java.util.Properties;


import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
/**
 * �����ύ
 * ���������
 *      RealName:����
 *      Title:��ν
 *      NoteType:��������
 *      NoteText:����
 *      Email:����
 *      PhoneNo:�ֻ�
 *      
 * ��������� 
 * 	    STATUS:�ɹ���ʶ
 *      �������
 */
public class HelpUserNoteHandler extends JSONHandler {

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		 
		return saveHelpUserNote(request);
		 
	} 
	
	/**
	 * �����ύ
	 * @param request
	 * @return
	 * @throws HandlerException
	 */
	@SuppressWarnings("unchecked")
	private JSONObject saveHelpUserNote(JSONObject request)throws HandlerException {
		
		if(request.get("UserID")==null || "".equals(request.get("UserID"))){
			throw new HandlerException("userid.error");
		}
		
		if(request.get("RealName")==null || "".equals(request.get("RealName"))){
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
		
		if(request.get("ReplyMethod")==null || "".equals(request.get("ReplyMethod"))){
			throw new HandlerException("replymethod.error");
		}
		
		if("1".equals(request.get("ReplyMethod").toString())){
		if(request.get("Email")==null || "".equals(request.get("Email"))){
			throw new HandlerException("email.error");
		}
		}
		
		if("2".equals(request.get("ReplyMethod").toString())){
		if(request.get("PhoneNo")==null || "".equals(request.get("PhoneNo"))){
			throw new HandlerException("phoneno.error");
		}
		}
		
		String sUserID= request.get("UserID").toString();
		String sRealName= request.get("RealName").toString();
		String sTitle = request.get("Title").toString();
		String sNoteType = request.get("NoteType").toString();
		String sNoteText = request.get("NoteText").toString();
		String sReplyMethod = request.get("ReplyMethod").toString();
		String sEmail = "";
		String sPhoneNo = "";
		if("1".equals(sReplyMethod)){
		    sEmail = request.get("Email").toString();
			sPhoneNo = "";
		}else if("2".equals(sReplyMethod)){
			sEmail = "";
			sPhoneNo = request.get("PhoneNo").toString();
		}
		JSONObject result = new JSONObject();
		JBOFactory jbo = JBOFactory.getFactory();
        JBOTransaction tx = null;
		try{  
			tx = jbo.createTransaction();
            BizObjectManager m = jbo.getManager("jbo.trade.user_note",tx);
            BizObject bo = m.newObject();
            bo.setAttributeValue("UserID", sUserID);//����
            bo.setAttributeValue("RealName", sRealName);//����
            bo.setAttributeValue("Title", sTitle);//��ν
            bo.setAttributeValue("NoteType", sNoteType);//��������
            bo.setAttributeValue("NoteText", sNoteText);//����
            bo.setAttributeValue("ReplyMethod", sReplyMethod);//�ظ���������
            bo.setAttributeValue("Email", sEmail);//����
            bo.setAttributeValue("PhoneNo", sPhoneNo);//�ֻ�
            bo.setAttributeValue("SubmitTime", GeneralTools.getDate()+" "+GeneralTools.getTime());//�ύʱ��
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
				throw new HandlerException("saveusernote.error");
			}
			e.printStackTrace();
			throw new HandlerException("saveusernote.error");
		}
	}
}