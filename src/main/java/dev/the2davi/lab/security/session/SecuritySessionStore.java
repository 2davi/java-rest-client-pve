package dev.the2davi.lab.security.session;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SecuritySessionStore {
	private final Map<String, SecuritySession> store = new ConcurrentHashMap<>();
	
	//세션을 등록할 때 Session ID 반환
	public String create(SecuritySession session) {
		String sid = UUID.randomUUID().toString();
		store.put(sid, session);
		return sid;
	}
	
	//Session ID를 이용해 세션 조회 (+ 만료 여부 판별하여...)
	public SecuritySession find(String sid) {
		if(sid == null) {
			return null;
		}
		SecuritySession session = store.get(sid);
		
		if(session == null) {
			return null;
		} if(session.isExpired()) {
			store.remove(sid);
			return null;
		} else {
			return session;
		}
	}
	
	//로그아웃 처리
	public void remove(String sid) {
		if(sid != null) store.remove(sid);
	}
}
