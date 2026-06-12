package dev.the2davi.lab.api.network.dto;

import java.util.List;
import java.util.Map;

/**
 * <pre>
 * 생성/수정 전용
 * </pre>
 *  
 * << 개정이력 >>
 *   
 *  수정일      수정자		수정내용
 *  ------------------------------------------------
 *  2026-06-12  kcy0122			최초 생성
 */
public record ProxmoxNetworkIfaceRequestDto(
		String iface
		, String type
		, Map<String, Object> config
		, List<String> delete
) {}
