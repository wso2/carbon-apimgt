package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiCommonUtil;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.MediationPoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.common.mappings.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.common.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;
import org.wso2.carbon.registry.api.Resource;
import org.wso2.carbon.registry.core.RegistryConstants;

import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;

public class MediationPoliciesApiServiceImpl implements MediationPoliciesApiService {

    private static final Log log = LogFactory.getLog(ApisApiServiceImpl.class);

    /**
     * Returns list of global Mediation policies
     *
     * @param limit       maximum number of mediation returns
     * @param offset      starting index
     * @param query       search condition
     * @param ifNoneMatch If-None-Match header value
     * @return Matched global mediation policies for given search condition
     */
    @Override
    public Response getAllGlobalMediationPolicies(Integer limit, Integer offset, String query, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
            List<Mediation> mediationList = apiProvider.getAllGlobalMediationPolicies();
            MediationListDTO mediationListDTO =
                    MediationMappingUtil.fromMediationListToDTO(mediationList, offset, limit);
            return Response.ok().entity(mediationListDTO).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving global mediation policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
            return null;
        }
    }
    /**
     * Returns content of a global Mediation policy
     *
     * @param mediationPolicyId       search condition
     * @param ifNoneMatch If-None-Match header value
     * @return Matched global mediation policies for given search condition
     */
    @Override
    public Response getGlobalMediationPolicyContent(String mediationPolicyId, String ifNoneMatch, MessageContext messageContext) throws APIManagementException {

        String username = RestApiCommonUtil.getLoggedInUsername();
        APIProvider apiProvider = RestApiCommonUtil.getLoggedInUserProvider();
        String tenantDomain = RestApiCommonUtil.getLoggedInUserTenantDomain();
        try {
            //Get registry resource correspond to identifier
            Resource mediationResource = apiProvider.getCustomMediationResourceFromUuid(mediationPolicyId);
            if (mediationResource != null) {
                // get the registry resource path
                String resource = mediationResource.getPath();
                resource = RegistryConstants.GOVERNANCE_REGISTRY_BASE_PATH + resource;
                Map<String, Object> mediationPolicyResourceMap = APIUtil.getDocument(username, resource, tenantDomain);
                Object fileDataStream = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_DATA);
                Object contentType = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_CONTENT_TYPE);
                contentType = contentType == null ? RestApiConstants.APPLICATION_OCTET_STREAM : contentType;
                String name = mediationPolicyResourceMap.get(APIConstants.DOCUMENTATION_RESOURCE_MAP_NAME).toString();
                return Response.ok(fileDataStream)
                        .header(RestApiConstants.HEADER_CONTENT_TYPE, contentType)
                        .header(RestApiConstants.HEADER_CONTENT_DISPOSITION, "attachment; filename=\"" + name + "\"")
                        .build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving global mediation policies";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
            return null;
        }
        return null;
    }
}
