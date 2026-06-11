package dev.the2davi.lab.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.dto.ProxmoxNodeDto;
import dev.the2davi.lab.api.dto.ProxmoxStorageDto;
import dev.the2davi.lab.api.service.ProxmoxService;
import dev.the2davi.lab.audit.service.AuditService;

@RestController
@RequestMapping("/api/proxmox")
public class ProxmoxController {

	private final ProxmoxService pveService;
	private final AuditService audit;
	
	public ProxmoxController(ProxmoxService pveService, AuditService audit) {
		this.pveService = pveService;
		this.audit = audit;
	}
	
	@GetMapping("/nodes")
	public ResponseEntity<List<ProxmoxNodeDto>> getNodes() {
		List<ProxmoxNodeDto> nodes = pveService.getClusterNodes();
		return ResponseEntity.ok(nodes);
	}
	

	

	
	/* Storage */
	@PostMapping("/storage")
	public ResponseEntity<String> addStorage(@RequestBody ProxmoxStorageDto dto) {
		pveService.createStorage(dto);
		return ResponseEntity.ok(dto.type() + " 스토리지가 성공적으로 추가되었습니다.");
	}

}














