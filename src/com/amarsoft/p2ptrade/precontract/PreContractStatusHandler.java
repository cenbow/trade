package com.amarsoft.p2ptrade.precontract;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * �鿴Ԥǩ��ͬ״̬
 * ���룺 UserID �ͻ���
 * �����status ���ؽ��
 * @author flian
 *
 */
public class PreContractStatusHandler extends JSONHandler{

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		try{
			if(request.containsKey("UserID")==false){
				throw new HandlerException("common.emptyusername");
			}
			BizObject obj = JBOFactory.getBizObjectManager("jbo.trade.pred_contract")
				.createQuery("select max(inputtime) as v.it from o where userid=:userid")
				.setParameter("userid", request.get("UserID").toString())
				.getSingleResult(false);
			if(obj==null){
				return "";
			}
			else{
				String sInputTime = obj.getAttribute("it").getString();
				
				if(sInputTime==null || sInputTime.trim().equals("")){
					return "";
				}
				else{
					//�����Ƿ�����Чʱ����
					try{
						SimpleDateFormat sdf =   new SimpleDateFormat( "yyyy/MM/dd HH:mm:ss" );
						Date date = sdf.parse(sInputTime);
						Date today = new Date();
						//7���ڲ���������д
						if(today.getTime()-date.getTime()>=7*24*3600*1000){
							return "";
						}
						else{
							return "[#C"+sInputTime+"]";
						}
					}
					catch(Exception e){
						e.printStackTrace();
						return "[Ԥǩ��ͬ״̬��ȡʧ��]";
					}
				}
			}
		}
		catch(Exception e){
			e.printStackTrace();
			throw new HandlerException("business.showhtml.fail");
		}
	}

}
