package tech.tongyu.bct.auth.common.user;

import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component("loginMap")
public class LoginMap {
	private Map<String, LoginInfo> map = new HashMap<>();

	public Map<String, LoginInfo> getMap() {
		return map;
	}

	public void setMap(Map<String, LoginInfo> map) {
		this.map = map;
	}
}
