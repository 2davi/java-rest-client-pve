package dev.the2davi.lab.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.dto.ProxmoxNodeDto;
import dev.the2davi.lab.api.dto.ProxmoxTaskStatusDto;
import dev.the2davi.lab.api.service.ProxmoxService;

@RestController
@RequestMapping("/api/proxmox")
public class ProxmoxController {

	private final ProxmoxService pveService;
	
	public ProxmoxController(ProxmoxService pveService) {
		this.pveService = pveService;
	}
	
	@GetMapping("/nodes")
	public ResponseEntity<List<ProxmoxNodeDto>> getNodes() {
		List<ProxmoxNodeDto> nodes = pveService.getClusterNodes();
		return ResponseEntity.ok(nodes);
	}
	
	@PostMapping("/nodes/{node}/qemu/{vmid}/status/{action}")
	public ResponseEntity<String> controlVm(
			@PathVariable("node") String node
			, @PathVariable("vmid") int vmid
			, @PathVariable("action") String action) {
		
		if(!action.equals("start") && !action.equals("stop") && !action.equals("shutdown")) {
			return ResponseEntity.badRequest().body("지원하지 않는 명령.");
		}
		
		String result = pveService.controlVmStatus(node, vmid, action);
		
		String upid = result.replace("{\"data\":\"", "").replace("\"}", "").trim();
		pveService.monitorTaskStatus(node, upid);
		
		return ResponseEntity.ok("명령 전송 성공. Task ID: " + result);
	}
	
	@GetMapping("/nodes/{node}/task/{upid}/status")
	public ResponseEntity<ProxmoxTaskStatusDto> checkTaskStatus(
			@PathVariable("node") String node
			, @PathVariable("upid") String upid) {
		
		ProxmoxTaskStatusDto statusInfo = pveService.getTaskStatus(node, upid);
		return ResponseEntity.ok(statusInfo);
	}
}
