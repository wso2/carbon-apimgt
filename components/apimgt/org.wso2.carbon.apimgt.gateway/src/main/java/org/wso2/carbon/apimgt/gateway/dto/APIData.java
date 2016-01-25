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
		ResourceData[] copy = new ResourceData[resources.length];
		System.arraycopy(resources, 0, copy, 0, resources.length);
		return copy;
	}

	public void setResources(ResourceData[] resources) {
		ResourceData[] copy = new ResourceData[resources.length];
		System.arraycopy(resources, 0, copy, 0, resources.length);
		this.resources = copy;
	}
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
}
