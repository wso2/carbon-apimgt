package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.impl.APIManagerFactory;
import org.wso2.carbon.apimgt.core.models.policy.ThreatProtectionPolicy;
import org.wso2.carbon.apimgt.rest.api.core.ApiResponseMessage;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.ThreatProtectionApiService;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThreatProtectionPolicyDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.ThreatProtectionPolicyListDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import javax.ws.rs.core.Response;
import java.util.List;
import java.util.Set;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

    //to-do: use APIMgtAdminService
    @Override
    public Response threatProtectionApisApiIdPolicyGet(String apiId, Request request) throws NotFoundException {
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        Set<String> idsList = null;
        try {
            idsList = dao.getThreatProtectionPolicyIdsForApi(apiId);
            return Response.ok().entity(idsList).build();
        } catch (APIMgtDAOException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Error getting Threat Protection Policy IDs").build();
    }

    @Override
    public Response threatProtectionPoliciesGet( Request request) throws NotFoundException {
        try {
            APIMgtAdminService apiMgtAdminService = APIManagerFactory.getInstance().getAPIMgtAdminService();
            List<ThreatProtectionPolicy> policyList = apiMgtAdminService.getThreatProtectionPolicyList();
            ThreatProtectionPolicyListDTO listDTO = new ThreatProtectionPolicyListDTO();
            for (ThreatProtectionPolicy policy: policyList) {
                listDTO.addListItem(MappingUtil.toThreatProtectionPolicyDTO(policy));
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
            apiMgtAdminService.addThreatProtectionPolicy(MappingUtil.toThreatProtectionPolicy(threatProtectionPolicy));
            return Response.ok().build();
        } catch (APIManagementException e) {
            log.error(e.getMessage(), e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdDelete(String threatProtectionPolicyId, Request request) throws NotFoundException {
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
            ThreatProtectionPolicyDTO dto = MappingUtil.toThreatProtectionPolicyDTO(
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
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
