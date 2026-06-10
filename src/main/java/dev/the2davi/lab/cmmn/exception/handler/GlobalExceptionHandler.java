package dev.the2davi.lab.cmmn.exception.handler;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;

import dev.the2davi.lab.cmmn.exception.response.ExceptionMessageResponse;

@RestControllerAdvice(basePackages = "dev.the2davi.lab.api")
@Order(Ordered.LOWEST_PRECEDENCE)
public class GlobalExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private final MessageSourceAccessor messageSource;
	
	private GlobalExceptionHandler(MessageSourceAccessor messageSource) {
		this.messageSource = messageSource;
	}
	
	public ResponseEntity<ExceptionMessageResponse> unauthorized(HttpClientErrorException.Unauthorized e, Locale locale) {
		HttpStatusCode status = HttpStatus.UNAUTHORIZED;
		
		String statusCode = String.valueOf(status.value());
		String alertMessage = "아이디·비밀번호·REALM을 확인하세요.";
		String debugMessage = "아이디/비밀번호/Realm 틀림, 2FA.";
		Void data = null;
		String changes = null;
		ExceptionMessageResponse<Void> emr = new ExceptionMessageResponse<> (
				statusCode
				, alertMessage
				, debugMessage
				, data
				, changes
		);
		
	}
}
