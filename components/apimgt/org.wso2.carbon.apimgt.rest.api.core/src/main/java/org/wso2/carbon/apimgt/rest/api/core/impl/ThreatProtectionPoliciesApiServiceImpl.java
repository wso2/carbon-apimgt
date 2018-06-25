package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.core.*;
import org.wso2.carbon.apimgt.rest.api.core.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;

public class ThreatProtectionPoliciesApiServiceImpl extends ThreatProtectionPoliciesApiService {

    private APIMgtAdminService apiMgtAdminService;

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionPoliciesApiServiceImpl.class);

    public ThreatProtectionPoliciesApiServiceImpl(APIMgtAdminService apiMgtAdminService) {
        this.apiMgtAdminService = apiMgtAdminService;
    }

    /**
     * Get a list of all threat protection policies
     *
     * @param request
     * @return List of threat protection policies
     * @throws NotFoundException
     */
    @Override
    public Response threatProtectionPoliciesGet(Request request) throws NotFoundException {
        try {
            List<ThreatProtectionPolicy> policyList = apiMgtAdminService.getThreatProtectionPolicyList();
            ThreatProtectionPolicyListDTO listDTO = new ThreatProtectionPolicyListDTO();
            for (ThreatProtectionPolicy policy : policyList) {
                listDTO.addListItem(MappingUtil.toThreatProtectionPolicyDTO(policy));
            }
            return Response.ok().entity(listDTO).build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }
}
