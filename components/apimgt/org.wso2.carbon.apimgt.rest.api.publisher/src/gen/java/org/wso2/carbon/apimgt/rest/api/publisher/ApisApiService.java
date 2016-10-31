package org.wso2.carbon.apimgt.rest.api.publisher;

<<<<<<< Updated upstream
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
=======
import org.apache.cxf.jaxrs.ext.multipart.Attachment;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.MediationDTO;
>>>>>>> Stashed changes

import javax.ws.rs.core.Response;
import java.io.InputStream;

public abstract class ApisApiService {
    public abstract Response apisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdContentPost(String apiId,String documentId,String contentType,InputStream fileInputStream,Attachment fileDetail,String inlineContent,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdDelete(String apiId,String documentId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId,String documentId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdDocumentsDocumentIdPut(String apiId,String documentId,DocumentDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdDocumentsGet(String apiId,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response apisApiIdDocumentsPost(String apiId,DocumentDTO body,String contentType);
    public abstract Response apisApiIdGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
<<<<<<< Updated upstream
=======
    public abstract Response apisApiIdPoliciesMediationGet(String apiId,Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
    public abstract Response apisApiIdPoliciesMediationMediationPolicyIdDelete(String apiId,String mediationPolicyId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdPoliciesMediationMediationPolicyIdGet(String apiId,String mediationPolicyId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdPoliciesMediationMediationPolicyIdPut(String apiId,String mediationPolicyId,MediationDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdPoliciesMediationPost(MediationDTO body,String apiId,String contentType,String ifMatch,String ifUnmodifiedSince);
>>>>>>> Stashed changes
    public abstract Response apisApiIdPut(String apiId,APIDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdSwaggerGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdSwaggerPut(String apiId,String apiDefinition,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdThumbnailGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdThumbnailPost(String apiId,InputStream fileInputStream,Attachment fileDetail,String contentType,String ifMatch,String ifUnmodifiedSince);
<<<<<<< Updated upstream
=======
    public abstract Response apisApiIdWsdlGet(String apiId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apisApiIdWsdlPost(String apiId,String wsdlDefinision,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisApiIdWsdlPut(String apiId,String wsdlDefinision,String contentType,String ifMatch,String ifUnmodifiedSince);
>>>>>>> Stashed changes
    public abstract Response apisChangeLifecyclePost(String action,String apiId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apisCopyApiPost(String newVersion,String apiId);
    public abstract Response apisGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
    public abstract Response apisPost(APIDTO body,String contentType);
}

