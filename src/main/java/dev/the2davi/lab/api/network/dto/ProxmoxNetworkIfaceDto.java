package dev.the2davi.lab.api.network.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <pre>
 * 읽기 전용
 * </pre>
 *  
 * << 개정이력 >>
 *   
 *  수정일      수정자		수정내용
 *  ------------------------------------------------
 *  2026-06-12  kcy0122			최초 생성
 */
public record ProxmoxNetworkIfaceDto(
		String iface
		, String node
		, String type
		, String address
		, String address6
		, Boolean autostart
		, @JsonProperty("bond-primary")
		  String bondPrimary
		, @JsonProperty("bond_mode")
		  String bondMode
		, @JsonProperty("bond_xmit_hash_policy")
		  String bondXmitHashPolicy
		, @JsonProperty("bridge_ports")
		  String bridgePorts
		, @JsonProperty("bridge_vids")
		  String bridgeVids
		, @JsonProperty("bridge_vlan_aware")
		  String bridgeVlanAware
		, String cidr
		, String cidr6
		, String comments
		, String comments6
		, String delete
		, String gateway
		, String gateway6
		, Integer mtu
		, String netmask
		, Integer netmask6
		, @JsonProperty("ovs_bonds")
		  String ovsBonds
		, @JsonProperty("ovs_bridge")
		  String ovsBridge
		, @JsonProperty("ovs_ports")
		  String ovsPorts
		, @JsonProperty("ove_tags")
		  String ovsTags
		, String slaves
		, @JsonProperty("vlan-id")
		  Integer vlanId
		, @JsonProperty("vlan-raw-device")
		  String vlanRawDevice
) {}
