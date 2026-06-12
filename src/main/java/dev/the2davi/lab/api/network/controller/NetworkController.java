package dev.the2davi.lab.api.network.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.network.dto.ProxmoxNetworkIfaceDto;
import dev.the2davi.lab.api.network.dto.ProxmoxNetworkIfaceRequestDto;
import dev.the2davi.lab.api.network.service.NetworkService;
import dev.the2davi.lab.monitor.TaskMonitor;

@RestController
@RequestMapping("/api/proxmox")
public class NetworkController {
	
	private final NetworkService service;
	private final TaskMonitor monitor;
	public NetworkController(NetworkService service, TaskMonitor monitor) {
		this.service = service;
		this.monitor = monitor;
	}
	
	/* Network List */
	@GetMapping("/nodes/{node}/network")
	public ResponseEntity<List<ProxmoxNetworkIfaceDto>> getNetworkList(
			@PathVariable String node) {
		return ResponseEntity.ok(service.getNetworkList(node));
	}
	
	/* Create Iface */
	@PostMapping("/nodes/{node}/network")
	public ResponseEntity<Map<String, String>> createIface(
			@PathVariable String node
			, @RequestBody ProxmoxNetworkIfaceRequestDto dto) {
		service.createIface(node, dto);
		return ResponseEntity.ok(Map.of(
				"message", dto.iface() + " 인터페이스 추가 (적용 전)"
		));
	}
	
	/* Modify Iface */
	@PutMapping("/nodes/{node}/network/{iface}")
	public ResponseEntity<Map<String, String>> modifyIface(
			@PathVariable String node
			, @PathVariable String iface
			, @RequestBody ProxmoxNetworkIfaceRequestDto dto) {
		service.modifyIface(node, iface, dto);
		return ResponseEntity.ok(Map.of(
				"message", iface + " 인터페이스 수정 (적용 전)"
		));
	}
	
	/* Remove Iface */
	@DeleteMapping("/nodes/{node}/network/{iface}")
	public ResponseEntity<Map<String, String>> removeIface(
			@PathVariable String node
			, @PathVariable String iface) {
		service.removeIface(node, iface);
		return ResponseEntity.ok(Map.of(
				"message", iface + " 인터페이스 제거 (적용 전)"
		));
	}
	
	/* Apply Network Settings */
	@PutMapping("/nodes/{node}/network")
	public ResponseEntity<Map<String, String>> applyNetwork(
			@PathVariable String node) {
		String upid = service.applyNetwork(node);
		
		if(upid != null) monitor.traceTaskStatus(node, upid);
		return ResponseEntity.ok(Map.of(
			"message", "네트워크 설정 적용(reload) 시작됨",
			"upid", upid != null ? upid : ""
		));
	}
	
	/* Revert Network Settings */
	@DeleteMapping("/nodes/{node}/network")
	public ResponseEntity<Map<String, String>> revertNetwork(
			@PathVariable String node) {
		service.revertNetwork(node);
		return ResponseEntity.ok(Map.of(
				"message", "적용 전 네트워크 변경 되돌림."
		));
	}
	
}
