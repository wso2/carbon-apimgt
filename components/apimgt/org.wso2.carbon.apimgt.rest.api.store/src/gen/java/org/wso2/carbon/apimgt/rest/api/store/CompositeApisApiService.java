package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-05-05T10:43:39.597+05:30")
public abstract class CompositeApisApiService {
    public abstract Response compositeApisApiIdDelete(String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdPut(String apiId
 ,APIDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerPut(String apiId
 ,String endpointId
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisGet(Integer limit
 ,Integer offset
 ,String query
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response compositeApisPost(APIDTO body
 ,String contentType
 , Request request) throws NotFoundException;
}
