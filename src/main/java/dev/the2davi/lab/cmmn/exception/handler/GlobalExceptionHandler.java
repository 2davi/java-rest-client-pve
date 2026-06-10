package dev.the2davi.lab.cmmn.exception.handler;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import dev.the2davi.lab.cmmn.exception.error.CmpException;

@RestControllerAdvice(basePackages = "dev.the2davi.lab.api")
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
	
	private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
	private final MessageSourceAccessor messageSource;
	
	GlobalExceptionHandler(MessageSourceAccessor messageSource) {
		this.messageSource = messageSource;
	}
	
	private ProblemDetail problem(HttpStatus status, String code) {
		String detail = messageSource.getMessage(code, null, status.getReasonPhrase(), Locale.getDefault());
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(status, detail);
		pd.setProperty("code", code);
		return pd;
	}
	
	/**
	 * <pre>
	 * 도메인 차원의 예외를 핸들링.
	 * </pre>
	 * @param e
	 * @param locale
	 * @return
	 * @author kcy0122
	 * @since 2026-06-10
	 */
	@ExceptionHandler(CmpException.class)
	public ProblemDetail handleCmp(CmpException e, Locale locale) {
		//백엔드 서버 로그에는 에러의 원본 스택까지 출력
		log.warn("[{}] {}", e.getStatus(), e.getMessageCode(), e);
		
		String detail = messageSource.getMessage(
				e.getMessageCode()
				, e.getArgs()
				, e.getMessageCode()
				, locale
		);
		ProblemDetail pd = ProblemDetail.forStatusAndDetail(e.getStatus(), detail);
		
		//프론트엔드가 분기할 상태 코드
		pd.setProperty("code", e.getMessageCode());
		return pd;
	}
	
	/**
	 * <pre>
	 * PVE 인프라 차원의 예외를 핸들링
	 * </pre>
	 * @param e
	 * @return
	 * @author kcy0122
	 * @since 2026-06-10
	 */
	@ExceptionHandler(ResourceAccessException.class)
	public ProblemDetail pveUnavailable(ResourceAccessException e) {
		log.error("PVE 연결 실패", e);
		return problem(HttpStatus.SERVICE_UNAVAILABLE, "error.pve.unavailable");
	}
	
	@ExceptionHandler(HttpServerErrorException.class)
	public ProblemDetail pveServer(HttpServerErrorException e) {
		log.error("PVE 5xx", e);
		return problem(HttpStatus.BAD_GATEWAY, "error.pve.server");
	}
	
	/**
	 * <pre>
	 * 최후 안전망
	 * - exception 패키지에서 누락된 예외를 수렴
	 * </pre>
	 * @param e
	 * @return
	 * @author kcy0122
	 * @since 2026-06-10
	 */
	@ExceptionHandler(Exception.class)
	public ProblemDetail unknown(Exception e) {
		log.error("처리되지 않은 예외", e);
		return problem(HttpStatus.INTERNAL_SERVER_ERROR, "error.unknown");
	}
	
//	public ResponseEntity<ExceptionMessageResponse> unauthorized(HttpClientErrorException.Unauthorized e, Locale locale) {
//		HttpStatusCode status = HttpStatus.UNAUTHORIZED;
//		
//		String statusCode = String.valueOf(status.value());
//		String alertMessage = "아이디·비밀번호·REALM을 확인하세요.";
//		String debugMessage = "아이디/비밀번호/Realm 틀림, 2FA.";
//		Void data = null;
//		String changes = null;
//		ExceptionMessageResponse<Void> emr = new ExceptionMessageResponse<> (
//				statusCode
//				, alertMessage
//				, debugMessage
//				, data
//				, changes
//		);
//		
//		return ResponseEntity.status(status.value()).contentType(MediaType.APPLICATION_JSON).body(emr);
//	}
}
