package org.wso2.carbon.apimgt.rest.api.admin.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.admin.*;
import org.wso2.carbon.apimgt.rest.api.admin.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.admin.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.admin.mappings.ThreatProtectionMappingUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {
    private Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

    @Override
    public Response threatProtectionPoliciesGet( Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            List<ThreatProtectionPolicy> policyList = apiMgtAdminService.getThreatProtectionPolicyList();
            ThreatProtectionPolicyListDTO listDTO = new ThreatProtectionPolicyListDTO();
            for (ThreatProtectionPolicy policy: policyList) {
                listDTO.addListItem(ThreatProtectionMappingUtil.toThreatProtectionPolicyDTO(policy));
            }
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }
    @Override
    public Response threatProtectionPolicyPost(ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            apiMgtAdminService.addThreatProtectionPolicy(ThreatProtectionMappingUtil.toThreatProtectionPolicy(threatProtectionPolicy));
            return Response.ok().build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }
    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdDelete(String threatProtectionPolicyId
  ,Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            apiMgtAdminService.deleteThreatProtectionPolicy(threatProtectionPolicyId);
            return Response.ok().build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdGet(String threatProtectionPolicyId, Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            ThreatProtectionPolicyDTO dto = ThreatProtectionMappingUtil.toThreatProtectionPolicyDTO(
                    apiMgtAdminService.getThreatProtectionPolicy(threatProtectionPolicyId));
            return Response.ok().entity(dto).build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdPost(String threatProtectionPolicyId
, ThreatProtectionPolicyDTO threatProtectionPolicy
  ,Request request) throws NotFoundException {
        return threatProtectionPolicyPost(threatProtectionPolicy, request);
    }
}
