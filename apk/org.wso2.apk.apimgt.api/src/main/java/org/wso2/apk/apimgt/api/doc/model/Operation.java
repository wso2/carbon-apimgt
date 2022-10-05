package org.wso2.apk.apimgt.api.doc.model;

import java.util.List;

public class Operation {
	
	String httpMethod;
	
	String summary;
	
	String nickname;
	
	List<Parameter> parameters;
	
	public Operation(String httpMethod, String summary, String nickname, List<Parameter> parameters) {
		this.httpMethod = httpMethod;
		this.summary = summary;
		this.nickname = nickname;
		if (parameters != null) {
			this.parameters = parameters;
		}
	}
}
