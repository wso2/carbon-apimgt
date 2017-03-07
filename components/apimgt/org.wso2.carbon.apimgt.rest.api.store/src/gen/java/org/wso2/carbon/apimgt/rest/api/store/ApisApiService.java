package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-28T14:58:05.886+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisGet(Integer limit
 ,Integer offset
 ,String query
 ,String accept
 ,String ifNoneMatch
 ,String minorVersion
 ) throws NotFoundException;
}
