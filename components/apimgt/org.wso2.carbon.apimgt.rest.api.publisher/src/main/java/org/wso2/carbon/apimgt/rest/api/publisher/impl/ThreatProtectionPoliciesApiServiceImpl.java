package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;


import java.util.ArrayList;
import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.MappingUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ThreatProtectionPoliciesApiServiceImpl extends ThreatProtectionPoliciesApiService {
    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionPoliciesApiServiceImpl.class);

    /**
     * Get a list of all threat protection policies
     *
     * @param request
     * @return List of threat protection policies
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionPoliciesGet(Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            List<ThreatProtectionPolicy> list = apiPublisher.getThreatProtectionPolicies();
            List<ThreatProtectionPolicyDTO> dtoList = new ArrayList<>();
            for (ThreatProtectionPolicy policy: list) {
                dtoList.add(MappingUtil.toThreatProtectionPolicyDTO(policy));
            }
            return Response.ok().entity(dtoList).build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    /**
     * Get a specific threat protection policy
     *
     * @param policyId ID of the policy to be retrieved
     * @param request
     * @return Threat protection policy
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionPoliciesPolicyIdGet(String policyId, Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            ThreatProtectionPolicy policy = apiPublisher.getThreatProtectionPolicy(policyId);
            if (policy == null) {
                return Response.status(404).entity("Requested policy was not found.").build();
            }
            return Response.ok().entity(MappingUtil.toThreatProtectionPolicyDTO(policy)).build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();

    }
}
