package org.wso2.carbon.apimgt.rest.api.admin;

import javax.ws.rs.core.Response;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierPermissionDTO;
import org.wso2.msf4j.Request;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-04-07T14:13:41.057+05:30")
public abstract class PoliciesApiService {
    public abstract Response policiesTierLevelTierLevelGet(String tierLevel
 ,Integer limit
 ,Integer offset
 ,String accept
 ,String ifNoneMatch
 , Request request) throws NotFoundException;
    public abstract Response policiesTierLevelTierLevelPost(TierDTO body
 ,String tierLevel
 ,String contentType
 , Request request) throws NotFoundException;
    public abstract Response policiesTierLevelTierLevelTierNameTierNameDelete(String tierName
 ,String tierLevel
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesTierLevelTierLevelTierNameTierNamePut(String tierName
 ,TierDTO body
 ,String tierLevel
 ,String contentType
 ,String ifMatch
 ,String ifUnmodifiedSince
 , Request request) throws NotFoundException;
    public abstract Response policiesUpdatePermissionPost(String tierName
 ,String tierLevel
 ,String ifMatch
 ,String ifUnmodifiedSince
 ,TierPermissionDTO permissions
 , Request request) throws NotFoundException;
}
