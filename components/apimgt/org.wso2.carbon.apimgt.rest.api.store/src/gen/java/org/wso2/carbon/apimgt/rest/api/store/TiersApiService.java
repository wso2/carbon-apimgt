package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierPermissionDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersGet(String accept,String ifNoneMatch);
    public abstract Response tiersPost(TierDTO body,String contentType);
    public abstract Response tiersTierNameGet(String tierName,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response tiersTierNamePut(String tierName,TierDTO body,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response tiersTierNameDelete(String tierName,String ifMatch,String ifUnmodifiedSince);
    public abstract Response tiersTierNameUpdatePermissionPost(String tierName,TierPermissionDTO permissions,String contentType,String ifMatch,String ifUnmodifiedSince);
}

