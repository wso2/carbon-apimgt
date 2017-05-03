package org.wso2.carbon.apimgt.rest.api.store;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-07T10:04:16.863+05:30")
public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId
 ,ApplicationDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsGenerateKeysPost(String applicationId
 ,ApplicationKeyGenerateRequestDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response applicationsGet(String query
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response applicationsPost(ApplicationDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
