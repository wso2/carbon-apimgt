package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Application;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationKey;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationKeyGenerateRequest;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ApplicationList;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:47:36.442+05:30")
public abstract class ApplicationsApiService {
    public abstract Response applicationsApplicationIdDelete(String applicationId ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response applicationsApplicationIdGet(String applicationId ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response applicationsApplicationIdPut(String applicationId ,Application body ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response applicationsGenerateKeysPost(String applicationId ,ApplicationKeyGenerateRequest body ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response applicationsGet(String groupId ,String query ,Integer limit ,Integer offset ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response applicationsPost(Application body ,String contentType ) throws NotFoundException;
}
