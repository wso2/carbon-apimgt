package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.DocumentListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingListDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-03-29T23:13:38.810+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdCommentsCommentIdDelete(String commentId
 ,String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdCommentsCommentIdGet(String commentId
 ,String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdCommentsPost(String apiId
 ,CommentDTO body
 ,String contentType
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdCommentsPut(String commentId
 ,String apiId
 ,CommentDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,String minorVersion
 ) throws NotFoundException;
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
    public abstract Response apisApiIdRatingGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdRatingPost(String apiId
 ,RatingDTO body
 ,String contentType
 ,String minorVersion
 ) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String labelName
 ,String scheme
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
