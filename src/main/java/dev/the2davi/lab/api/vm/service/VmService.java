package dev.the2davi.lab.api.vm.service;

import java.util.Collections;
import java.util.List;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import dev.the2davi.lab.api.dto.ProxmoxResponse;
import dev.the2davi.lab.api.vm.dto.ProxmoxVmCloneDto;
import dev.the2davi.lab.api.vm.dto.ProxmoxVmDestroyDto;
import dev.the2davi.lab.api.vm.dto.ProxmoxVmDto;
import dev.the2davi.lab.cmmn.format.TypeUtil;
import dev.the2davi.lab.cmmn.type.CloneType;

@Service
public class VmService {

	private final RestClient restClient;
	public VmService(RestClient pveRestClient) {
		this.restClient = pveRestClient;
	}
	
	/* VM List */
	@Deprecated
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
	public List<ProxmoxVmDto> getVmList() {
		String uri = "/cluster/resources?type=vm";
		ParameterizedTypeReference<ProxmoxResponse<List<ProxmoxVmDto>>> responseType = new ParameterizedTypeReference<>() {};
		
		ProxmoxResponse<List<ProxmoxVmDto>> response = restClient.get()
				.uri(uri)
				.retrieve()
				.body(responseType);
		
		return response != null && response.data() != null
				? response.data()
				: Collections.emptyList();
	}
	
	/* VM Control */
	public String controlVmStatus(String node, int vmid, String action) {
		String uri = String.format("/nodes/%s/qemu/%d/status/%s", node, vmid, action);
		String response = restClient.post()
				.uri(uri)
				.header("Content-Length", "0")
				.retrieve()
				.body(String.class);

		return response;
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
