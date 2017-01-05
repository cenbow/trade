package com.amarsoft.p2ptrade.personcenter;

import java.util.Calendar;
import java.util.List;
import java.util.Properties;

import com.amarsoft.app.accounting.util.NumberTools;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOException;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * ����������ϸ���ۼ�������ϸ����Ͷ������ϸ��
 * 
 * ���������
 * 
 * UserId�� DetailType:010-�ۼ�������ϸ��020-��Ͷ������ϸ curPage pageSize
 * 
 * @author hhCai 2015-2-13
 */
public class UserIncomeDetailHandler extends JSONHandler {

	private String DetailType;

	private int curPage = 0, pageSize = 20;

	private int totalAcount;

	@Override
	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException {
		// TODO Auto-generated method stub
		// ����У��
		if (request.get("UserID") == null || "".equals(request.get("UserID"))) {
			throw new HandlerException("common.emptyuserid");
		}
		if (request.get("DetailType") == null
				|| "".equals(request.get("DetailType"))) {
			DetailType = "010";
		} else {
			DetailType = request.get("DetailType").toString();
		}
		if (request.containsKey("pageSize"))
			this.pageSize = Integer
					.parseInt(request.get("pageSize").toString());
		if (request.containsKey("pageNo"))
			this.curPage = Integer.parseInt(request.get("pageNo").toString());
		String sUserID = request.get("UserID").toString();
		JSONObject resultObject = new JSONObject();
		try {
			resultObject.put("InvestArray", getRuningInvestDetail(sUserID));// ���������ϸ.������ʷ��������Ͷ����
			resultObject.put("TotalAcount", totalAcount);
		} catch (Exception e) {
			// TODO: handle exception
			new HandlerException(e.getMessage());
		}
		return resultObject;
	}

	/**
	 * ���ƽ̨������ϸ
	 * 
	 * @param userid
	 * @return
	 * @throws JBOException
	 */
	public JSONArray getRewardDetail(String userid) throws JBOException {

		return null;
	}

	/**
	 * ���������ϸ
	 * 
	 * @param sUserID
	 * @return
	 * @throws JBOException
	 */
	public JSONArray getRuningInvestDetail(String sUserID) throws JBOException {

		BizObjectManager manager = JBOFactory
				.getBizObjectManager("jbo.trade.income_detail");
		String statusrand = "010".equals(DetailType) ? "('1','3')" : "('1')";
		String sql = "select ActualPayInteAmt+ActualExpiationSum as v.ActualAmount,o.actualpaydate,o.inputdate,o.inputtime,uc.projectid,pi.PROJECTNAME,"
				+ "uc.investsum,uc.status from o,jbo.trade.user_contract uc,jbo.trade.project_info_listview pi "
				+ "where o.userid=:userid and uc.userid=o.userid and uc.subcontractno = o.subcontractno and uc.status in "
				+ statusrand
				+ "and pi.serialno=uc.projectid order by o.inputdate desc,o.inputtime desc";
		// ��ȡ�����������м�¼
		BizObjectQuery query = manager.createQuery(sql).setParameter("userid",
				sUserID);
		// ��ҳ
		totalAcount = query.getTotalCount();
		int pageCount = (totalAcount + pageSize - 1) / pageSize;
		if (curPage > pageCount)
			curPage = pageCount;
		if (curPage < 1)
			curPage = 1;
		query.setFirstResult((curPage - 1) * pageSize);
		query.setMaxResults(pageSize);
		// ��Ͷ����(����)
		List<BizObject> list = query.getResultList(false);
		JSONArray jsonArray = new JSONArray();

		for (int i = 0; i < list.size(); i++) {
			BizObject o = list.get(i);
			if (o != null) {
				String projetid = o.getAttribute("PROJECTID").toString() == null ? ""
						: o.getAttribute("PROJECTID").toString();
				JSONObject jsonObject = null;
				String status = o.getAttribute("status").toString() == null ? ""
						: o.getAttribute("status").toString();
				jsonObject = new JSONObject();
				if ("3".equals(status)) {// ��ʷͶ������

				} else if ("1".equals(status)) {// ��Ͷ����
					String CurMonthInteAmt, CurPayDate;
					try {
						JSONObject object = getRuningInvestCurMothIncome(
								projetid, sUserID);
						if (object != null) {
							CurMonthInteAmt = object
									.containsKey("CurMonthInteAmt") ? object
									.get("CurMonthInteAmt").toString() : "";
							CurPayDate = object.containsKey("CurPayDate") ? object
									.get("CurPayDate").toString() : "";
						} else {
							CurMonthInteAmt = CurPayDate = "";
						}

					} catch (Exception e) {
						// TODO: handle exception
						CurMonthInteAmt = CurPayDate = "";
					}
					jsonObject.put("CurMonthInteAmt", CurMonthInteAmt);
					jsonObject.put("CurPayDate", CurPayDate);
				}
				if (jsonObject != null) {
					jsonObject.put("Status", status);
					jsonObject
							.put("ActualAmount",
									o.getAttribute("ActualAmount").toString() == null ? ""
											: o.getAttribute("ActualAmount")
													.toString());// ����Ŀ������Ϣ
					jsonObject
							.put("ActualPayDate",
									o.getAttribute("ACTUALPAYDATE").toString() == null ? ""
											: o.getAttribute("ACTUALPAYDATE")
													.toString());// ��Ŀ�տ�����
					jsonObject.put("ProjectId", projetid);// ��Ŀ���
					jsonObject
							.put("ProjectName",
									o.getAttribute("PROJECTNAME").toString() == null ? ""
											: o.getAttribute("PROJECTNAME")
													.toString());// ��Ŀ����
					jsonObject.put("InvestSum",
							o.getAttribute("INVESTSUM").toString() == null ? ""
									: o.getAttribute("INVESTSUM").toString());// ��ĿͶ���ܶ�
				}
				jsonArray.add(jsonObject);
			}
		}
		return jsonArray;
	}

	/**
	 * ���ĳ��Ŀ�ĵ���Ӧ������
	 * 
	 * @param projetid
	 *            ��Ŀ���
	 * @return
	 */
	private JSONObject getRuningInvestCurMothIncome(String projetid,
			String userid) throws JBOException {
		String sPloanSetupDate = getPloanSetupDate();
		BizObjectManager manager = JBOFactory
				.getBizObjectManager("jbo.trade.income_schedule");
		String sql = "Select PayInteAmt as PayInteAmt,PayDate From o,jbo.trade.user_contract uc Where o.UserID = uc.UserID And PayDate like :month And uc.Status = '1' And uc.UserID =:userid and o.projectno=:projetid";
		BizObject obj = manager.createQuery(sql).setParameter("userid", userid)
				.setParameter("projetid", projetid)
				.setParameter("month", sPloanSetupDate.substring(0, 8) + "%")
				.getSingleResult(false);
		// ����Ӧ����Ϣ
		double dMonthInteAmt = 0;
		if (obj != null)
			dMonthInteAmt = obj.getAttribute("PayInteAmt").getDouble();
		int iMonthDay = getCurrentMonthLastDay();// �����ж�����
		int itoday = Integer.parseInt(sPloanSetupDate.substring(8));// ���µڼ���
		double MonthInteAmt = NumberTools.round(
				(dMonthInteAmt / iMonthDay * itoday), 2);// ����Ŀ����Ӧ����Ϣ
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("CurMonthInteAmt", MonthInteAmt);// ����Ŀ����Ӧ����Ϣ
		jsonObject.put("CurPayDate", obj.getAttribute("PayDate").toString());// ����Ӧ������
		return jsonObject;
	}

	private String getPloanSetupDate() throws JBOException {
		return JBOFactory.getBizObjectManager("jbo.trade.ploan_setup")
				.createQuery("").getSingleResult(false)
				.getAttribute("curdeductdate").getString();
	}

	/**
	 * ȡ�õ�������
	 * */
	public static int getCurrentMonthLastDay() {
		Calendar a = Calendar.getInstance();
		a.set(Calendar.DATE, 1);// ����������Ϊ���µ�һ��
		a.roll(Calendar.DATE, -1);// ���ڻع�һ�죬Ҳ�������һ��
		int maxDate = a.get(Calendar.DATE);
		return maxDate;
	}
}
