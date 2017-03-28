package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.Policy;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.core.util.ETagUtils;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.TierListDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.TierMappingUtil;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;

@javax.annotation.Generated(value = "org.wso2.maven.plugins.JavaMSF4JServerCodegen", date = "2017-02-09T12:36:56.084+05:30")
public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Logger log = LoggerFactory.getLogger(PoliciesApiServiceImpl.class);

    /**
     * Retrieve a list of tiers for a particular tier level
     * 
     * @param tierLevel Tier level
     * @param limit maximum number of tiers to return
     * @param offset starting position of the pagination
     * @param accept accept header value
     * @param ifNoneMatch If-Non-Match header value
     * @param minorVersion minor version
     * @return A list of qualifying tiers
     * @throws NotFoundException
     */
    @Override
    public Response policiesTierLevelGet(String tierLevel, Integer limit, Integer offset, String accept,
                                         String ifNoneMatch, String minorVersion) throws NotFoundException {
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

    /**
     * Retrieves a tier by tier name and level
     * 
     * @param tierName Name of the tier
     * @param tierLevel Level of the tier
     * @param accept accept header value
     * @param ifNoneMatch If-Non-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion minor version
     * @return The requested tier as the response
     * @throws NotFoundException
     */
    @Override
    public Response policiesTierLevelTierNameGet(String tierName, String tierLevel, String accept, String ifNoneMatch,
                                                 String ifModifiedSince, String minorVersion) throws NotFoundException {
        TierDTO tierDTO = null;
        String username = RestApiUtil.getLoggedInUsername();
        try {
            APIStore apiStore = RestApiUtil.getConsumer(username);
            String existingFingerprint = policiesTierLevelTierNameGetFingerprint(tierName, tierLevel, accept,
                    ifNoneMatch, ifModifiedSince, minorVersion);
            if (!StringUtils.isEmpty(ifNoneMatch) && !StringUtils.isEmpty(existingFingerprint) && ifNoneMatch
                    .contains(existingFingerprint)) {
                return Response.notModified().build();
            }

            Policy tier = apiStore.getPolicy(tierLevel, tierName);
            tierDTO = TierMappingUtil.fromTierToDTO(tier, tierLevel);
            return Response.ok().entity(tierDTO)
                    .header(HttpHeaders.ETAG, "\"" + existingFingerprint + "\"")
                    .build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving tier";
            HashMap<String, String> paramList = new HashMap<String, String>();
            paramList.put(APIMgtConstants.ExceptionsConstants.TIER_LEVEL, tierLevel);
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }

    /**
     * Retrieves the fingerprint of a throttling policy given its UUID
     * 
     * @param policyName name of the policy
     * @param policyLevel level of the policy
     * @param accept accept header value
     * @param ifNoneMatch If-Non-Match header value
     * @param ifModifiedSince If-Modified-Since header value
     * @param minorVersion minor version
     * @return fingerprint of a throttling policy
     */
    public String policiesTierLevelTierNameGetFingerprint(String policyName, String policyLevel, String accept,
            String ifNoneMatch, String ifModifiedSince, String minorVersion) {
        String username = RestApiUtil.getLoggedInUsername();
        try {
            String lastUpdatedTime = RestApiUtil.getConsumer(username)
                    .getLastUpdatedTimeOfThrottlingPolicy(policyLevel, policyName);
            return ETagUtils.generateETag(lastUpdatedTime);
        } catch (APIManagementException e) {
            //gives a warning and let it continue the execution
            String errorMessage = "Error while retrieving last updated time of policy :" + policyLevel + "/" + policyName;
            log.error(errorMessage, e);
            return null;
        }
    }
}
