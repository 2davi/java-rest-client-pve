package dev.the2davi.lab.api.storage.service;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxStorageDto;

@Service
public class StorageService {

	private final RestClient restClient;
	
	private StorageService(RestClient pveRestClient) {
		this.restClient = pveRestClient;
	}
	
	public List<ProxmoxStorageDto> getStorageList() {
		String uri = "/cluster/resources?type=storage";
		
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxStorageDto>>> responseType
				= new ParameterizedTypeReference<> () {};
		
		ProxmoxResponse<List<ProxmoxStorageDto>> response =  restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null
				? response.data()
				: Collections.emptyList();
	}
}
