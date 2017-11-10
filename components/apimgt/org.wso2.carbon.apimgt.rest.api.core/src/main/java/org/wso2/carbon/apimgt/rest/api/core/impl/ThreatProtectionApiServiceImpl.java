package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.*;
import org.wso2.carbon.apimgt.rest.api.core.dto.*;


import java.util.List;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;

import java.io.InputStream;

import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.formparam.FormDataParam;
import org.wso2.msf4j.formparam.FileInfo;
import org.wso2.msf4j.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

    @Override
    public Response threatProtectionPoliciesGet( Request request) throws NotFoundException {
        try {
            ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
            List<ThreatProtectionPolicy> policyList = dao.getPolicies();
            ThreatProtectionPolicyListDTO listDTO = new ThreatProtectionPolicyListDTO();
            for (ThreatProtectionPolicy policy: policyList) {
                listDTO.addListItem(MappingUtil.toThreatProtectionPolicyDTO(policy));
            }
            return Response.ok().entity(listDTO).build();
        } catch (APIMgtDAOException e) {
            log.error("Error retrieving Threat Protection Policies.");
        }

        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyPost(ThreatProtectionPolicyDTO threatProtectionPolicy
            ,Request request) throws NotFoundException {
        try {
            ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
            ThreatProtectionPolicy policy = MappingUtil.toThreatProtectionPolicy(threatProtectionPolicy);
            dao.addPolicy(policy);
            return Response.status(201).build();
        } catch (APIMgtDAOException e) {
            log.error("Error adding Threat Protection Policy.");
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }
    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdGet( Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdPost(String threatProtectionPolicyId
            , ThreatProtectionPolicyDTO threatProtectionPolicy
            ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
