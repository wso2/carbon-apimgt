package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationKeyGenerateRequestDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ApplicationListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-04T10:24:30.459+05:30")
public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId
 ,ApplicationDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response applicationsGenerateKeysPost(String applicationId
 ,ApplicationKeyGenerateRequestDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response applicationsGet(String query
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 ) throws NotFoundException;
    public abstract Response applicationsPost(ApplicationDTO body
 ,String contentType
 ) throws NotFoundException;
}
