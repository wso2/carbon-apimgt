package org.wso2.carbon.apimgt.gateway.mcp;

import java.util.ArrayList;
import java.util.List;

public class SchemaMapping {

    private List<String> pathParams = new ArrayList<>();
    private List<Param> queryParams = new ArrayList<>();
    private List<Param> headerParams = new ArrayList<>();
    private boolean hasBody;
    private String contentType;

    public List<String> getPathParams() {
        return pathParams;
    }

    public void setPathParams(List pathParams) {
        this.pathParams = pathParams;
    }

    public List<Param> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(List<Param> queryParams) {
        this.queryParams = queryParams;
    }

    public List<Param> getHeaderParams() {
        return headerParams;
    }

    public void setHeaderParams(List<Param> headerParams) {
        this.headerParams = headerParams;
    }

    public boolean isHasBody() {
        return hasBody;
    }

    public void setHasBody(boolean hasBody) {
        this.hasBody = hasBody;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }
}
