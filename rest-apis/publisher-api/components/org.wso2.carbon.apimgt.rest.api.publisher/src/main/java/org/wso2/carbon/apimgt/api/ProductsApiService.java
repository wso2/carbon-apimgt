package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.APIProductList;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.APIProduct;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public abstract class ProductsApiService {
      public abstract Response productsGet(Integer limit,Integer offset,String query,String accept,String ifNoneMatch);
      public abstract Response productsPost(APIProduct body,String contentType);
      public abstract Response productsChangeProductLifecyclePost(String action,String productId,String lifecycleChecklist,String ifMatch,String ifUnmodifiedSince);
      public abstract Response productsCopyProductPost(String newVersion,String productId);
      public abstract Response productsProductIdGet(String productId,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response productsProductIdPut(String productId,APIProduct body,String contentType,String ifMatch,String ifUnmodifiedSince);
      public abstract Response productsProductIdDelete(String productId,String ifMatch,String ifUnmodifiedSince);
}
