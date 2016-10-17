package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApisApiService {
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
    public abstract Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant);
    public abstract Response apisApiIdSwaggerGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince,String xWSO2Tenant);
    public abstract Response apisApiIdThumbnailGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisGenerateSdkPost(String apiId,String language,String xWSO2Tenant);
    public abstract Response apisGet(Integer limit,Integer offset,String xWSO2Tenant,String query,String accept,String ifNoneMatch);
}

