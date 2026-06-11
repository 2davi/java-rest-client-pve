package dev.the2davi.lab.audit.service;

import java.time.Instant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxTaskStatusDto;
import dev.the2davi.lab.audit.TaskOutcome;
import dev.the2davi.lab.audit.recorder.TaskOutcomeRecorder;

@Service
public class AuditService {
	
	private static final Logger log = LoggerFactory.getLogger(AuditService.class);
	private final RestClient restClient;
	private final TaskOutcomeRecorder recorder;
	
	public AuditService(RestClient restClient, TaskOutcomeRecorder recorder) {
		this.restClient = restClient;
		this.recorder = recorder;
	}
	
	@Async
	public void auditTaskStatus(String node, String upid) {
		final Instant startedAt = Instant.now();
		MDC.put("upid", upid);
		try{
			log.info("백그라운드 감시 시작");
			TaskOutcome o = watch(node, upid);
			record(o);
		} finally {
			//ThreadLocal 정리
			MDC.remove("upid");
		}
	}
	
	private TaskOutcome watch(String node, String upid) {
		final int maxRetries = 30;
		for(int attempt = 1; attempt <= maxRetries; attempt++) {
			try{
				ProxmoxTaskStatusDto statusInfo = getTaskStatus(node, upid);
				
				if(statusInfo != null && "stopped".equals(statusInfo.status())) {
					return TaskOutcome.completed(upid, node, statusInfo.exitStatus());
				}
				
				log.info("작업 진행 중... ({}/{})", attempt, maxRetries);
				Thread.sleep(2000);
			} catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return TaskOutcome.monitorFailed(upid, node, "interrupted");
			} catch(Exception e) {
				log.error("상태 조회 중 에러", e);
				return TaskOutcome.monitorFailed(upid, node, e.toString());
			}
		}
		return TaskOutcome.timedOut(upid, node);
	}
	
	private ProxmoxTaskStatusDto getTaskStatus(String node, String upid) {
		String uri = String.format("/nodes/%s/tasks/%s/status", node, upid);
		
		ParameterizedTypeReference<ProxmoxResponse<ProxmoxTaskStatusDto>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<ProxmoxTaskStatusDto> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null ? response.data() : null;
	}
	
	private void record(TaskOutcome o) {
		try{
			recorder.record(o);
		} catch(RuntimeException e) {
			log.error("TaskOutcome 기록 실패 ─ 결과 유실 방지 Trace: {}", o, e);
		}
	}
}





















