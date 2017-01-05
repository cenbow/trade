package com.amarsoft.p2ptrade.util;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.security.DefaultAppContext;
import com.amarsoft.mobile.webservice.DataConvertor;
import com.amarsoft.mobile.webservice.ServiceFactory;
import com.amarsoft.mobile.webservice.TradeManager;
import com.amarsoft.mobile.webservice.business.AppContextSetting;
import com.amarsoft.mobile.webservice.business.BusinessHandler;
import com.amarsoft.mobile.webservice.business.BusinessValidator;
import com.amarsoft.mobile.webservice.business.IDeviceType;
import com.amarsoft.mobile.webservice.imp.SessionBusinessHandler;
import com.amarsoft.mobile.webservice.model.BaseRequest;
import com.amarsoft.mobile.webservice.model.BaseResponse;
import com.amarsoft.mobile.webservice.security.Base64;
import com.amarsoft.mobile.webservice.security.CompressHelp;
import com.amarsoft.mobile.webservice.security.EncryptAction;
import com.amarsoft.mobile.webservice.security.imp.Des3Encryption;
import com.amarsoft.mobile.webservice.session.SessionManager;
import com.amarsoft.mobile.webservice.system.SignAuthor;
/**
 * �ƶ�����������
 * @author flian
 *
 */
public class ImageService {
	
	protected static final String SUCCESS= "SUCCESS";
	private Properties handlerProperties=new Properties();
	 @Resource
	 private WebServiceContext wsContext;
	 private String clientIP = "";
	 
	 public ImageService(){
		 
	 }
	 
	 public ImageService(String clientIP){
		 this.clientIP = clientIP;
	 }
	 
	 public void appendHandlerProperties(String name,String value){
		 handlerProperties.setProperty(name, value);
	 }

	 
	//���񹤳�
	protected ServiceFactory factory;
	protected String sessionKey = null;
	protected String deviceType = null;
	
	public Object genImage(String method,String requestFormat,String requestStr,String signKey){
		TradeManager tradeManager =null;
		Date dStartTime = new Date();
		String sRemortAddress = clientIP;
		try{
			if(wsContext!=null){
				MessageContext messageContext = wsContext.getMessageContext();
				HttpServletRequest hrequest = (HttpServletRequest) (messageContext.get(MessageContext.SERVLET_REQUEST));
				sRemortAddress = hrequest.getRemoteAddr();
			}
			ARE.getLog().info("����"+ sRemortAddress+"������method=" + method+",requestFormat=" + requestFormat + ",requestStr="+requestStr+",signKey="+signKey);
			DefaultAppContext appContext = new DefaultAppContext();
			appContext.setClientAddress(sRemortAddress);
			
			this.factory = getFactory();
			tradeManager = getTradeManager(method);
			if(null==tradeManager){
				ARE.getLog().error("��֧�ֵķ�����" + method);
				this.dbLog(method, requestFormat, requestStr, "��֧�ֵķ�����" + method, dStartTime,  sRemortAddress);
				return encodeData("��֧�ֵķ�����" + method,tradeManager);
				//return encodeData("request.method.error");
				
			}
			DataConvertor convertor = tradeManager.getConvertor(requestFormat);
			if(convertor==null){
				ARE.getLog().error("����"+method+"��֧�����ݸ�ʽ"+requestFormat);
				this.dbLog(method, requestFormat, requestStr, "����"+method+"��֧�����ݸ�ʽ"+requestFormat, dStartTime,  sRemortAddress);
				return encodeData("����"+method+"��֧�ֵ����ݸ�ʽ"+requestFormat,tradeManager);
				//return  encodeData("request.format.error");
			}
				
			//���ݽ���
			try{
				requestStr = this.decodeData(requestStr,tradeManager);
				if(tradeManager.getTradeConfigModel().isOutputRequestStr())
					ARE.getLog().debug("���������ݱ���:" + requestStr);
				
					
			}
			catch(Exception e){
				e.printStackTrace();
				ARE.getLog().info("���ݽ���ʧ�ܣ�"+requestStr);
				this.dbLog(method, requestFormat, requestStr, "���ݽ���ʧ�ܣ�"+requestStr, dStartTime,  sRemortAddress);
				return encodeData("request.decode.error",tradeManager);
			}
			//��ýṹ������
			BaseRequest request = convertor.convertToObject(requestStr);
			if(request==null){
				
				this.dbLog(method, requestFormat, requestStr,"request.invalid", dStartTime,  sRemortAddress);
				return encodeData("request.invalid",tradeManager);
			}
			deviceType = request.getDeviceType();
			//ϵͳ����У��
			String systemAuthResult = systemAuth(method,requestFormat,requestStr,signKey,request,tradeManager);
			if(!SUCCESS.equals(systemAuthResult)){
				String sInfo = returnError(convertor,systemAuthResult);
				this.dbLog(method, requestFormat, requestStr,systemAuthResult, dStartTime,  sRemortAddress);
				return encodeData(sInfo,tradeManager);
			}
			//��ȡҵ������
			BusinessHandler businessHandler = tradeManager.getBusinessHandler();
			if(businessHandler==null){
				this.dbLog(method, requestFormat, requestStr,"trade.method.handler.invalid", dStartTime,  sRemortAddress);
				return encodeData(returnError(convertor,"trade.method.handler.invalid"),tradeManager);
			}
			//���û�������
			if(businessHandler instanceof AppContextSetting){
				((AppContextSetting)businessHandler).setAppContext(appContext);
			}
			businessHandler.setRequestBusinessObject(request.getBusinessRequestObject());
			if(businessHandler instanceof IDeviceType){
				((IDeviceType)businessHandler).setDeviceType(request.getDeviceType());
			}
			if(businessHandler instanceof SessionBusinessHandler){
				SessionBusinessHandler bsh = (SessionBusinessHandler)businessHandler;
				//if(request.getSessionKey()==null || request.getSessionKey().equals("")){
				if(tradeManager.getTradeConfigModel().isNeedLogon() && (request.getSessionKey()==null || request.getSessionKey().equals(""))){
					this.dbLog(method, requestFormat, requestStr,"account.session.invalid", dStartTime,  sRemortAddress);
					return encodeData(returnError(convertor,"account.session.invalid"),tradeManager);
				}
				bsh.setSessionKey(request.getSessionKey());
				this.sessionKey = bsh.getSessionKey();
			}
			
			//ҵ�񼶱�У��
			if(businessHandler instanceof BusinessValidator){
				String validResult = ((BusinessValidator)businessHandler).valid();
				if(!SUCCESS.equals(validResult)){
					this.dbLog(method, requestFormat, requestStr,validResult, dStartTime,  sRemortAddress);
					return encodeData(returnError(convertor,validResult),tradeManager);
				}
			}
			//ִ��ҵ���߼�
			handlerProperties.putAll(tradeManager.getHandlerProperties()) ;
			String executeResult = businessHandler.execute(handlerProperties);
			if(!SUCCESS.equals(executeResult)){
				if("response.unkown".equals(executeResult)){
					String sResult = "N";
					ARE.getLog().debug("sReturn=" + sResult);
					this.dbLog(method, requestFormat, requestStr,sResult, dStartTime,  sRemortAddress);
					return sResult;
				}
				else{
					String sResult = encodeData(returnError(convertor,executeResult),tradeManager);
					this.dbLog(method, requestFormat, requestStr,executeResult, dStartTime,  sRemortAddress);
					ARE.getLog().debug("sReturn=" + sResult);
					return sResult;
				}
			}
			//ת���������
			Object responseBusinesObj = businessHandler.getResponseBusinessObject();
			if((responseBusinesObj instanceof byte[])==false){
				return encodeData("image.unkown",tradeManager);
			}
			else{
				return encodeBytes(responseBusinesObj,tradeManager);
			}
		}
		catch(Exception fe){
			fe.printStackTrace();
			this.dbLog(method, requestFormat, requestStr,"response.unknown", dStartTime,  sRemortAddress);
			return encodeData("response.unknown",tradeManager);
		}
		
	}
	
	private Object encodeBytes(Object responseBusinesObj,TradeManager tradeManager) {
		boolean bSafeMode = true;
		if(tradeManager!=null)
			bSafeMode = !tradeManager.isUnsafe();
		//����ģʽ�£���Ҫ�Ƚ����ݽ��м��ܺ�ѹ��
		if(false && bSafeMode&&"true".equals(factory.getTransportEncryption())){
			try{
				//ѹ��
				byte[] bytes = (byte[])responseBusinesObj;
				byte[] compressedbytes=CompressHelp.compressData(bytes);
				//����
				String sTransportEncryptKey = factory.getTransportEncryptKey();
				int iDot = sTransportEncryptKey.lastIndexOf(",");
				String sKey = sTransportEncryptKey.substring(0,iDot);
				byte[] encryptKey = sKey.getBytes(factory.getDataChangeEncode());
				String sIV = sTransportEncryptKey.substring(iDot+1, sTransportEncryptKey.length());
				EncryptAction ea = createEncryptAction(factory.getTransportEncryptAlgorithrm());
				ea.setDecryptKey(encryptKey);
				ea.setIV(sIV);
				return ea.encrypt(compressedbytes);
			}
			catch(Exception e){
				return "response.unknown";
			}
		}
		else{
			return responseBusinesObj;
		}
	}

	//��ȡ���񹤳�
	protected ServiceFactory getFactory(){
		return ServiceFactory.getFactory();
	}
	//��ȡ���׹�����
	protected TradeManager getTradeManager(String method){
		TradeManager tradeManager = null;
		try{
			tradeManager = factory.createTradeManager(method);
		}
		catch(Exception e){
			return null;
		}
		return tradeManager;
	}
	//���ݽ���
	protected String decodeData(String str,TradeManager tradeManager)throws Exception{
		//����ģʽ�£���Ҫ�Ƚ����ݽ��н��ܺͽ�ѹ��
		boolean bSafeMode = true;
		if(tradeManager!=null)
			bSafeMode = !tradeManager.isUnsafe();
		if(bSafeMode&&"true".equals(factory.getTransportEncryption())){
			String sTransportEncryptKey = factory.getTransportEncryptKey();
			int iDot = sTransportEncryptKey.lastIndexOf(",");
			String sKey = sTransportEncryptKey.substring(0,iDot);
			String sIV = sTransportEncryptKey.substring(iDot+1, sTransportEncryptKey.length());
			byte[] decryptKey = sKey.getBytes(factory.getDataChangeEncode());
			EncryptAction ea = createEncryptAction(factory.getTransportEncryptAlgorithrm());
			ea.setDecryptKey(decryptKey);
			ea.setIV(sIV);
			//����
			byte[] dedata = ea.decrypt(Base64.decode(str.replace("\\/", "/")));
			//��ѹ��
			byte[] uncompressedbytes=CompressHelp.uncompressData(dedata);
			str = new String(uncompressedbytes,factory.getDataChangeEncode());
			return str;
		}
		else{
			return str;
		}
	}
	
	protected EncryptAction createEncryptAction(String clazz)throws Exception{
		if(clazz==null || clazz.trim().length()==0)
			return new Des3Encryption();
		else
			return (EncryptAction)Class.forName(clazz).newInstance();
	}
	
	//���ݱ���
	protected String encodeData(String str,TradeManager tradeManager){
		boolean bSafeMode = true;
		if(tradeManager!=null)
			bSafeMode = !tradeManager.isUnsafe();
		//����ģʽ�£���Ҫ�Ƚ����ݽ��м��ܺ�ѹ��
		if(bSafeMode&&"true".equals(factory.getTransportEncryption())){
			try{
				//ѹ��
				byte[] bytes = str.getBytes(factory.getDataChangeEncode());
				byte[] compressedbytes=CompressHelp.compressData(bytes);
				//����
				String sTransportEncryptKey = factory.getTransportEncryptKey();
				int iDot = sTransportEncryptKey.lastIndexOf(",");
				String sKey = sTransportEncryptKey.substring(0,iDot);
				byte[] encryptKey = sKey.getBytes(factory.getDataChangeEncode());
				String sIV = sTransportEncryptKey.substring(iDot+1, sTransportEncryptKey.length());
				EncryptAction ea = createEncryptAction(factory.getTransportEncryptAlgorithrm());
				ea.setDecryptKey(encryptKey);
				ea.setIV(sIV);
				return bytes.length +","+Base64.encode(ea.encrypt(compressedbytes));
			}
			catch(Exception e){
				return "response.unknown";
			}
		}
		else{
			return str;
		}
	}
	
	protected BaseResponse createResponse(String returnCode,Object responseBusinesObj){
		BaseResponse response = new BaseResponse();
		response.setReturnCode(returnCode);
		response.setBusinessResponseObject(responseBusinesObj);
		return response;
	}
	
	protected String returnError(DataConvertor convertor,String returnCode){
		return returnStringResult(convertor,returnCode,null);
	}
	
	protected String returnStringResult(DataConvertor convertor,String returnCode,Object responseBusinesObj){
		BaseResponse response = createResponse(returnCode,responseBusinesObj);
		return convertor.convertFromObject(response);
	}
	
	protected void dbLog(String method,String requestFormat,String requestStr,String responseStr,Date startTime,String ip){
		boolean needLog = false;
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date finishTime = new Date();
		try{
			if(factory.getLogTradeTime()!=null && factory.getLogTradeTime().length()>0){
				int iSesconds = Integer.parseInt(factory.getLogTradeTime());
				if(iSesconds==0 ||(finishTime.getTime()-startTime.getTime())*1000>=iSesconds)
					needLog = true;
			}
		}
		catch(Exception e){
			ARE.getLog().error("logTradeTime�����������⣬���׼�¼������");
			e.printStackTrace();
		}
		if(needLog==false)return;
		try{
			BizObjectManager manager = JBOFactory.getBizObjectManager("jbo.trade.system.AME_RUNTIME");
			BizObject obj = manager.newObject();
			obj.setAttributeValue("METHOD", method);
			obj.setAttributeValue("REQUESTFORMAT", requestFormat);
			if(requestStr!=null && requestStr.length()>500)
				obj.setAttributeValue("REQUESTSTR", requestStr.substring(0, 500));
			else
				obj.setAttributeValue("REQUESTSTR", requestStr);
			if(responseStr!=null && responseStr.length()>500)
				obj.setAttributeValue("RESPONSESTR", responseStr.substring(0, 500));
			else
				obj.setAttributeValue("RESPONSESTR", responseStr);
			obj.setAttributeValue("DEVICETYPE", deviceType);
			obj.setAttributeValue("STARTTIME", formatter.format(startTime));
			obj.setAttributeValue("FINISHTIME", formatter.format(finishTime));
			obj.setAttributeValue("SESSIONKEY", sessionKey);
			obj.setAttributeValue("USERID", SessionManager.getUserId(sessionKey));
			obj.setAttributeValue("IP",ip );
			manager.saveObject(obj);
		}
		catch(Exception e){
			ARE.getLog().error("���׼�¼������");
			e.printStackTrace();
		}
		
		
		
	}
	
	//ϵͳ���У��
	protected String systemAuth(String method,String requestFormat,String requestStr,String signKey,BaseRequest request,TradeManager tradeManager){
		if(true){
			return SUCCESS;
		}
		if(request.getSessionKey()==null || request.getSessionKey().length()==0){//û�е�¼
			boolean bSafeMode = true;
			if(tradeManager!=null)
				bSafeMode = !tradeManager.isUnsafe();
			if(bSafeMode)
				return (new SignAuthor(factory.getSystemKey()).auth(method,requestFormat,requestStr,signKey));
			else
				return SUCCESS;
		}
		else{//�Ѿ���¼,����afterlogonУ��
			return SUCCESS;
		}
	}
}
