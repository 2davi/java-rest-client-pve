package dev.the2davi.lab.api.service;

import java.util.Collections;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxNodeDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxTaskStatusDto;
import lombok.extern.slf4j.Slf4j;

@Service @Slf4j
public class ProxmoxService {

	private static final Logger log = LoggerFactory.getLogger(ProxmoxService.class);
	private final RestClient restClient;
	public ProxmoxService(RestClient pveRestClient) {
		this.restClient = pveRestClient;
	}
	
	public List<ProxmoxNodeDto> getClusterNodes() {
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxNodeDto>>> responseType = new ParameterizedTypeReference<>() {};
		ProxmoxResponse<List<ProxmoxNodeDto>> response = restClient.get()
				.uri("/nodes")
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null ? response.data() : Collections.emptyList();
	}
	
	public String controlVmStatus(String node, int vmid, String action) {
		String uri = String.format("/nodes/%s/qemu/%d/status/%s", node, vmid, action);
		String response = restClient.post()
				.uri(uri)
				.header("Content-Length", "0")
				.retrieve()
				.body(String.class);

		return response;
	}
	
	public ProxmoxTaskStatusDto getTaskStatus(String node, String upid) {
		String uri = String.format("/nodes/%s/tasks/%s/status", node, upid);
		
		ParameterizedTypeReference<ProxmoxResponse<ProxmoxTaskStatusDto>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<ProxmoxTaskStatusDto> response = restClient
				.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null ? response.data() : null;
	}
	
	@Async
	public void monitorTaskStatus(String node, String upid) {
		log.info("0...백그라운드 감시 시작 [Task: {}]", upid);
		
		int maxRetries = 30;
		int currentTry = 0;
		
		while(currentTry < maxRetries) {
			try {
				//Task 상태를 조회해 온다.
				ProxmoxTaskStatusDto statusInfo = getTaskStatus(node, upid);
				
				if(statusInfo != null && "stopped".equals(statusInfo.status())) {
					if("OK".equals(statusInfo.exitstatus())) {
						log.info("1...작업 완료 [Task: {} 성공적으로 끝남!]", upid);
					} else {
						log.error("0...작업 실패 [Task: {} 에러 발생: {}]", upid, statusInfo.exitstatus());
					}
					
					//stopped 상태로 들어왔으면 TASK 종료. 작업 끝났으니 스레드 탈출.
					return;
				}
				
				log.info("0...작업 진행 중... (시도 횟수: {}/{})", currentTry + 1, maxRetries);
				Thread.sleep(2000);
				currentTry++;
			} catch(InterruptedException e) {
				log.error("Watcher Thread 문제 발생..!", e);
				return;
			} catch(Exception e) {
				log.error("상태 조회 중 에러 발생..!", e);
				return;
			}
		}
		log.warn("작업이 60초 이상 진행 중... Watcher Thread Out..!");
	}
	
}
