package com.amarsoft.p2ptrade.util;

import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.binary.Base64;

public class UrlBaseTest {
	public static void main(String[] args) throws UnsupportedEncodingException {  
		       String str = "2014120500000002";  
			String s="����~<br>��л��ע��񱾽��ڣ�����֤�������䡣���������֤���䣺<br> http://localhost:8080/qsh_p2p/member/account/mail_success.jsp?userid=MjAxNDExMTYwMDAyMQ==&serialno=MjAxNDEyMDUwMDAwMDAwOA==&chkmsg=MDgyODQy<br>����������޷����,����ֱ�Ӹ������µ�ַ��������������֤��<br>http://localhost:8080/qsh_p2p/member/account/mail_success.jsp?userid=MjAxNDExMTYwMDAyMQ==&serialno=MjAxNDEyMDUwMDAwMDAwOA==&chkmsg=MDgyODQy,_KEY_SERIALNO=2014120500000008";
	       System.out.println("sssssssssssssssssssssssssssssssss"+s.length());
			// ���ܸ��ַ���  
	       String encodedString = UrlBase64.encoded(str);  
		      System.out.println(encodedString);  
	        // ���ܸ��ַ���  
		       String decodedString = UrlBase64.decode(encodedString);  
		       System.out.println(decodedString);  
		   }  

}
