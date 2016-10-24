package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

//import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Document;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIList;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-24T10:47:36.442+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId ,String documentId ,String xWSO2Tenant ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId ,String documentId ,String xWSO2Tenant ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId ,Integer limit ,Integer offset ,String xWSO2Tenant ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId ,String accept ,String ifNoneMatch ,String ifModifiedSince ,String xWSO2Tenant ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId ,String accept ,String ifNoneMatch ,String ifModifiedSince ,String xWSO2Tenant ) throws NotFoundException;
    public abstract Response apisGet(Integer limit ,Integer offset ,String xWSO2Tenant ,String query ,String accept ,String ifNoneMatch ) throws NotFoundException;
}
