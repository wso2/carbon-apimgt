package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-03T16:10:24.266+05:30")
public abstract class CompositeApisApiService {
    public abstract Response compositeApisApiIdDelete(String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisApiIdGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisApiIdPut(String apiId
 ,APIDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerGet(String apiId
 ,String labelName
 ,String scheme
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerPut(String apiId
 ,String endpointId
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisGet(Integer limit
 ,Integer offset
 ,String query
 ,String accept
 ,String ifNoneMatch
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response compositeApisPost(APIDTO body
 ,String contentType
 ,String minorVersion
 ) throws NotFoundException;
}
