package org.wso2.carbon.apimgt.rest.api.admin;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;

import org.wso2.carbon.apimgt.rest.api.admin.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.TierPermissionDTO;

import java.util.List;

import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-08T15:53:45.426+05:30")
public abstract class PoliciesApiService {
    public abstract Response policiesTierLevelDelete(String tierName
            , String tierLevel
            , String ifMatch
            , String ifUnmodifiedSince
            , String minorVersion
    ) throws NotFoundException;

    public abstract Response policiesTierLevelGet(String tierLevel
            , Integer limit
            , Integer offset
            , String accept
            , String ifNoneMatch
            , String minorVersion
    ) throws NotFoundException;

    public abstract Response policiesTierLevelPost(TierDTO body
            , String tierLevel
            , String contentType
            , String minorVersion
    ) throws NotFoundException;

    public abstract Response policiesTierLevelPut(String tierName
            , TierDTO body
            , String tierLevel
            , String contentType
            , String ifMatch
            , String ifUnmodifiedSince
            , String minorVersion
    ) throws NotFoundException;

    public abstract Response policiesUpdatePermissionPost(String tierName
            , String tierLevel
            , String ifMatch
            , String ifUnmodifiedSince
            , String minorVersion
            , TierPermissionDTO permissions
    ) throws NotFoundException;
}
