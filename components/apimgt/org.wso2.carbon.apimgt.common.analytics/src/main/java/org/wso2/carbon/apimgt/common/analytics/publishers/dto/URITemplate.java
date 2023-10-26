package org.wso2.carbon.apimgt.common.analytics.publishers.dto;

import java.util.ArrayList;
import java.util.List;

/**
 * URITemplate attribute in analytics event.
 */
public class URITemplate {
    private String uriTemplate;
    private String resourceURI;
    private String resourceSandboxURI;
    private String httpVerb;

    private String authScheme;
    private List<OperationPolicy> operationPolicies = new ArrayList<>();

    public void setOperationPolicies(List<OperationPolicy> operationPolicies) {
        this.operationPolicies = operationPolicies;
    }

    public List<OperationPolicy> getOperationPolicies() {
        return operationPolicies;
    }

    public void setHttpVerb(String httpVerb) {
        this.httpVerb = httpVerb;
    }

    public String getHttpVerb() {
        return httpVerb;
    }

    public void setUriTemplate(String uriTemplate) {
        this.uriTemplate = uriTemplate;
    }

    public String getUriTemplate() {
        return uriTemplate;
    }

    public void setResourceURI(String resourceURI) {
        this.resourceURI = resourceURI;
    }

    public String getResourceURI() {
        return resourceURI;
    }

    public void setResourceSandboxURI(String resourceSandboxURI) {
        this.resourceSandboxURI = resourceSandboxURI;
    }

    public String getResourceSandboxURI() {
        return resourceSandboxURI;
    }

    public void setAuthScheme(String authScheme) {
        this.authScheme = authScheme;
    }

    public String getAuthScheme() {
        return authScheme;
    }
}
