package org.wso2.carbon.apimgt.api.doc.model;

import java.util.List;

public class APIDefinition {
	
	private String apiVersion;
	
	private String swaggerVersion;
	
	private String basePath;
	
	private String resourcePath;
	
	private List<APIResource> apis;
	
	public APIDefinition(String apiVersion, String swaggerVersion, String basePath, String resourcePath, List<APIResource> apis) {
		this.apiVersion = apiVersion;
		this.swaggerVersion = swaggerVersion;
		this.basePath = basePath;
		this.resourcePath = resourcePath;
		this.apis = apis;
	}

}
