package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.dto.TierPermissionDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class UpdateTierPermissionApiService {
    public abstract Response updateTierPermissionPost(String tierName,TierPermissionDTO permissions,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
}

