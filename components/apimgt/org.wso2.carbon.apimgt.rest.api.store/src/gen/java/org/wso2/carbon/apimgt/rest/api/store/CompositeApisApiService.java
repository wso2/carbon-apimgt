package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;

import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.CompositeAPIListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.dto.FileInfoDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public abstract class CompositeApisApiService {
    public abstract Response compositeApisApiIdDelete(String apiId
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdImplementationGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdImplementationPut(String apiId
 ,InputStream apiImplementationInputStream, FileInfo apiImplementationDetail
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdPut(String apiId
 ,CompositeAPIDTO body
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerGet(String apiId
 ,String ifNoneMatch
 ,String ifModifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisApiIdSwaggerPut(String apiId
 ,String apiDefinition
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response compositeApisGet(Integer limit
 ,Integer offset
 ,String query
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response compositeApisPost(CompositeAPIDTO body
 , Request request) throws NotFoundException;
}
