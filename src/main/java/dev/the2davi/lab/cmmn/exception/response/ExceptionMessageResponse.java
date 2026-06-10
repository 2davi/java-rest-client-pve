package dev.the2davi.lab.cmmn.exception.response;

public record ExceptionMessageResponse<OBJECT>(
		String statusCode		//응답 코드
		, String alertMessage	//응답 메시지
		, String debugMessage	//디버깅 메시지
		, OBJECT data			//응답 데이터
		, String changes		//변경된 상태
) {}
