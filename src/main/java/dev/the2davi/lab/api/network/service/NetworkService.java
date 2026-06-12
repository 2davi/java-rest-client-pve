package dev.the2davi.lab.api.network.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.network.dto.ProxmoxNetworkIfaceDto;
import dev.the2davi.lab.api.network.dto.ProxmoxNetworkIfaceRequestDto;
import dev.the2davi.lab.cmmn.format.TypeUtil;

@Service
public class NetworkService {

	private final RestClient restClient;
	public NetworkService(RestClient pveRestClient) {
		this.restClient = pveRestClient;
	}
	
	/* Network List */
	public List<ProxmoxNetworkIfaceDto> getNetworkList(String node) {
		String uri = String.format("/nodes/%s/network", node);
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxNetworkIfaceDto>>> responseType
			= new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<List<ProxmoxNetworkIfaceDto>> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null
				? response.data()
				: Collections.emptyList();
	}
	
	/* Create Iface */
	public void createIface(String node, ProxmoxNetworkIfaceRequestDto dto) {
		String uri = String.format("/nodes/%s/network", node);
		StringBuilder formData = new StringBuilder();
		
		formData.append("iface=").append(TypeUtil.encodeUTF_8(dto.iface()))
				.append("&type=").append(dto.type());
		appendConfig(formData, dto.config());
		
		restClient.post()
			.uri(uri)
			.header("Content-Type", "application/x-www-form-urlencoded")
			.body(formData.toString())
			.retrieve()
			.toBodilessEntity();
	}
	
	/* Modify Iface */
	public void modifyIface(String node, String iface, ProxmoxNetworkIfaceRequestDto dto) {
		String uri = String.format("/nodes/%s/network/%s", node, iface);
		StringBuilder formData = new StringBuilder();
		
		//PVE PUT은 type을 요구한다.
		formData.append("type=").append(dto.type());
		appendConfig(formData, dto.config());
		
		//필드를 지우기 위해, delete 파라미터로 키를 넘긴다.
		if(dto.delete() != null && !dto.delete().isEmpty()) {
			formData.append("&delete=").append(String.join(",", dto.delete()));
		}
		
		restClient.put()
				.uri(uri)
				.header("Content-Type", "application/x-www-form-urlenocded")
				.body(formData.toString())
				.retrieve()
				.toBodilessEntity();
	}
	
	/* Remove Iface */
	public void removeIface(String node, String iface) {
		String uri = String.format("/nodes/%s/network/%s", node, iface);
		restClient.delete()
				.uri(uri)
				.retrieve()
				.toBodilessEntity();
	}
	
	/**
	 * Apply Network Settings 
	 * 
	 * @return upid
	 */
	public String applyNetwork(String node) {
		String uri = String.format("/nodes/%s/network", node);
		
		ParameterizedTypeReference<ProxmoxResponse<String>> responseType
				= new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<String> response = restClient.put()
				.uri(uri)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.retrieve()
				.body(responseType);
		return response != null ? response.data() : null;
	}
	
	/**
	 * Revert Network Settings
	 * interfaces.new 제거
	 */
	public void revertNetwork(String node) {
		String uri = String.format("/nodes/%s/network", node);
		restClient.put()
			.uri(uri)
			.retrieve()
			.toBodilessEntity();
	}
	
	private void appendConfig(StringBuilder sb, Map<String, Object> config) {
		if(config == null) return;
		config.forEach((k, v) -> {
			String ev = TypeUtil.encodeUTF_8(v);
			if(ev != null) {
				sb.append("&").append(k).append("=").append(ev);
			}
		});
	}
}


































