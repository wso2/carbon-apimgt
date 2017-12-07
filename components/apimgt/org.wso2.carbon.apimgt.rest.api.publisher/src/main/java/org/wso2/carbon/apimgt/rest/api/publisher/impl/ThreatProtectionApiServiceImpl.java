package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.publisher.ThreatProtectionApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {
    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

    /**
     * Get a list of threat protection policy ids associated to an API
     * @param apiId APIID
     * @param request
     * @return List of threat protection policy ids
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionApisApiIdPoliciesGet(String apiId, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            API api = apiPublisher.getAPIbyUUID(apiId);
            Set<String> policies = api.getThreatProtectionPolicies();
            if (policies == null) {
                policies = new HashSet<>();
            }
            return Response.status(200).entity(policies).build();
        } catch (APIManagementException e) {
            String errorMsg = "Error getting threat protection policies of API (ID: " + apiId + ").";
            return Response.status(500).entity(errorMsg).build();
        }
    }

    /**
     * Remove a threat protection policy from an API
     * @param apiId APIID
     * @param policyId Threat protection policy iD
     * @param request
     * @return HTTP status 200, 500 if failed to remove the policy
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionApisApiIdPoliciesPolicyIdDelete(String apiId, String policyId, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.deleteThreatProtectionPolicy(apiId, policyId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while deleting threat protection policy. API_ID: " + apiId +
                    ", POLICY_ID: " + policyId;
            log.error(errorMsg, e);
            return Response.status(500).entity(errorMsg).build();
        }
    }

    /**
     * Add a threat protection policy to an API
     * @param apiId APIID
     * @param policyId Threat protection policy ID
     * @param request
     * @return HTTP status 200, 500 if failed to add the policy
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionApisApiIdPoliciesPolicyIdPost(String apiId, String policyId, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.addThreatProtectionPolicy(apiId, policyId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while adding threat protection policy. API_ID: " + apiId +
                    ", POLICY_ID: " + policyId;
            log.error(errorMsg, e);
            return Response.status(500).entity(errorMsg).build();
        }
    }

    /**
     * Get a list of all threat protection policies
     * @param request
     * @return List of threat protection policies
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionPoliciesGet( Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<ThreatProtectionPolicy> list = apiPublisher.getThreatProtectionPolicies();
            return Response.ok().entity(list).build();
        } catch (APIManagementException e) {
            log.error("Error retrieving Threat Protection Policies", e);
        }
        return Response.status(500).build();
    }

    /**
     * Get a specific threat protection policy
     * @param policyId Threat protection policy ID
     * @param request
     * @return A threat protection policy
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionPoliciesPolicyIdGet(String policyId, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            ThreatProtectionPolicy policy = apiPublisher.getThreatProtectionPolicy(policyId);

            if (policy == null) {
                return Response.status(404).entity("No policy found for PolicyID: " + policyId).build();
            }

            ThreatProtectionPolicyDTO dto = MappingUtil.toThreatProtectionPolicyDTO(policy);
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            log.error("Error retrieving Threat Protection Policies", e);
        }
        return Response.status(500).build();
    }
}
