package org.wso2.carbon.apimgt.rest.api.store;

import org.wso2.carbon.apimgt.rest.api.store.*;
import org.wso2.carbon.apimgt.rest.api.store.dto.*;

import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;

import java.util.List;

import java.io.InputStream;


import javax.ws.rs.core.Response;

public abstract class TiersApiService {
    public abstract Response tiersGet(Integer limit,Integer offset,String accept,String ifNoneMatch);
    public abstract Response tiersTierNameGet(String tierName,String accept,String ifNoneMatch,String ifModifiedSince);
}

