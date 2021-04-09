package tech.tongyu.bct.auth.dto;

import org.apache.commons.lang3.StringUtils;

public class ClientMessage {

	private String name;

	private String ip;

	private String mac;

	private String hash;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getMac() {
		return mac;
	}

	public void setMac(String mac) {
		this.mac = mac;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	public static boolean isValid(ClientMessage clientMessage) {
		return clientMessage != null
				&& StringUtils.isNotBlank(clientMessage.name)
				&& StringUtils.isNotBlank(clientMessage.hash);
	}
}
