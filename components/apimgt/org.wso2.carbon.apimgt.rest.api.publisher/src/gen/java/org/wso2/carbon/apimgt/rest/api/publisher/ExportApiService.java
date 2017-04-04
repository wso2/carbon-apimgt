package org.wso2.carbon.apimgt.rest.api.publisher;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-04T15:48:35.633+05:30")
public abstract class ExportApiService {
    public abstract Response exportApisGet(String query
 ,String contentType
 ,Integer limit
 ,Integer offset
 ) throws NotFoundException;
}
