package org.wso2.carbon.apimgt.api.doc.model;

public class Parameter {
	
	private String name;
	
	private String description;
	
	private String paramType;
	
	private boolean required;
	
	private boolean allowMultiple;
	
	private String dataType;
	
	public Parameter(String name, String description, String paramType, boolean required, boolean allowMultiple, String dataType) {
		this.name = name;
		this.description = description;
		this.paramType = paramType;
		this.required = required;
		this.allowMultiple = allowMultiple;
		this.dataType = dataType;		
	}

}
