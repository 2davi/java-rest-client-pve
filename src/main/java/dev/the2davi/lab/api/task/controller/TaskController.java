package dev.the2davi.lab.api.task.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import dev.the2davi.lab.api.dto.CmpTaskHistoryDto;
import dev.the2davi.lab.api.task.dto.ProxmoxTaskLogDto;
import dev.the2davi.lab.api.task.service.TaskService;

@RestController
@RequestMapping("/api/proxmox")
public class TaskController {
	private final TaskService service;
	
	public TaskController(TaskService service) {
		this.service = service;
	}
	
	/* Task List (Datacenter scope) */
	@GetMapping("/cluster/tasks")
	public ResponseEntity<List<CmpTaskHistoryDto>> getTaskList() {
		
		List<CmpTaskHistoryDto> tasks = service.getTaskList();
		return ResponseEntity.ok(tasks);
	}
	
	/* Log Trace */
	@GetMapping("/nodes/{node}/tasks/{upid}/log")
	public ResponseEntity<List<ProxmoxTaskLogDto>> getTaskLog(
			@PathVariable String node,
			@PathVariable String upid) {
		
		List<ProxmoxTaskLogDto> logs = service.getTaskLog(node, upid);
		return ResponseEntity.ok(logs);
	}
}
