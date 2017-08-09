package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.store.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.APIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentListDTO;
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

public abstract class ApisApiService {
    public abstract Response apisApiIdCommentsCommentIdDelete(String commentId
 ,String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsCommentIdGet(String commentId
 ,String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsCommentIdPut(String commentId
 ,String apiId
 ,CommentDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsGet(String apiId
 ,Integer limit
 ,Integer offset
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsPost(String apiId
 ,CommentDTO body
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId
 ,String documentId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId
 ,String documentId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdRatingsGet(String apiId
 ,Integer limit
 ,Integer offset
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdRatingsRatingIdGet(String apiId
 ,String ratingId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdUserRatingPut(String apiId
 ,RatingDTO body
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdWsdlGet(String apiId
 ,String labelName
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisGet(Integer limit
 ,Integer offset
 ,String query
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
}
