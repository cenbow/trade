package com.amarsoft.p2ptrade.mobileapp;

import java.util.List;
import java.util.Properties;

import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;

/**
 * APP-Banner���λ
 * 
 * @author hhCai 2015-3-16
 */
public class APPBannerAdHandler extends JSONHandler {

	@Override
	public Object createResponse(JSONObject arg0, Properties arg1)
			throws HandlerException {
		// TODO Auto-generated method stub
//		String adsql = "select o.* from o where o.positionid='2015031600000001' and isshow='Y'";// ����ѯ���
		String adsql = "select o.* from o where o.positionid='2015031800000001' and isshow='Y'";// ����ѯ���
		try {
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.inf_advertise");
			// APP���λ�ļ�¼
			BizObjectQuery query = m.createQuery(adsql);
			// ����������л�ȡ�ļ�¼�ŵ�json������
			List<BizObject> list = query.getResultList(false);
			if (list != null) {
				JSONArray array = new JSONArray();
				for (int i = 0; i < list.size(); i++) {
					BizObject o = list.get(i);
					if (o != null) {
						JSONObject obj = new JSONObject();
						String title = o.getAttribute("title").toString() == null ? ""
								: o.getAttribute("title").toString();// ������
						String remark = o.getAttribute("remark").toString() == null ? ""
								: o.getAttribute("remark").toString();// ˵��
						String linkurl = o.getAttribute("linkurl").toString() == null ? ""
								: o.getAttribute("linkurl").toString();// ������ӵ�ַ
						String picno = o.getAttribute("picno").toString() == null ? ""
								: o.getAttribute("picno").toString();// ͼƬ���
						String picpath;
						if (picno.length() == 0) {
							picpath = "";
						} else
							picpath = "http://www.houbank.com/picView?picno=advpic/"
									+ picno + ".png";// ͼƬ·��
//							picpath = "http://192.168.1.245:8280/hbweb/picView?picno=advpic/"
//									+ picno + ".png";
						obj.put("title", title);
						obj.put("remark", remark);
						obj.put("linkurl", linkurl);
						obj.put("picpath", picpath);
						array.add(obj);
					}
				}
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("Banners", array);
				return jsonObject;
			} else {
				throw new HandlerException("NOBanners");
			}
		} catch (Exception e) {
			// TODO: handle exception
			throw new HandlerException("NOBanners");
		}
	}

}
