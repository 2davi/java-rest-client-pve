package dev.the2davi.lab.monitor;

import java.time.Duration;
import java.time.Instant;

import dev.the2davi.lab.cmmn.type.ResourceType;
import dev.the2davi.lab.cmmn.type.TaskAction;
import dev.the2davi.lab.cmmn.type.TaskType;

public record TaskOutcome(
		String upid
		, String node
		, TaskType taskType
		, String targetId
		, Status status
		, String exitStatus			// PVE exitstatus 원문 - 에러 메시지 보존
		, Instant startedAt
		, Instant finishedAt
) {

	public enum Status {
		SUCCEEDED					// stopped && exitstatus "OK"
		, FAILED					// stopped && "ERROR"
		, TIMED_OUT					// Watcher 감시 한도 내에 TASK 종료를 확인 못함 - 끝날 때까지 마냥 틀어놓을 수 없음
		, MONITOR_ERROR				// Watcher 자체가 뻗음
	}

	public long durationMs() {
		//작업 소요시간은 메서드로 파생값 취급
		return Duration.between(startedAt, finishedAt).toMillis();
	}
	
	public static TaskOutcome completed(String upid, String node, String exitStatus) {
		Status st = "OK".equals(exitStatus) ? Status.SUCCEEDED : Status.FAILED;
		return build(upid, node, st, exitStatus);
	}
	public static TaskOutcome timedOut(String upid, String node) {
		return build(upid, node, Status.TIMED_OUT, "monitor timeout");
	}
	public static TaskOutcome monitorFailed(String upid, String node, String reason) {
		return build(upid, node, Status.MONITOR_ERROR, reason);
	}
	
	//파생 접근자
	public ResourceType resourceType() { return taskType.resourceType(); }
	public TaskAction action()         { return taskType.action(); }
	
	//build() 메서드로 UPID 구조분해
	private static TaskOutcome build(String upid, String node, Status status, String exitStatus) {
		String[] seg = upid.split(":");
		String starttime = seg.length > 4 ? seg[4] : "0";
		long startedAt = Long.parseLong(starttime, 16);
		String type = seg.length > 5 ? seg[5] : "";
		String targetId = seg.length > 6 ? seg[6] : "";
		return new TaskOutcome(
				upid
				, node
				, TaskType.from(type)
				, targetId
				, status
				, exitStatus
				, Instant.ofEpochSecond(startedAt)
				, Instant.now()
		);
	}
	
	
}
