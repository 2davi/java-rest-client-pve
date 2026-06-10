package dev.the2davi.lab.cmmn.type;

public enum ResourceType {
	VM("qm", "VM")
	, CT("vz", "CT")
	, NODE("", "NODE");
	
	private final String prefix;
	private final String displayName;
	
	ResourceType(String prefix, String displayName){
		this.prefix = prefix;
		this.displayName = displayName;
	}
	
	public String getDisplayName() {
		return displayName;
	}
	
	public static ResourceType fromTaskType(String taskType) {
		if(taskType == null) return NODE;
		if(taskType.startsWith(VM.prefix)) return VM;
		if(taskType.startsWith(CT.prefix)) return CT;
		return NODE;
	}
}
