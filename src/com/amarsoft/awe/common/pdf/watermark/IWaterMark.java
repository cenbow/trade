package com.amarsoft.awe.common.pdf.watermark;
/**
 * ˮӡ�����ӿ�
 */
import com.lowagie.text.pdf.PdfContentByte;

public interface IWaterMark {
	void run(PdfContentByte under)throws Exception;
}
