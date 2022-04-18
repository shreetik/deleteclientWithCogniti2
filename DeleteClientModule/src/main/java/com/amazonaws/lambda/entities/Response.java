package com.amazonaws.lambda.entities;

import java.util.List;

public class Response {

	private String status;
	private String msg;
	private List<ClientData> clients;
	
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public List<ClientData> getClients() {
		return clients;
	}
	public void setClients(List<ClientData> clients) {
		this.clients = clients;
	}
	
	@Override
	public String toString() {
		return "Response [status=" + status + ", msg=" + msg + ", clients=" + clients + "]";
	}
	
	
}
