package com.amarsoft.p2ptrade.util;

public interface MobileServiceInvoker {

	//�����ֻ���
	public void setPhone(String phone);
	//���÷�������
	public void setContent(String content);
	//��������
	public void send();
	//����״̬
	public boolean isSuccess();
}
