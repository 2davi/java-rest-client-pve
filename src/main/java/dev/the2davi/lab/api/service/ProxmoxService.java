package dev.the2davi.lab.api.service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.CmpTaskHistoryDto;
import dev.the2davi.lab.api.dto.ProxmoxNodeDto;
import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.dto.ProxmoxStorageDto;
import dev.the2davi.lab.api.dto.ProxmoxTaskDto;
import dev.the2davi.lab.api.dto.ProxmoxTaskLogDto;
import dev.the2davi.lab.api.dto.ProxmoxVmCloneDto;
import dev.the2davi.lab.api.dto.ProxmoxVmDestroyDto;
import dev.the2davi.lab.api.dto.ProxmoxVmDto;
import dev.the2davi.lab.cmmn.format.CmpUtils;
import dev.the2davi.lab.cmmn.format.TypeUtil;
import dev.the2davi.lab.cmmn.type.CloneType;

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
	
	public String controlVmStatus(String node, int vmid, String action) {
		String uri = String.format("/nodes/%s/qemu/%d/status/%s", node, vmid, action);
		String response = restClient.post()
				.uri(uri)
				.header("Content-Length", "0")
				.retrieve()
				.body(String.class);

		return response;
	}
	
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
	
	public List<CmpTaskHistoryDto> getTaskList(String node) {
		String uri = String.format("/nodes/%s/tasks", node);
		
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
	
	public List<ProxmoxVmDto> getVmList(String node) {
		String uri = String.format("/nodes/%s/qemu", node);
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxVmDto>>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<List<ProxmoxVmDto>> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null
				? response.data()
				: Collections.emptyList();
	}
	
	
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
	
	/* VM Clone */
	public String cloneVm(String node, int vmid, ProxmoxVmCloneDto dto) {
		String uri = String.format("/nodes/%s/qemu/%d/clone", node, vmid);
		StringBuilder formData = new StringBuilder();
		
		formData.append("newid=").append(dto.newVmid());
		
		if(StringUtils.hasText(dto.name())) {
			formData.append("&name=").append(TypeUtil.encodeUTF_8(dto.name()));
		}
		
		formData.append("&full=").append(CloneType.verify(dto.isFull()));
		
		return restClient.post()
				.uri(uri)
				.header("Content-Type", "application/x-www-form-urlencoded")
				.body(formData.toString())
				.retrieve()
				.body(String.class); //Controller에서 UPID 파싱하기 위해 원시 타입 반환
	}
	
	/* VM Destroy */
	public String deleteVm(String node, int vmid, ProxmoxVmDestroyDto dto) {
		// purge=1 & destroy-unreferenced-disk=1 :
		String uri = String.format("/nodes/%s/qemu/%d?purge=%d&destroy-unreferenced-disks=%d", 
				node, 
				vmid, 
				Boolean.TRUE.equals(dto.purge()) ? 1 : 0, 
				Boolean.TRUE.equals(dto.destroyUnreferencedDisk()) ? 1 : 0
		);
		
		return restClient.delete()
				.uri(uri)
				.retrieve()
				.body(String.class);
	}
}



























