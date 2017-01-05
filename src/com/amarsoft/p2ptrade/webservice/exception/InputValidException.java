package com.amarsoft.p2ptrade.webservice.exception;

public class InputValidException extends Exception {
    public InputValidException() {
        super();
    }
    /**
     * ���ݸ�����Ϣ��������
     *
     * @param message ��ϸ��Exception��Ϣ
     */
    public InputValidException(String message) {
        super(message);
    }

    /**
     * ���ݸ�����ԭʼ�������Ϣ����һ���µ��쳣����ϸ��Ϣ�Ǹ�������Ϣ��Դ��������
     *
     * @param message ��ϸ��Ϣ
     * @param cause ԭʼ����
     */
    public InputValidException(String message, Throwable cause) {
        super(message + " (Caused by " + cause + ")");
        this.cause = cause; // Two-argument version requires JDK 1.4 or later
    }

    /**
     * ԭʼ����.
     */
    protected Throwable cause = null;
    /**
     * Return the underlying cause of this exception (if any).
     */
    public Throwable getCause() {
        return (this.cause);
    }
}
