package com.amarsoft.p2ptrade.util;

import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import com.amarsoft.are.ARE;


public class JavaSendMail {  
	
	private static int ConnectTimeout = 600000;
	
	private static int SocketTimeout = ConnectTimeout;
	
	 // �����ʼ�������������  
	private static String mailHost= ARE.getProperty("mailHost");
	private static String mailProxy = ARE.getProperty("mailProxy", "");
	//���÷�����
	private static String sendPersonMail= ARE.getProperty("sendPersonMail");
	//�����ʼ�����������
	private static String mailServiceName= ARE.getProperty("mailServiceName");
	//�����ʼ����������� 
    private static String mailServicePassword= ARE.getProperty("mailServicePassword");
    //���÷�������
    private String mailContent;
    //�����ռ�������
    private String receiveMail;
	public String getReceiveMail() {
		return receiveMail;
	}
	public void setReceiveMail(String receiveMail) {
		this.receiveMail = receiveMail;
	}
	public String getMailContent() {
		return mailContent;
	}
	public void setMailContent(String mailContent) {
		this.mailContent = mailContent;
	}
	
	public static void SendEmailByWeb(String sContent,String sReceiveMail) throws Exception{
		String sUrl = mailProxy.trim();
		CloseableHttpClient httpClient = null;//��ȡhttpClientʵ�� ������closebleHttpClient
		try{
			httpClient = HttpClients.createDefault();//httpClients.createDefault()���ó�ʼ����httpClentʵ��
			HttpPost httpMethod = new HttpPost(sUrl);//���httpPost��ʵ����new HttpHost(�����������������)
			List<NameValuePair> nameValuePairs =new ArrayList<NameValuePair>();//����������List<NameValuePair>
			nameValuePairs.add(new BasicNameValuePair("title","�񱾽���"));
			ARE.getLog().info("���post����:title=" + "�񱾽���");
			nameValuePairs.add(new BasicNameValuePair("content",sContent));
			ARE.getLog().info("���post����:content=" + sContent);
			nameValuePairs.add(new BasicNameValuePair("receiver",sReceiveMail));
			ARE.getLog().info("���post����:receiver=" + sReceiveMail);
			/*
			nameValuePairs.add(new BasicNameValuePair("mailHost",mailHost));
			ARE.getLog().info("���post����:mailHost=" + mailHost);
			nameValuePairs.add(new BasicNameValuePair("sender",sendPersonMail));
			ARE.getLog().info("���post����:sender=" + sendPersonMail);
			nameValuePairs.add(new BasicNameValuePair("senderpassword",mailServicePassword));
			ARE.getLog().info("���post����:senderpassword=" + mailServicePassword);
			*/
			httpMethod.setEntity(new UrlEncodedFormEntity(nameValuePairs,"GBK"));//httpEntity
			
			RequestConfig requestConfig = RequestConfig.custom()
					.setSocketTimeout(SocketTimeout)
					.setConnectTimeout(ConnectTimeout)
					.build();//��������ʹ��䳬ʱʱ��
			httpMethod.setConfig(requestConfig);
			
			ARE.getLog().info("connect to " + sUrl);
			CloseableHttpResponse response = httpClient.execute(httpMethod);//httpClient.excute()
			HttpEntity httpEntity = response.getEntity();
			int responseStatus = response.getStatusLine().getStatusCode();
			if(responseStatus==200){//��ȷ����
				String sResult = EntityUtils.toString(httpEntity).trim();
				if(sResult.equalsIgnoreCase("success")){
					ARE.getLog().info("send success");
					return;
				}
				else{
					throw new Exception("�����ʼ�ʧ�ܣ��������Ϊ" + sResult);
				}
			}
			else{
				throw new Exception("�����ʼ�ʧ�ܣ�ҳ��ִ�д��󣬴������Ϊ" + responseStatus);
			}
		}
		finally{
			if(httpClient!=null)httpClient.close();
		}
		
	}
	
    public static void SendEmail(String sContent,String sReceiveMail) throws Exception{
    	if(mailProxy!=null && mailProxy.trim().length()>0){
    		SendEmailByWeb(sContent,sReceiveMail);
    		return;
		}
    	 Properties props = new Properties();  
	        // ����debug����  
	        props.setProperty("mail.debug", "true");  
	        // ���ͷ�������Ҫ�����֤  
	        props.setProperty("mail.smtp.auth", "true");  
	        // �����ʼ�������������  
	        props.setProperty("mail.host", mailHost);  
	        // �����ʼ�Э������  
	        props.setProperty("mail.transport.protocol", "smtp");  
	        // ���û�����Ϣ  
	        Session session = Session.getInstance(props);  
	        // �����ʼ�����  
	        Message msg = new MimeMessage(session);  
	        msg.setSubject("�񱾽���");  
	        // �����ʼ�����  
	        msg.setText(sContent);  
	        // ���÷�����  
	        msg.setFrom(new InternetAddress(sendPersonMail));  
	          
	        Transport transport = session.getTransport();  
	        // �����ʼ�������  
	        transport.connect(mailServiceName, mailServicePassword);  
	        // �����ʼ�  
	        transport.sendMessage(msg, new Address[] {new InternetAddress(sReceiveMail)});  
	        // �ر�����  
	        transport.close(); 
    	
    }
	
 // MD5���ܺ���
 	public static String MD5Encode(String sourceString) {
 		String resultString = null;
 		try {
 			resultString = new String(sourceString);
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			resultString = byte2hexString(md.digest(resultString.getBytes()));
 		} catch (Exception ex) {
 		}
 		return resultString;
 	}

 	public final static String byte2hexString(byte[] bytes) {
 		StringBuffer bf = new StringBuffer(bytes.length * 2);
 		for (int i = 0; i < bytes.length; i++) {
 			if ((bytes[i] & 0xff) < 0x10) {
 				bf.append("0");
 			}
 			bf.append(Long.toString(bytes[i] & 0xff, 16));
 		}
 		return bf.toString();
 	}
	

}  
