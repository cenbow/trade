package com.amarsoft.awe.common.pdf;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.tidy.Tidy;
import org.xhtmlrenderer.pdf.ITextFontResolver;
import org.xhtmlrenderer.pdf.ITextRenderer;
import org.xhtmlrenderer.pdf.PDFEncryption;

import com.amarsoft.are.ARE;
import com.amarsoft.awe.common.pdf.css.CssParserFactory;
import com.amarsoft.awe.common.pdf.util.StringConvertor;
import com.lowagie.text.pdf.BaseFont;
/**
 * htmlģ������pdf
 * @author flian
 *
 */
public class Html2Pdf {
	
	/** 
	 * �����ļ�����Ŀ¼
	 */
	public static String FONTS_DIR = "";//ARE.getProperty("APP_HOME")+"/etc/fonts/";
	
	/**
	 * �ĵ�����Ȩ����
	 */
	public static String OWNER_PWD = "123456";
	
	/**
	 * �ĵ��ɶ�Ȩ������
	 */
	public static String READER_PWD = "";
	
	/**
	 * html��תpdf��
	 * @param htmlStream html������
	 * @param outputStream �����
	 * @param sourceDir ��Դ·��
	 * @throws Exception 
	 */
	public static void generatePdf(InputStream htmlStream,OutputStream outputStream,String sourceDir)throws Exception{
		//(new com.lowagie.text.pdf.BaseFont()).getc
		//�������·���Ƿ�����
		if(checkFontPath()==false){
			throw new Exception("��Ч������·��:"+FONTS_DIR);
		}
		ByteArrayOutputStream tidyOut = null;
		Reader inputReader = null;
		InputStream tidyIn = null;
		
		try {
			tidyOut = new ByteArrayOutputStream();
			Tidy tidy = new Tidy();
			tidy.setXHTML(true);
			tidy.setInputEncoding("gbk"); 
			tidy.setOutputEncoding("gbk"); //Ϊ�˱��ⲿ�������ַ������ĵ��޷������������������Ϊgbk
			//tidy.parse(input, tidyOut);��Ϊ�����д��룬��css����
			String sSource = StringConvertor.inputStream2String(htmlStream, "gbk");
			//css�滻
			sSource =  CssParserFactory.getCssParser().parse(sSource, sourceDir);
			//�滻<o:p>��ǩ
			sSource = sSource.replaceAll("<o:p>", "");
			sSource = sSource.replaceAll("</o:p>", "");
			//�滻st1:chsdate��ǩ
			sSource = sSource.replaceAll("<st1:chsdate[\\w\\W]+?>", "");
			//Ϊtable��ǩ����bordercolor
			sSource = sSource.replaceAll("<\\s*[Tt][Aa][Bb][Ll][Ee]", "<table bordercolor=''");
			//�����滻
			sSource = CssParserFactory.getCssParser("com.amarsoft.awe.common.pdf.css.FontParser").parse(sSource, null);
			//htmlsource -> tidy
			inputReader = new StringReader(sSource);
			tidy.parse(inputReader, tidyOut);
			tidyIn = new ByteArrayInputStream(tidyOut.toByteArray());
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder db = dbf.newDocumentBuilder();
			Document doc = db.parse(tidyIn);
			
			//tidy->pdf
			Element style = doc.createElement("style");
			//����ĵ���û���������壬��ô��������Ĭ�����壺simsun
			style.setTextContent("body {font-family:SimSun;}\n@page {size: 8.5in 11in; }");
			Element root = doc.getDocumentElement();
			//root.getElementsByTagName("head").item(0).appendChild(style);
			ITextRenderer renderer = new ITextRenderer();
			//����
			//encrypt(renderer);
			// �������֧������
			ITextFontResolver fr = renderer.getFontResolver();	
			//����Ĭ������simsun��֧��
			fr.addFont(FONTS_DIR + "simsun.ttc",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			//����html�ĵ������Ӷ�������֧��
			fr.addFont(FONTS_DIR + "simkai.ttf",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			fr.addFont(FONTS_DIR + "simhei.ttf",BaseFont.IDENTITY_H,BaseFont.NOT_EMBEDDED);
			renderer.setDocument(doc,"http://www.amarsoft.com");
			//������Դ��·��
			renderer.getSharedContext().setBaseURL("file://" + sourceDir );   
			//renderer.getSharedContext().setBaseURL("http://localhost:8081/P2P_Loan/investment/agreement/");   
			renderer.layout();
			//�����
			renderer.createPDF(outputStream);
		} finally{
			if(tidyOut != null){
				try {
					tidyOut.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(tidyIn != null){
				try {
					tidyIn.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if(inputReader != null){
				try {
					inputReader.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	
	//����
	private static void encrypt(ITextRenderer renderer){
		PDFEncryption enc = new PDFEncryption();
		enc.setOwnerPassword(OWNER_PWD.getBytes());
		enc.setUserPassword(READER_PWD.getBytes());
		renderer.setPDFEncryption(enc);
	}
	
	private static boolean checkFontPath() {
		File file = new File(FONTS_DIR);
		return file.exists();
	}

	/**
	 * html�ļ�תpdf�ļ�
	 * @param htmlPath html�ļ�ȫ����·��
	 * @param outputPath ����ļ�ȫ����·��
	 * @param sourceDir ��Դ�ļ�Ŀ¼
	 * @throws Exception
	 */
	public static void generatePdf(String htmlPath,String outputPath,String sourceDir)throws Exception{
		InputStream input = null;
		OutputStream output = null;
		try {
			input = new FileInputStream(htmlPath);
			output = new FileOutputStream(outputPath); 
			generatePdf(input,output,sourceDir);
			output.flush();
		} finally {
			if(output != null){
				output.close();
			}
			if(input != null){
				input.close();
			}
		}
	}
	
	public static void main(String[] args)throws Exception{
		System.out.println("start");
		String sSerialno = "20140701000013";
		String htmlPath = "/home/yhxu/workspace/P2P_Service/WebRoot/Contract/recharge.html";
		String outputPath = "/home/yhxu/pdf/"+sSerialno+".pdf";
		String sFonts="/home/yhxu/workspace/P2P_Service/WebRoot/WEB-INF/etc/fonts/";
		Html2Pdf.FONTS_DIR = sFonts;
		Html2Pdf.generatePdf(htmlPath, outputPath, "/home/yhxu/workspace/P2P_Service/WebRoot/Contract/");
		System.out.println("end");		
	}
}
