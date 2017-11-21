package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.dao.ThreatProtectionDAO;
import org.wso2.carbon.apimgt.core.dao.impl.DAOFactory;
import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.exception.GatewayException;
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
import java.util.UUID;

public class ThreatProtectionApiServiceImpl extends ThreatProtectionApiService {

    private static final Logger log = LoggerFactory.getLogger(ThreatProtectionApiServiceImpl.class);

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

        APIGateway gateway = APIManagerFactory.getInstance().getApiGateway();
        try {
            ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
            ThreatProtectionPolicy policy = MappingUtil.toThreatProtectionPolicy(threatProtectionPolicy);

            String policyUuid = policy.getUuid();
            if (policyUuid == null || policyUuid.length() == 0) {
                policy.setUuid(UUID.randomUUID().toString());
            }
            gateway.addThreatProtectionPolicy(policy);
            dao.addPolicy(policy);
            return Response.status(201).build();
        } catch (APIMgtDAOException e) {
            log.error("Error adding Threat Protection Policy.");
        } catch (GatewayException e) {
            log.error("Error publishing Threat Protection Policy to Topic Connection.");
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdDelete(String threatProtectionPolicyId, Request request) throws NotFoundException {
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        APIGateway gateway = APIManagerFactory.getInstance().getApiGateway();
        try {
            dao.deletePolicy(threatProtectionPolicyId);

            ThreatProtectionPolicy policy = new ThreatProtectionPolicy();
            policy.setUuid(threatProtectionPolicyId);
            gateway.deleteThreatProtectionPolicy(policy);
            return Response.ok().build();
        } catch (APIMgtDAOException e) {
            log.error("Error deleting threat protection policy. PolicyID: " + threatProtectionPolicyId, e);
        } catch (GatewayException e) {
            log.error("Error publishing threat protection policy delete event to gateway. PolicyID: "
                    + threatProtectionPolicyId, e);
        }
        return Response.status(500).entity("Internal Server Error.").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdGet(String threatProtectionPolicyId, Request request) throws NotFoundException {
        ThreatProtectionDAO dao = DAOFactory.getThreatProtectionDAO();
        try {
            ThreatProtectionPolicyDTO dto = MappingUtil.toThreatProtectionPolicyDTO(
                    dao.getPolicy(threatProtectionPolicyId));

            return dto != null? Response.ok().entity(dto).build(): Response.status(404).entity("Policy not found.").
                    build();
        } catch (APIMgtDAOException e) {
            log.error("Error getting threat protection policy for PolicyID: " + threatProtectionPolicyId, e);
        }
        return Response.status(500).entity("Internal Server Error").build();
    }

    @Override
    public Response threatProtectionPolicyThreatProtectionPolicyIdPost(String threatProtectionPolicyId
            , ThreatProtectionPolicyDTO threatProtectionPolicy
            ,Request request) throws NotFoundException {
        // do some magic!
        return Response.ok().entity(new ApiResponseMessage(ApiResponseMessage.OK, "magic!")).build();
    }
}
