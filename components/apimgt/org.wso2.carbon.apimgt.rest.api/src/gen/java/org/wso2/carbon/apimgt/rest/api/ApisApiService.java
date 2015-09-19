package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.dto.DocumentDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class ApisApiService {
    public abstract Response apisGet(String limit,String offset,String query,String type,String sort,String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response apisPost(APIDTO body,String contentType)
    throws NotFoundException;
    public abstract Response apisChangeLifecyclePost(String newState,String publishToGateway,String resubscription,String apiId,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response apisCopyApiPost(String newVersion,String apiId)
    throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String contentType)
    throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response apisApiIdEnvironmentsGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response apisApiIdExternalStoresGet(String apiId,String limit,String offset,String query,String accept,String ifNoneMatch)
    throws NotFoundException;
}

