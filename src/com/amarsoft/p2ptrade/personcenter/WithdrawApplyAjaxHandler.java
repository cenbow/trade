package com.amarsoft.p2ptrade.personcenter;

import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.are.jbo.JBOTransaction;
import com.amarsoft.are.util.StringFunction;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.p2ptrade.tools.GeneralTools;
import com.amarsoft.p2ptrade.tools.TimeTool;

/**
 * �����������뽻�� ��������� UserID:�˻���� SerialNo:��ˮ�� Amount:���ֽ�� ��������� �ɹ���ʶ
 * 
 */
public class WithdrawApplyAjaxHandler extends JSONHandler {

    public Object createResponse(JSONObject request, Properties arg1) throws HandlerException {
        return withdrawApply(request);
    }

    /**
     * ������������
     * 
     * @param request
     * @return
     * @throws HandlerException
     */
    private JSONObject withdrawApply(JSONObject request) throws HandlerException {
        JSONObject result = new JSONObject();
        // boolean WithDrawFlag = false;
        // ����У��
        if (request.get("UserID") == null || "".equals(request.get("UserID")))
            throw new HandlerException("common.emptyuserid");
        if (request.get("SerialNo") == null || "".equals(request.get("SerialNo")))
            throw new HandlerException("param.emptyserialno.error");
        if (request.get("Amount") == null || "".equals(request.get("Amount")))
            throw new HandlerException("param.emptyamount.error");
        double withDrawAmount = 0d;
        try {
            withDrawAmount = Double.parseDouble(request.get("Amount").toString());
        } catch (NumberFormatException e2) {
            throw new HandlerException("param.formartamount.error");
        }
        if (withDrawAmount <= 0) {
            throw new HandlerException("param.limitamount.error");
        }
        String sUserID = request.get("UserID").toString();// �û����
        String sSerialNo = request.get("SerialNo").toString();
        JBOFactory jbo = JBOFactory.getFactory();
        // �޶�У��
        double limitAmount = getLimitCount(jbo, sSerialNo);
        if (withDrawAmount > limitAmount) {// ���ֽ������û����õ����ֽ��
            result.put("WithDrawAmout",limitAmount);
        }else
        	result.put("WithDrawAmout",1000000);
        return result;
    }

    /**
     * ��ȡ�û����õ������޶�
     * 
     * @param jbo
     *            JBOFactory
     * @param sUserID
     *            �û����
     * @return �û����õ��޶�
     * @throws HandlerException
     */
    private double getLimitCount(JBOFactory jbo, String serialNo) throws HandlerException {
        try {
            String table = "jbo.trade.code_library";
            String sql = "select attribute4 AS v.limitamount from o where o.codeno='BankNo' and o.itemno=(select ai.ACCOUNTBELONG from jbo.trade.account_info ai where ai.serialno=:serialno and ai.status='2')";
            BizObjectManager accManager = jbo.getManager(table);
            BizObjectQuery query = accManager.createQuery(sql);
            query.setParameter("serialno", serialNo);

            BizObject accBo = query.getSingleResult(false);
            double limitAmount = 0;
            if (accBo != null) {
                
                String limitA=accBo.getAttribute("limitamount").toString();
                if(limitA==null || "".equals(limitA) || "null".equals(limitA)){
                    limitA="10000000";
                }                
                limitAmount = Double.parseDouble(limitA);
                return limitAmount;
            } else {
                throw new HandlerException("getlimitcount.nodata.error");
            }
        } catch (JBOException e) {
            e.printStackTrace();
            throw new HandlerException("getlimitcount.error");
        }
    }
}

