package org.wso2.carbon.apimgt.rest.api.store.v1;

import org.wso2.carbon.apimgt.rest.api.store.v1.*;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.v1.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.TierDTO;

import java.util.List;

import java.io.InputStream;
import org.apache.cxf.jaxrs.ext.multipart.Attachment;

import javax.ws.rs.core.Response;

public abstract class PoliciesApiService {
    public abstract Response policiesTierLevelGet(String tierLevel,Integer limit,Integer offset,String ifNoneMatch);
    public abstract Response policiesTierLevelTierNameGet(String tierName,String tierLevel,String ifNoneMatch,String ifModifiedSince);
}

