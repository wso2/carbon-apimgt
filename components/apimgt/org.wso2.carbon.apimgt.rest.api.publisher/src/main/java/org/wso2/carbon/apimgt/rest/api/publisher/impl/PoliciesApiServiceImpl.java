package org.wso2.carbon.apimgt.rest.api.publisher.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.APIProvider;
import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.rest.api.publisher.PoliciesApiService;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.MediationListDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.utils.mappings.MediationMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.RestApiConstants;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.List;

public class PoliciesApiServiceImpl extends PoliciesApiService {

    private static final Log log = LogFactory.getLog(PoliciesApiServiceImpl.class);

    /**
     * Returns list of global Mediation policies
     *
     * @param limit       maximum number of mediation returns
     * @param offset      starting index
     * @param query       search condition
     * @param accept      accept header value
     * @param ifNoneMatch If-None-Match header value
     * @return Matched global mediation policies for given search condition
     */
    @Override
    public Response policiesMediationGet(Integer limit, Integer offset, String query,
                                         String accept, String ifNoneMatch) {
        //pre-processing
        //setting default limit and offset values if they are not set
        limit = limit != null ? limit : RestApiConstants.PAGINATION_LIMIT_DEFAULT;
        offset = offset != null ? offset : RestApiConstants.PAGINATION_OFFSET_DEFAULT;
        try {
            APIProvider apiProvider = RestApiUtil.getLoggedInUserProvider();
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
}
