package dev.the2davi.lab.api.task.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.CmpTaskHistoryDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.task.dto.ProxmoxTaskDto;
import dev.the2davi.lab.api.task.dto.ProxmoxTaskLogDto;
import dev.the2davi.lab.cmmn.format.CmpUtils;

@Service
public class TaskService {
	private final RestClient restClient;
	
	public TaskService(RestClient pveRestClient) {
		this.restClient = pveRestClient;
	}
	
	/* Task List (Datacenter scope) */
	public List<CmpTaskHistoryDto> getTaskList() {
		String uri = "/cluster/tasks";
		
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxTaskDto>>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<List<ProxmoxTaskDto>> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		if(response == null || response.data() == null) {
			return Collections.emptyList();
		}
		
		return response.data().stream()
				.map(CmpUtils::parseToCmpTask)
				.collect(Collectors.toList());
	}
	
	/* Log Trace */
	public List<ProxmoxTaskLogDto> getTaskLog(String node, String upid) {
		String uri = String.format("/nodes/%s/tasks/%s/log?limit=100000", node, upid);
		
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxTaskLogDto>>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<List<ProxmoxTaskLogDto>> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null
				? response.data()
				: Collections.emptyList();
	}
}
