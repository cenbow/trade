package com.amarsoft.p2ptrade.front;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;

import com.amarsoft.are.ARE;
import com.amarsoft.are.jbo.BizObject;
import com.amarsoft.are.jbo.BizObjectManager;
import com.amarsoft.are.jbo.BizObjectQuery;
import com.amarsoft.are.jbo.JBOFactory;
import com.amarsoft.awe.util.json.JSONArray;
import com.amarsoft.awe.util.json.JSONObject;
import com.amarsoft.mobile.webservice.ServiceFactory;
import com.amarsoft.mobile.webservice.business.HandlerException;
import com.amarsoft.mobile.webservice.business.JSONHandler;
import com.amarsoft.mobile.webservice.security.Base64;

/**
 * ͼƬ���洦����
 * 
 * ��������� BizType:ҵ�����ͣ��ͻ�����ģ��-CustomerManagement��ҵ�����롪BusinessApply�����ƣ������䣻
 * ObjectNo-�����ţ����ǿͻ�������Ϊ�ͻ���ţ���Ϊҵ��������Ϊҵ���ţ����ƣ�������;
 * ObjectType-�������ͣ���Ϊ�ͻ������Ϊ�ͻ����ͣ�Ĭ��ͬBizType,�Ǳ��䣻
 * 
 */
public class PhotoLoadHandler extends JSONHandler {

	/**
	 * ҵ������
	 */
	private String bizType;
	/**
	 * ������
	 */
	private String objectNo;
	/**
	 * �������ͣ�Ĭ����bizTypeһ�£�
	 */
	private String objectType;
	private String sUserID;

	public Object createResponse(JSONObject request, Properties arg1)
			throws HandlerException{

		if (request.containsKey("UserID")) {
			sUserID = request.get("UserID").toString();
		} else {
			throw new HandlerException("ȱ�ٲ���UserID");
		}
		if (request.containsKey("BizType")) {
			bizType = request.get("BizType").toString();
		} else {
			throw new HandlerException("ȱ�ٲ���BizType");
		}

		if (request.containsKey("ObjectNo")) {
			objectNo = request.get("ObjectNo").toString();
		} else {
			throw new HandlerException("ȱ�ٲ���ObjectNo");
		}

		if (bizType == null || bizType.trim().length() == 0)
			throw new HandlerException("����BizType����");

		if (objectNo == null || objectNo.trim().length() == 0)
			throw new HandlerException("����ObjectNo����");

		if (request.containsKey("ObjectType")) {
			objectType = request.get("ObjectType").toString();
		} else {
			objectType = bizType;
		}

		JSONObject result = new JSONObject();
		try {
//			String sSqlPhoto = "select PI.photodesc as PhotoDesc,address as Address,LATITUDE as Latitude,LONGITUDE as Longitude,PI.PhotoPath as PhotoPath"
//					+ " from photo_info PI "
//					+ " WHERE PI.ObjectNo ='"
//					+ objectNo
//					+ "' and PI.ObjectType = '"
//					+ objectType
//					+ "' and PI.BizType = '"
//					+ bizType
//					+ "' and INPUTUSER='"
//					+ userId + "'";
//			PreparedStatement psPhoto = conn.prepareStatement(sSqlPhoto);
//			ARE.getLog().debug(sSqlPhoto);
//			ResultSet rsPhoto = psPhoto.executeQuery();
			
			JBOFactory jbo = JBOFactory.getFactory();
			BizObjectManager m = jbo.getManager("jbo.trade.photo_info");
			BizObjectQuery q = m.createQuery("ObjectNo=:ObjectNo and ObjectType=:ObjectType and BizType=:BizType and INPUTUSER=:INPUTUSER");
			q.setParameter("ObjectNo", objectNo).setParameter("ObjectType", objectType)
			.setParameter("BizType", bizType).setParameter("INPUTUSER", sUserID);
			String sPhotoRootPath = ServiceFactory.getFactory().getUploadRootPath();// ��·��
			String sReturnPhotoData = "";
			JSONArray photoDescs = new JSONArray();
			
			List<BizObject> list = q.getResultList(false);
			for(BizObject o : list){
				String sPhotoPath = o.getAttribute("PhotoPath").toString();// ���·��
				sPhotoPath = sPhotoRootPath + sPhotoPath;// ����·��
				ARE.getLog().info(sPhotoPath);
				try {
					FileInputStream fis = new FileInputStream(sPhotoPath);// sPhotpPath��ͼƬ����·��
					byte[] bPhotoData = getBytes(fis);// ��ȡȡ�ļ�ת���ɶ�����
					fis.close();
					// �����ƾ���base64���뽫������ת�����ַ���
					String sReturnPhotoData1 = Base64.encode(bPhotoData);
					sReturnPhotoData = sReturnPhotoData + sReturnPhotoData1 + ",";
					JSONObject photoDescObject = new JSONObject();
					photoDescObject.put("PhotoDesc",o.getAttribute("PhotoDesc").toString());//ͼƬ����
					photoDescObject.put("Address",o.getAttribute("Address").toString());//��ַ
					photoDescObject.put("Longitude",o.getAttribute("Longitude").toString());//����
					photoDescObject.put("Latitude",o.getAttribute("Latitude").toString());//γ��
					photoDescs.add(photoDescObject);
				} catch (FileNotFoundException exception) {
					exception.printStackTrace();
				}
			}
			ARE.getLog().info(sReturnPhotoData + "  ");
			ARE.getLog().info(photoDescs.toString());
			result.put("PhotoData", sReturnPhotoData);// ��Ƭ����
			result.put("PhotoDescs", photoDescs);// ��Ƭ����
			return result;
		} catch (Exception e) {
			throw new HandlerException(e.getMessage());
		}
	}

	private byte[] getBytes(InputStream is) throws IOException {
		ByteArrayOutputStream ois = new ByteArrayOutputStream();
		byte[] buffer = new byte[10240];
		int b = is.read(buffer);
		while (b > -1) {
			ois.write(buffer, 0, b);
			b = is.read(buffer);
		}
		return ois.toByteArray();
	}
}
