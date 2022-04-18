package com.amazonaws.lambda.entities;

public class Request {

	private String httpMethod;
	private String email;
	private String client_email;
	private String confirmation_code;
	
	public String getHttpMethod() {
		return httpMethod;
	}
	public void setHttpMethod(String httpMethod) {
		this.httpMethod = httpMethod;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	
	public String getClient_email() {
		return client_email;
	}
	public void setClient_email(String client_email) {
		this.client_email = client_email;
	}
	
	public String getConfirmation_code() {
		return confirmation_code;
	}
	public void setConfirmation_code(String confirmation_code) {
		this.confirmation_code = confirmation_code;
	}
	
	@Override
	public String toString() {
		return "Request [httpMethod=" + httpMethod + ", email=" + email + ", client_email=" + client_email + "]";
	}
	
	
}
