package com.amarsoft.awe.common.pdf;

import java.io.File;
import java.util.Properties;

import com.allinpay.ets.client.util.Base64;
import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.util.RunTradeService;

/**
 * ����PDF�ļ�
 *
 */
public class LoadContractPdfHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		String sSerialNo = (String) request.get("SerialNo");//�������
		String sUserID = (String) request.get("UserID");//�û�ID
		String sRelativeType = (String) request.get("RelativeType");//�����������
		String sContractType = (String) request.get("ContractType");//��ͬ����
		
		if(sSerialNo == null || sSerialNo.length() == 0){
			throw new HandlerException("common.serialno");
		}
		if(sUserID == null || sUserID.length() == 0){
			throw new HandlerException("common.emptyuserid");
		}
		if(sRelativeType == null || sRelativeType.length() == 0){
			throw new HandlerException("common.relativetype");
		}
		if(sContractType == null || sContractType.length() == 0){
			throw new HandlerException("common.contracttype");
		}

		try{
			// ����P2P_Trade����
			String sMethod = "loadcontractpdf";
			String sRequestFormat = "json";
			String sRequestStr = GeneralTools.createJsonString(request);
			JSONObject responsePram = RunTradeService.runTranProcess(
					sMethod, sRequestFormat, sRequestStr);
			ARE.getLog().info(responsePram.toString());
			
			if(responsePram.containsKey("returnCode")){
				String returnCode = responsePram.get("returnCode").toString();
				if(returnCode.equals("contract.exist.error")){
					throw new HandlerException("contract.exist.error");
				}else if(returnCode.equals("contract.status.error")){
					throw new HandlerException("contract.status.error");
				}else if(returnCode.equals("contract.status.error002")){
					throw new HandlerException("contract.status.error002");
				}else{
					throw new HandlerException("loadcontractpdf.error");
				}
			}
			return responsePram;
		}catch(HandlerException e){
			throw e;
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("loadcontractpdf.error");
		}
	}

}
