package com.amarsoft.awe.common.pdf.watermark;

import java.awt.Color;

import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
/**
 * ����ˮӡʵ����
 * @author Administrator
 *
 */
public class TextWaterMark implements IWaterMark {
	
	private static String WATER_CONTENT = "www.amarsoft.com";

	public void run(PdfContentByte under) {
		BaseFont base = null;
		try{
			base = BaseFont.createFont("STSong-Light", "UniGB-UCS2-H",BaseFont.NOT_EMBEDDED);
		}
		catch(Exception e){
			e.printStackTrace();
		}
		under.beginText(); 
		under.setColorFill(Color.BLUE); 
		under.setFontAndSize(base, 30);
		int j = WATER_CONTENT.length();
		int rise = 500;
		// ����ˮӡ����������б ��ʼ 
		if (j >= 15) { 
			under.setTextMatrix(200, 120); 
			for (int k = 0; k < j; k++) { 
				under.setTextRise(rise); 
				char c = WATER_CONTENT.charAt(k); 
				under.showText(c + ""); 
				rise -= 20; 
			} 
		} else { 
			under.setTextMatrix(180, 100); 
			for (int k = 0; k < j; k++) { 
				under.setTextRise(rise); 
				char c = WATER_CONTENT.charAt(k); 
				under.showText(c + ""); 
				rise -= 18; 
			} 
		} 
		// �������ý��� 
		under.endText(); 

	}

}
