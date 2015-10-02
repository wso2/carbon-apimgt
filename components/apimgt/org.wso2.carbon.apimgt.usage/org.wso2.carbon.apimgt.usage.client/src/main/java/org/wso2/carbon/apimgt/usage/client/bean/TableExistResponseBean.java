package org.wso2.carbon.apimgt.usage.client.bean;

public class TableExistResponseBean {

	String status;
	String message;
	public TableExistResponseBean(String status, String message) {
		super();
		this.status = status;
		this.message = message;
	}
	public String getStatus() {
		return status;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}	
}
