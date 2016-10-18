package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersTierLevelGet(String tierLevel,Integer limit,Integer offset,String xWSO2Tenant,String accept,String ifNoneMatch);
    public abstract Response tiersTierLevelTierNameGet(String tierName,String tierLevel,String xWSO2Tenant,String accept,String ifNoneMatch,String ifModifiedSince);
}

