package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIPublisher;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.*;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.publisher.NotFoundException;

import java.io.InputStream;
import java.util.Set;

import org.wso2.carbon.apimgt.rest.api.publisher.utils.RestAPIPublisherUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {
    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

    @Override
    public Response threatProtectionAddPolicyApiIdPolicyIdPost(String apiId
            , String threatProtectionPolicyId
            ,Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.addThreatProtectionPolicy(apiId, threatProtectionPolicyId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while adding threat protection policy. API_ID: " + apiId +
                    ", POLICY_ID: " + threatProtectionPolicyId;
            log.error(errorMsg, e);
            return Response.status(500).entity(errorMsg).build();
        }
    }
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
    @Override
    public Response threatProtectionPolicyApiIdGet(String apiId
            ,Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            API api = apiPublisher.getAPIbyUUID(apiId);
            Set<String> policies = api.getThreatProtectionPolicies();
            return Response.status(200).entity(policies).build();
        } catch (APIManagementException e) {
            String errorMsg = "Error getting threat protection policies of API (ID: " + apiId + ").";
            return Response.status(500).entity(errorMsg).build();
        }

    }
    @Override
    public Response threatProtectionRemovePolicyApiIdPolicyIdPost(String apiId
            , String threatProtectionPolicyId
            ,Request request) throws NotFoundException {
        String username = RestApiUtil.getLoggedInUsername(request);
        try {
            APIPublisher apiPublisher = RestAPIPublisherUtil.getApiPublisher(username);
            apiPublisher.deleteThreatProtectionPolicy(apiId, threatProtectionPolicyId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            String errorMsg = "Error while deleting threat protection policy. API_ID: " + apiId +
                    ", POLICY_ID: " + threatProtectionPolicyId;
            log.error(errorMsg, e);
            return Response.status(500).entity(errorMsg).build();
        }
    }
}
