package dev.the2davi.lab.cmmn.exception.error;

import org.springframework.http.HttpStatus;

public class CmpException extends RuntimeException{
	private static final long serialVersionUID = -1247364066058552550L;
	
	private final HttpStatus status;
	private final String messageCode;		//MessageSoruce Key
	private final transient Object[] args;	//메시지 치환 인자(없으면 빈 배열)
	
	protected CmpException(HttpStatus status, String messageCode, Throwable cause, Object... args) {
		super(messageCode, cause);
		this.status = status;
		this.messageCode = messageCode;
		this.args = args;
	}
	
	public HttpStatus getStatus() {return status;}
	public String getMessageCode() {return messageCode;}
	public Object[] getArgs() {return args;}
}
