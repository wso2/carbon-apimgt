package org.wso2.carbon.apimgt.rest.api.publisher;

import javax.ws.rs.core.Response;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-06T17:02:03.158+05:30")
public abstract class ExportApiService {
    public abstract Response exportApisGet(String query
 ,String contentType
 ,Integer limit
 ,Integer offset
 , Request request) throws NotFoundException;
}
