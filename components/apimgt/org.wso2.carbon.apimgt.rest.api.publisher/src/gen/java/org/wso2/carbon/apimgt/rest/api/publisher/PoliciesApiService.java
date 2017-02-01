package org.wso2.carbon.apimgt.rest.api.publisher;

import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.TierPermissionDTO;

import javax.ws.rs.core.Response;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-03T20:31:12.997+05:30")
public abstract class PoliciesApiService {
    public abstract Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                                  String ifNoneMatch) throws NotFoundException;

    public abstract Response policiesTierLevelPost(TierDTO body, String tierLevel, String contentType)
            throws NotFoundException;

    public abstract Response policiesTierLevelTierNameDelete(String tierName, String tierLevel, String ifMatch,
                                                             String ifUnmodifiedSince) throws NotFoundException;

    public abstract Response policiesTierLevelTierNameGet(String tierName, String tierLevel, String accept,
                                                          String ifNoneMatch, String ifModifiedSince)
            throws NotFoundException;

    public abstract Response policiesTierLevelTierNamePut(String tierName, TierDTO body, String tierLevel,
                                                          String contentType, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException;

    public abstract Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
                                                          String ifUnmodifiedSince, TierPermissionDTO permissions)
            throws NotFoundException;
}
