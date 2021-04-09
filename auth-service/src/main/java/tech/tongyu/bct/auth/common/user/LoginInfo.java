package tech.tongyu.bct.auth.common.user;

public class LoginInfo {
	private String loginName;

	private Long latestLogin;

	private String hash;

	public LoginInfo(String loginName, Long latestLogin, String hash) {
		this.loginName = loginName;
		this.latestLogin = latestLogin;
		this.hash = hash;
	}

	public String getLoginName() {
		return loginName;
	}

	public void setLoginName(String loginName) {
		this.loginName = loginName;
	}

	public Long getLatestLogin() {
		return latestLogin;
	}

	public void setLatestLogin(Long latestLogin) {
		this.latestLogin = latestLogin;
	}

	public String getHash() {
		return hash;
	}

	public void setHash(String hash) {
		this.hash = hash;
	}

	@Override
	public String toString() {
		return "LoginInfo{" +
				"loginName='" + loginName + '\'' +
				", latestLogin=" + latestLogin +
				", hash='" + hash + '\'' +
				'}';
	}
}
