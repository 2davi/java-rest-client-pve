package dev.the2davi.lab.cmmn.exception.error.auth;

import org.springframework.http.HttpStatus;

import dev.the2davi.lab.cmmn.exception.error.CmpException;

public class InvalidCredentialsException extends CmpException {
	private static final long serialVersionUID = 5761584807187504322L;

	public InvalidCredentialsException(Throwable cause) {
		super(HttpStatus.UNAUTHORIZED, "error.auth.invalid-credentials", cause);
	}
}
