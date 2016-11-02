package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 ) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response apisGet(Integer limit
 ,Integer offset
 ,String query
 ,String accept
 ,String ifNoneMatch
 ) throws NotFoundException;
}
