package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.DedicatedGatewayDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CompositeAPIDTO;
import java.io.File;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.FileInfoDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.CompositeAPIListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class CompositeApisApiService {
    public abstract Response compositeApisApiIdDedicatedGatewayGet(String apiId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response compositeApisApiIdDedicatedGatewayPut(String apiId,DedicatedGatewayDTO body,String ifMatch,String ifUnmodifiedSince);
    public abstract Response compositeApisApiIdDelete(String apiId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response compositeApisApiIdGet(String apiId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response compositeApisApiIdImplementationGet(String apiId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response compositeApisApiIdImplementationPut(String apiId,InputStream apiImplementationInputStream,Attachment apiImplementationDetail,String ifMatch,String ifUnmodifiedSince);
    public abstract Response compositeApisApiIdPut(String apiId,CompositeAPIDTO body,String ifMatch,String ifUnmodifiedSince);
    public abstract Response compositeApisApiIdSwaggerGet(String apiId,String ifNoneMatch,String ifModifiedSince);
    public abstract Response compositeApisApiIdSwaggerPut(String apiId,String apiDefinition,String ifMatch,String ifUnmodifiedSince);
    public abstract Response compositeApisGet(Integer limit,Integer offset,String query,String ifNoneMatch);
    public abstract Response compositeApisPost(CompositeAPIDTO body);
}

