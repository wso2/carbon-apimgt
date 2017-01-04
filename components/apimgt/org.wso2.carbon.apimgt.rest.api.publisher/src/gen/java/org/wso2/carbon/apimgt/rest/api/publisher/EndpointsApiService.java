package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.EndPointListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public abstract class EndpointsApiService {
    public abstract Response endpointsEndpointIdDelete(String endpointId
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response endpointsEndpointIdGet(String endpointId
 ) throws NotFoundException;
    public abstract Response endpointsEndpointIdPut(String endpointId
 ,EndPointDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response endpointsGet() throws NotFoundException;
    public abstract Response endpointsPost(EndPointDTO body
 ,String contentType
 ) throws NotFoundException;
}
