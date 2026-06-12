package dev.the2davi.lab.api.storage.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.dto.ProxmoxStorageDto;
import dev.the2davi.lab.api.storage.service.StorageService;
import dev.the2davi.lab.monitor.TaskMonitor;

@RestController
@RequestMapping("/api/proxmox")
public class StorageController {

	private static final Logger log = LoggerFactory.getLogger(StorageController.class);
	
	private final StorageService service;
	private final TaskMonitor monitor;
	
	private StorageController(StorageService service, TaskMonitor monitor) {
		this.service = service;
		this.monitor = monitor;
	}
	
	@GetMapping("/cluster/storage")
	public ResponseEntity<List<ProxmoxStorageDto>> getStorageList() {
		return ResponseEntity.ok(service.getStorageList());
	}
	
}
