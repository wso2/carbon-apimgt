package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.glassfish.jersey.media.multipart.FormDataContentDisposition;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierList;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Error;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.Tier;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermission;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-10-26T15:09:45.077+05:30")
public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel ,Integer limit ,Integer offset ,String accept ,String ifNoneMatch ) throws NotFoundException;
    public abstract Response tiersTierLevelPost(Tier body ,String tierLevel ,String contentType ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNameDelete(String tierName ,String tierLevel ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNameGet(String tierName ,String tierLevel ,String accept ,String ifNoneMatch ,String ifModifiedSince ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNamePut(String tierName ,Tier body ,String tierLevel ,String contentType ,String ifMatch ,String ifUnmodifiedSince ) throws NotFoundException;
    public abstract Response tiersUpdatePermissionPost(String tierName ,String tierLevel ,String ifMatch ,String ifUnmodifiedSince ,TierPermission permissions ) throws NotFoundException;
}
