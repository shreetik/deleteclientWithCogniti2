package com.amazonaws.lambda.entities;

public class Request {

	private String httpMethod;
	private String email;
	
	
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
	
	@Override
	public String toString() {
		return "Request [httpMethod=" + httpMethod + ", email=" + email + "]";
	}
	
	
}
