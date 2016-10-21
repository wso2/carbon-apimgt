package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIProductDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class ProductsApiService {
    public abstract Response productsChangeProductLifecyclePost(String action,String productId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince);
    public abstract Response productsCopyProductPost(String newVersion,String productId);
    public abstract Response productsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
    public abstract Response productsPost(APIProductDTO body,String contentType);
    public abstract Response productsProductIdDelete(String productId,String ifMatch,String ifUnmodifiedSince);
    public abstract Response productsProductIdGet(String productId,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response productsProductIdPut(String productId,APIProductDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
}

