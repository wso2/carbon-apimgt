package org.wso2.carbon.apimgt.impl.dto;

public class SoapToRestMediationDto {

    private String resource;
    private String method;
    private String content;

    public SoapToRestMediationDto(String resource, String method, String content) {

        this.resource = resource;
        this.method = method;
        this.content = content;
    }

    public SoapToRestMediationDto() {

    }

    public String getResource() {

        return resource;
    }

    public void setResource(String resource) {

        this.resource = resource;
    }

    public String getMethod() {

        return method;
    }

    public void setMethod(String method) {

        this.method = method;
    }

    public String getContent() {

        return content;
    }

    public void setContent(String content) {

        this.content = content;
    }
}
