package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-28T11:12:39.119+05:30")
public abstract class EndpointsApiService {
    public abstract Response endpointsEndpointIdDelete(String endpointId
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response endpointsEndpointIdGet(String endpointId
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response endpointsEndpointIdPut(String endpointId
 ,EndPointDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response endpointsGet(String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response endpointsPost(EndPointDTO body
 ,String contentType
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
}
