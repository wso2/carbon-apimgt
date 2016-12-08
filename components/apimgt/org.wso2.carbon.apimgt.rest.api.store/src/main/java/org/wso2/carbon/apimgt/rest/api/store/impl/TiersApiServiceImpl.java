package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.TiersApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.TierMappingUtil;

import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "class org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2016-11-01T13:48:55.078+05:30")
public class TiersApiServiceImpl extends TiersApiService {

    private static final Logger log = LoggerFactory.getLogger(TiersApiServiceImpl.class);

    @Override public Response tiersTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
            String ifNoneMatch) throws NotFoundException {
        TierListDTO tierListDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            List<Policy> tierList = apiStore.getPolicies(tierLevel);
            tierListDTO = TierMappingUtil.fromTierListToDTO(tierList, tierLevel, limit, offset);
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tiers";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER_LEVEL, tierLevel);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        return Response.ok().entity(tierListDTO).build();
    }
    @Override
    public Response tiersTierLevelTierNameGet(String tierName
, String tierLevel
, String accept
, String ifNoneMatch
, String ifModifiedSince
 ) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
