package org.wso2.carbon.apimgt.rest.api;

import org.wso2.carbon.apimgt.rest.api.*;
import org.wso2.carbon.apimgt.rest.api.dto.*;

import org.wso2.carbon.apimgt.rest.api.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.dto.ErrorDTO;

import java.util.List;
import org.wso2.carbon.apimgt.rest.api.NotFoundException;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersGet(String accept,String ifNoneMatch)
    throws NotFoundException;
    public abstract Response tiersPost(TierDTO body,String contentType)
    throws NotFoundException;
    public abstract Response tiersTierNameGet(String tierName,String accept,String ifNoneMatch,String ifModifiedSince)
    throws NotFoundException;
    public abstract Response tiersTierNamePut(String tierName,TierDTO body,String contentType,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
    public abstract Response tiersTierNameDelete(String tierName,String ifMatch,String ifUnmodifiedSince)
    throws NotFoundException;
}

