package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class ApisApiService {
    public abstract Response apisGet(Integer limit,Integer offset,String query,String type,String sort,String accept,String ifNoneMatch);
    public abstract Response apisPost(APIDTO body,String contentType);
    public abstract Response apisChangeLifecyclePost(String apiId,String newState,String publishToGateway,String resubscription,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisCopyApiPost(String apiId,String newVersion);
    public abstract Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
    public abstract Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String contentType);
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince);
}

