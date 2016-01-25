package org.wso2.carbon.apimgt.gateway.dto;

import org.apache.synapse.rest.RESTConstants;

public class ResourceData {
	
private String[] methods = new String[4];
	
	private String contentType;
	
	private String userAgent;
	
	private int protocol = RESTConstants.PROTOCOL_HTTP_AND_HTTPS;
	
	private String inSequenceKey;
	
	private String outSequenceKey;
	
	private String faultSequenceKey;
	
	private String uriTemplate;
	
	private String urlMapping;

    private String inSeqXml;

    private String outSeqXml;

    private String faultSeqXml;

	public String[] getMethods() {
		String[] copy = new String[methods.length];
		System.arraycopy(methods, 0, copy, 0, methods.length);
		return copy;
	}

	public void setMethods(String[] methods) {
		String[] copy = new String[methods.length];
		System.arraycopy(methods, 0, copy, 0, methods.length);
		this.methods = copy;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public int getProtocol() {
		return protocol;
	}

	public void setProtocol(int protocol) {
		this.protocol = protocol;
	}

	public String getInSequenceKey() {
		return inSequenceKey;
	}

	public void setInSequenceKey(String inSequenceKey) {
		this.inSequenceKey = inSequenceKey;
	}

	public String getOutSequenceKey() {
		return outSequenceKey;
	}

	public void setOutSequenceKey(String outSequenceKey) {
		this.outSequenceKey = outSequenceKey;
	}

	public String getFaultSequenceKey() {
		return faultSequenceKey;
	}

	public void setFaultSequenceKey(String faultSequenceKey) {
		this.faultSequenceKey = faultSequenceKey;
	}
	
	public String getUriTemplate() {
		return uriTemplate;
	}

	public void setUriTemplate(String uriTemplate) {
		this.uriTemplate = uriTemplate;
	}

	public String getUrlMapping() {
		return urlMapping;
	}

	public void setUrlMapping(String urlMapping) {
		this.urlMapping = urlMapping;
	}

    public String getInSeqXml() {
        return inSeqXml;
    }

    public void setInSeqXml(String inSeqXml) {
        if(inSeqXml == null){
            return;
        }
        this.inSeqXml = inSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }

    public String getOutSeqXml() {
        return outSeqXml;
    }

    public void setOutSeqXml(String outSeqXml) {
        if(outSeqXml == null){
            return;
        }
        this.outSeqXml = outSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }

    public String getFaultSeqXml() {
        return faultSeqXml;
    }

    public void setFaultSeqXml(String faultSeqXml) {
    	if(faultSeqXml == null){
    		return;
    	}
        this.faultSeqXml = faultSeqXml.trim().replaceAll("&gt", ">").replaceAll("&lt", "<")
                .replaceAll("\n", "").replaceAll("\t", " ");
    }

}
