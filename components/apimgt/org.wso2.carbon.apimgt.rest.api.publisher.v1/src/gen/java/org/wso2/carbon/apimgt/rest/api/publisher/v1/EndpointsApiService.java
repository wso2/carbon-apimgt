package org.wso2.carbon.apimgt.rest.api.publisher.v1;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.EndPointListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class EndpointsApiService {
    public abstract Response endpointsEndpointIdDelete(String endpointId,String ifMatch);
    public abstract Response endpointsEndpointIdGet(String endpointId,String ifMatch);
    public abstract Response endpointsEndpointIdPut(String endpointId,EndPointDTO body,String ifMatch);
    public abstract Response endpointsGet(String ifNoneMatch);
    public abstract Response endpointsHead(String name,String ifNoneMatch);
    public abstract Response endpointsPost(EndPointDTO body,String ifNoneMatch);
}

