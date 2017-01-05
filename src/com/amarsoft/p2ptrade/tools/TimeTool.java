package com.amarsoft.p2ptrade.tools;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * ��ȡ��ǰ���ں�ʱ��
 * @author yhxu
 *
 */
public class TimeTool {
	private String sCurrentDate;
	private String sCurrentTime;
	private String sCurrentMoment;
	private String sChDate;
	
	/**
	 * ��ȡ����
	 * @return
	 */
	public String getsCurrentDate() {
		return sCurrentDate;
	}

	/**
	 * ��ȡʱ��
	 * @return
	 */
	public String getsCurrentTime() {
		return sCurrentTime;
	}
	
	/**
	 * ��ȡ����+ʱ��
	 * @return
	 */
	public String getsCurrentMoment() {
		return sCurrentMoment;
	}

	
	/**
	 * ��ȡ���ĸ�ʽʱ��
	 * @return
	 */
	public String getsChDate() {
		return sChDate;
	}

	/**
	 * ���������Ĺ��췽��
	 */
	public TimeTool() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		String sDateAndTime = sdf.format(date);
		String[] sTimearray = sDateAndTime.split(" ");
		sCurrentMoment = sDateAndTime;
		sCurrentDate = sTimearray[0];
		sCurrentTime = sTimearray[1];
		//05/15 14:31
		sChDate = sDateAndTime.substring(5, 16);
//		sChDate = "("+sChDate;
//		sChDate = sChDate.replace("/", ")��(").replace(" ", ")��(").replace(":", ")ʱ(");
//		sChDate = sChDate+")��";
		sChDate = sChDate.replace("/", "��").replace(" ", "��").replace(":", "ʱ");
		sChDate = sChDate+"��";
	}

	/**
	 * �������Ĺ��췽��
	 * @param sTimeForm
	 */
	public TimeTool(String sTimeForm) {
		SimpleDateFormat sdf = new SimpleDateFormat(sTimeForm);
		Date date = new Date();
		String sDateAndTime = sdf.format(date);
		String[] sTimearray = sDateAndTime.split(" ");
		sCurrentMoment = sDateAndTime;
		sCurrentDate = sTimearray[0];
		sCurrentTime = sTimearray[1];
	}
}
