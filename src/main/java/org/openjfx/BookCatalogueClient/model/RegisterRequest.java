package org.openjfx.BookCatalogueClient.model;

public class RegisterRequest {
	
	private String username;
	private String password;
	private String code;
	
	public RegisterRequest(String username, String password, String code) {
		this.username = username;
		this.password = password;
		this.code = code;
	}
	
	public RegisterRequest() {}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}
	
	
}
