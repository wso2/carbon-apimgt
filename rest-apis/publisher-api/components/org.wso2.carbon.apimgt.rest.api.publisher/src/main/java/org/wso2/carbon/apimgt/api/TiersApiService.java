package org.wso2.carbon.apimgt.api;

import org.wso2.carbon.apimgt.api.*;

import org.wso2.carbon.apimgt.model.Tier;
import org.wso2.carbon.apimgt.model.TierPermission;
import org.wso2.carbon.apimgt.model.Error;
import org.wso2.carbon.apimgt.model.TierList;

import java.util.List;

import java.io.InputStream;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class io.swagger.codegen.languages.JaxRSServerCodegen", date = "2016-10-14T14:19:24.818+05:30")
public abstract class TiersApiService {
      public abstract Response tiersUpdatePermissionPost(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince,TierPermission permissions);
      public abstract Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String accept,String ifNoneMatch);
      public abstract Response tiersTierLevelPost(Tier body,String tierLevel,String contentType);
      public abstract Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String accept,String ifNoneMatch,String ifModifiedSince);
      public abstract Response tiersTierLevelTierNamePut(String tierName,Tier body,String tierLevel,String contentType,String ifMatch,String ifUnmodifiedSince);
      public abstract Response tiersTierLevelTierNameDelete(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince);
}
