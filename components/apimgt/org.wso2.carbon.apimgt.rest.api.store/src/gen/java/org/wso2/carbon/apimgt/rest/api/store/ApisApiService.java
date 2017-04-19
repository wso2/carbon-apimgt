package org.wso2.carbon.apimgt.rest.api.store;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.store.dto.CommentDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.RatingDTO;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-07T10:04:16.863+05:30")
public abstract class ApisApiService {
    public abstract Response apisApiIdCommentsCommentIdDelete(String commentId
 ,String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsCommentIdGet(String commentId
 ,String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsPost(String apiId
 ,CommentDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdCommentsPut(String commentId
 ,String apiId
 ,CommentDTO body
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdContentGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsDocumentIdGet(String apiId
 ,String documentId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdDocumentsGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdGet(String apiId
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdRatingGet(String apiId
 ,Integer limit
 ,Integer offset
 ,String accept
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdRatingPost(String apiId
 ,RatingDTO body
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response apisApiIdSwaggerGet(String apiId
 ,String labelName
 ,String scheme
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response apisGet(Integer limit
 ,Integer offset
 ,String query
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
}
