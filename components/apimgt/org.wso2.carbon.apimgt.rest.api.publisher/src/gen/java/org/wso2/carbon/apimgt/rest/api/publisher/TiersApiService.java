package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:47:43.416+05:30")
public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 ) throws NotFoundException;
    public abstract Response tiersTierLevelPost(TierDTO body
 ,String tierLevel
 ,String contentType
 ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNameDelete(String tierName
 ,String tierLevel
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNameGet(String tierName
 ,String tierLevel
 ,String accept
 ,String ifNoneMatch
 ,String ifModifiedSince
 ) throws NotFoundException;
    public abstract Response tiersTierLevelTierNamePut(String tierName
 ,TierDTO body
 ,String tierLevel
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 ) throws NotFoundException;
    public abstract Response tiersUpdatePermissionPost(String tierName
 ,String tierLevel
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,TierPermissionDTO permissions
 ) throws NotFoundException;
}
