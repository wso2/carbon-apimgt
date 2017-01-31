package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-01-19T14:57:15.952+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {
    @Override
    public Response policiesTierLevelDelete(String tierName, String tierLevel, String ifMatch, String ifUnmodifiedSince)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch) throws NotFoundException {

        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesTierLevelPost(TierDTO body, String tierLevel, String contentType) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesTierLevelPut(String tierName, TierDTO body, String tierLevel, String contentType,
                                         String ifMatch, String ifUnmodifiedSince) throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response policiesUpdatePermissionPost(String tierName, String tierLevel, String ifMatch,
                                                 String ifUnmodifiedSince, TierPermissionDTO permissions)
            throws NotFoundException {
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
