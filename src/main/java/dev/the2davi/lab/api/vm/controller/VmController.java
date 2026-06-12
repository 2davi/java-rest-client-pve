package dev.the2davi.lab.api.vm.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.vm.dto.ProxmoxVmCloneDto;
import dev.the2davi.lab.api.vm.dto.ProxmoxVmDestroyDto;
import dev.the2davi.lab.api.vm.dto.ProxmoxVmDto;
import dev.the2davi.lab.api.vm.service.VmService;
import dev.the2davi.lab.cmmn.type.TaskType;
import dev.the2davi.lab.monitor.TaskMonitor;

@RestController
@RequestMapping("/api/proxmox")
public class VmController {
	private final VmService service;
	private final TaskMonitor audit;
	
	public VmController(VmService service, TaskMonitor audit) {
		this.service = service;
		this.audit = audit;
	}
	
	/* VM List */
	@GetMapping("/nodes/{node}/qemu")
	public ResponseEntity<List<ProxmoxVmDto>> getVmList(@PathVariable String node) {
		//return ResponseEntity.ok(service.getVmList(node));
		return ResponseEntity.ok(service.getVmList());
	}
	
	/* VM Control */
	@PostMapping("/nodes/{node}/qemu/{vmid}/status/{status}")
	public ResponseEntity<Map<String, String>> controlVm(
			@PathVariable String node
			, @PathVariable int vmid
			, @PathVariable String status) {
		String rawResponse = service.controlVmStatus(node, vmid, status);
		String upid = rawResponse.replace("{\"data\":\"", "").replace("\"}", "").trim();
		
		String[] seg = upid.split(":");
		String type = seg.length > 5 ? seg[5] : "";
		
		
		audit.auditTaskStatus(node, upid);
		return ResponseEntity.ok(Map.of(
				"message", TaskType.from(type).getDisplayName(),
				"upid", upid
		));
	}
	
	/* VM Clone */
	@PostMapping("/nodes/{node}/qemu/{vmid}/clone")
	public ResponseEntity<Map<String, String>> cloneVm(
			@PathVariable String node
			, @PathVariable int vmid
			, @RequestBody ProxmoxVmCloneDto dto) {
		String rawResponse = service.cloneVm(node, vmid, dto);
		String upid = rawResponse.replace("{\"data\":\"", "").replace("\"}", "").trim();
		
		audit.auditTaskStatus(node, upid);
		
		return ResponseEntity.ok(Map.of(
				"message", "VM 복제 작업 시작됨",
				"upid", upid
		));
	}
	
	/* VM Destroy*/
	@DeleteMapping("/nodes/{node}/qemu/{vmid}")
	public ResponseEntity<Map<String, String>> deleteVm(
			@PathVariable String node
			, @PathVariable int vmid
			, @RequestBody ProxmoxVmDestroyDto dto) {
		String rawResponse = service.deleteVm(node, vmid, dto);
		String upid = rawResponse.replace("{\"data\":\"", "").replace("\"}", "").trim();

		audit.auditTaskStatus(node, upid);
		
		return ResponseEntity.ok(Map.of(
				"message", "VM 삭제 작업 시작됨",
				"upid", upid
		));
	}
}
