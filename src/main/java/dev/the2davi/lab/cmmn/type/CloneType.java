package dev.the2davi.lab.cmmn.type;

public enum CloneType {
	VM_FULL_CLONE("full-clone", "완전한 복제", 1)
	, VM_LINKED_CLONE("linked-clone", "링크된 복제", 0)
	, UNKNOWN("unknown", "알 수 없음", 1);
	
	private final String keyword;
	private final String name;
	private final Integer isFull;
	CloneType(String keyword, String name, Integer isFull) {
		this.keyword = keyword;
		this.name = name;
		this.isFull = isFull;
	}
	
	public String getKeyword() {
		return keyword;
	}
	public String getName() {
		return name;
	}
	public Integer getBinary() {
		return isFull;
	}
	
	public static Integer verify(Boolean isFull) {
		if(isFull == null) { return UNKNOWN.getBinary(); }
		return Boolean.TRUE.equals(isFull) ? VM_FULL_CLONE.getBinary() : VM_LINKED_CLONE.getBinary();
	}
}
