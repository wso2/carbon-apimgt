package org.wso2.carbon.apimgt.gateway.dto;

public class APIData {
private String name;
	
	private String host;
	
	private int port = -1;
	
	private String context;
	
	private String fileName;
	
	private ResourceData[] resources;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getContext() {
		return context;
	}

	public void setContext(String context) {
		this.context = context;
	}

	public ResourceData[] getResources() {
		return resources;
	}

	public void setResources(ResourceData[] resources) {
		this.resources = resources;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
