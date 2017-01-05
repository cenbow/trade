package com.amarsoft.awe.common.pdf.css;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * �����滻
 * @author Administrator
 *
 */
public class FontParser extends DefaultOuterCssParser {
	public String parse(String html, String resourcePath) {
		//ͨ��������ʽѭ����ȡlink��ǩ
		StringBuffer sb = new StringBuffer();
		Pattern pattern = Pattern.compile("[Ff][Oo][Nn][Tt][\\-][Ff][Aa][Mm][Ii][Ll][Yy]\\s*:[\\w\\W]+?;");
		Matcher m = pattern.matcher(html);
		while (m.find()) {
			String sFont = m.group(0);
			String[] arr = sFont.split(":");
			arr[1] = arr[1].substring(0,arr[1].length()-1).trim().toLowerCase();
			arr[1] = arr[1].replaceAll("\"", "").trim();
			if(arr[1].equals("����")){
				sFont = "font-family:SimSun;";
			}
			else if(arr[1].equals("����") || arr[1].equals("simhei")){
				sFont = "font-family:SimHei;";
			}
			else if(arr[1].indexOf("����")>-1 || arr[1].equals("simkai")){
				sFont = "font-family:KaiTi_GB2312;";
			}
			else{
				sFont = "font-family:SimSun;";
			}
			m.appendReplacement(sb, sFont);
		}
		m.appendTail(sb);
		//System.out.println(sb.toString());
		return sb.toString();
	}
}
