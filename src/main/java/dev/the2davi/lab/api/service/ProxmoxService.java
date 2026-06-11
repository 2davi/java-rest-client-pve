package dev.the2davi.lab.api.service;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxNodeDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxStorageDto;
import dev.the2davi.lab.cmmn.format.TypeUtil;

@Service
public class ProxmoxService {
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
	

	

	
//	public List<ProxmoxTaskDto> getTaskList(String node) {
//		String uri = String.format("/nodes/%s/tasks", node);
//		
//		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxTaskDto>>> responseType = new ParameterizedTypeReference<>() {};
//		
//		ProxmoxResponse<List<ProxmoxTaskDto>> response = restClient.get()
//				.uri(uri)
//				.retrieve()
//				.body(responseType);
//		
//		return response != null && response.data() != null
//				? response.data()
//				: Collections.emptyList();
//	}
	

	

	
	
	/* Storage */
	public void createStorage(ProxmoxStorageDto dto) {
		String uri = "/storage";
		StringBuilder formData = new StringBuilder();
		
		formData.append("type=").append(dto.type())
				.append("&storage=").append(dto.storage())
				.append("&content=").append(dto.content() != null ? dto.content() : "images");
		
		if(dto.config() != null) {
			dto.config().forEach((k,v) -> {
				String ev = TypeUtil.encodeUTF_8(v);
				if(ev != null) {
					formData.append("&").append(k).append("=").append(ev);
				}
			});
		}
		
		restClient.post()
				.uri(uri)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.body(formData.toString())
				.retrieve()
				.toBodilessEntity();
	}
	

}



























