package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response tiersTierLevelPost(TierDTO body,String tierLevel,String contentType);
    public abstract Response tiersTierLevelTierNameDelete(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince);
    public abstract Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String accept,String ifNoneMatch,String ifModifiedSince);
    public abstract Response tiersTierLevelTierNamePut(String tierName,TierDTO body,String tierLevel,String contentType,String ifMatch,String ifUnmodifiedSince);
    public abstract Response tiersUpdatePermissionPost(String tierName,String tierLevel,String ifMatch,String ifUnmodifiedSince,TierPermissionDTO permissions);
}

