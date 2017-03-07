package org.wso2.carbon.apimgt.rest.api.publisher;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T18:57:34.679+05:30") public abstract class ExportApiService {
    public abstract Response exportApisGet(String contentType, String query, Integer limit, Integer offset)
            throws NotFoundException;
}
