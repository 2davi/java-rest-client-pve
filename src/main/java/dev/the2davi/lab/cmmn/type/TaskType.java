package dev.the2davi.lab.cmmn.type;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public enum TaskType {
	// ── VM (QEMU) : worker type = qm* ───────────────────────────────
	QM_START   ("qmstart",    ResourceType.VM,        TaskAction.START),
	QM_STOP    ("qmstop",     ResourceType.VM,        TaskAction.STOP),
	QM_SHUTDOWN("qmshutdown", ResourceType.VM,        TaskAction.SHUTDOWN),
	QM_REBOOT  ("qmreboot",   ResourceType.VM,        TaskAction.REBOOT),
	QM_RESET   ("qmreset",    ResourceType.VM,        TaskAction.RESET),
	QM_SUSPEND ("qmsuspend",  ResourceType.VM,        TaskAction.SUSPEND),
	QM_RESUME  ("qmresume",   ResourceType.VM,        TaskAction.RESUME),
	QM_CREATE  ("qmcreate",   ResourceType.VM,        TaskAction.CREATE),
	QM_DESTROY ("qmdestroy",  ResourceType.VM,        TaskAction.DESTROY),
	QM_CLONE   ("qmclone",    ResourceType.VM,        TaskAction.CLONE),
	QM_MIGRATE ("qmigrate",   ResourceType.VM,        TaskAction.MIGRATE),   // ★ qmigrate (qmmigrate 아님)
	QM_MOVE    ("qmmove",     ResourceType.VM,        TaskAction.MOVE),
	QM_TEMPLATE("qmtemplate", ResourceType.VM,        TaskAction.TEMPLATE),
	QM_SNAPSHOT("qmsnapshot", ResourceType.VM,        TaskAction.SNAPSHOT),
	QM_ROLLBACK("qmrollback", ResourceType.VM,        TaskAction.ROLLBACK),
	QM_RESTORE ("qmrestore",  ResourceType.VM,        TaskAction.RESTORE),
	
	// ── 컨테이너 (LXC) : worker type = vz* (예외 있음) ────────────────
	VZ_START   ("vzstart",    ResourceType.CONTAINER, TaskAction.START),
	VZ_STOP    ("vzstop",     ResourceType.CONTAINER, TaskAction.STOP),
	VZ_SHUTDOWN("vzshutdown", ResourceType.CONTAINER, TaskAction.SHUTDOWN),
	VZ_REBOOT  ("vzreboot",   ResourceType.CONTAINER, TaskAction.REBOOT),
	VZ_SUSPEND ("vzsuspend",  ResourceType.CONTAINER, TaskAction.SUSPEND),
	VZ_RESUME  ("vzresume",   ResourceType.CONTAINER, TaskAction.RESUME),
	VZ_CREATE  ("vzcreate",   ResourceType.CONTAINER, TaskAction.CREATE),
	VZ_DESTROY ("vzdestroy",  ResourceType.CONTAINER, TaskAction.DESTROY),
	CT_MOVE_VOLUME("move_volume", ResourceType.CONTAINER, TaskAction.MOVE), // vz 접두사 안 따르는 확인된 예외
	
	// ── 백업 / 노드 레벨 ────────────────────────────────────────────
	VZDUMP     ("vzdump",     ResourceType.BACKUP,    TaskAction.BACKUP),
	APT_UPDATE ("aptupdate",  ResourceType.NODE,      TaskAction.UPDATE),
	START_ALL  ("startall",   ResourceType.NODE,      TaskAction.START),
	STOP_ALL   ("stopall",    ResourceType.NODE,      TaskAction.STOP),
	
	// ── 미열거/미지원 ───────────────────────────────────────────────
	VNC_PROXY  ("vncproxy",   ResourceType.VM,        TaskAction.VNC),
	UNKNOWN    ("",           ResourceType.UNKNOWN,   TaskAction.UNKNOWN);
	
	private final String pveType;
	private final ResourceType resourceType;
	private final TaskAction action;
	
	TaskType(String pveType, ResourceType resourceType, TaskAction action) {
		this.pveType = pveType;
		this.resourceType = resourceType;
		this.action = action;
	}
	public String pveType()            { return pveType; }
	public ResourceType resourceType() { return resourceType; }
	public TaskAction action()         { return action; }
	public String getDisplayName() {
		if(this == UNKNOWN) return "알 수 없음";
		return String.format("%s %s", resourceType.getName(), action.getName());
	}
	
	private static final Map<String, TaskType> BY_PVE_TYPE
			= Arrays.stream(values())
					.filter(t -> !t.pveType.isEmpty())
					.collect(Collectors.toUnmodifiableMap(TaskType::pveType, t -> t)
	);
	
	
	/**
	 * <pre>
	 * pve worker type 문자열을 TaskType으로 해석.
	 * 카탈로그에 없거나 null일 시 UNKNOWN으로 해석 ─ 모르는 작업이라도 이력 남기기
	 * </pre>
	 * @param pveType
	 * @return
	 * @author kcy0122
	 * @since 2026-06-11
	 */
	public static TaskType from(String pveType) {
		if(pveType == null) return UNKNOWN;
		return BY_PVE_TYPE.getOrDefault(pveType, UNKNOWN);
	}
}
