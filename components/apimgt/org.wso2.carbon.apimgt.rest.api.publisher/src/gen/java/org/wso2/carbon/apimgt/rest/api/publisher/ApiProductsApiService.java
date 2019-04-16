package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDetailedDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ApiProductsApiService {
    public abstract Response apiProductsApiProductIdDelete(String apiProductId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response apiProductsApiProductIdGet(String apiProductId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response apiProductsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch,Boolean expand,String tenantDomain);
    public abstract Response apiProductsPost(APIProductDetailedDTO body,String contentType);
}

