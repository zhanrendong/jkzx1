package tech.tongyu.bct.auth.dto;

public class ServerMessage {

	private String code;

	private String responseMessage;

	private String userName;

	private String addIn;

	private Long latestLogin;

	public ServerMessage(String code, String responseMessage, String userName, String addIn, Long latestLogin) {
		this.code = code;
		this.responseMessage = responseMessage;
		this.userName = userName;
		this.addIn = addIn;
		this.latestLogin = latestLogin;
	}

	public ServerMessage(String code, String responseMessage) {
		this.code = code;
		this.responseMessage = responseMessage;
	}

	public String getResponseMessage() {
		return responseMessage;
	}

	public void setResponseMessage(String responseMessage) {
		this.responseMessage = responseMessage;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getAddIn() {
		return addIn;
	}

	public void setAddIn(String addIn) {
		this.addIn = addIn;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public Long getLatestLogin() {
		return latestLogin;
	}

	public void setLatestLogin(Long latestLogin) {
		this.latestLogin = latestLogin;
	}
}
