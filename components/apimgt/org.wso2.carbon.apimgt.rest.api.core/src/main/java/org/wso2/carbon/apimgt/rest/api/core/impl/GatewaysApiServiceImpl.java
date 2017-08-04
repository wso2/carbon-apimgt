package org.wso2.carbon.apimgt.rest.api.core.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIGateway;
import org.wso2.carbon.apimgt.core.api.APIMgtAdminService;
import org.wso2.carbon.apimgt.core.dao.APISubscriptionDAO;
import org.wso2.carbon.apimgt.core.dao.ApiDAO;
import org.wso2.carbon.apimgt.core.dao.ApplicationDAO;
import org.wso2.carbon.apimgt.core.dao.LabelDAO;
import org.wso2.carbon.apimgt.core.dao.PolicyDAO;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.exception.APIMgtResourceNotFoundException;
import org.wso2.carbon.apimgt.core.exception.ExceptionCodes;
import org.wso2.carbon.apimgt.core.impl.APIMgtAdminServiceImpl;
import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.core.models.RegistrationSummary;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.core.GatewaysApiService;
import org.wso2.carbon.apimgt.rest.api.core.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.core.dto.LabelInfoDTO;
import org.wso2.carbon.apimgt.rest.api.core.dto.RegistrationDTO;
import org.wso2.carbon.apimgt.rest.api.core.utils.MappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.List;
import javax.ws.rs.core.Response;

public class GatewaysApiServiceImpl extends GatewaysApiService {

    private static final Logger log = LoggerFactory.getLogger(GatewaysApiServiceImpl.class);

    private APISubscriptionDAO apiSubscriptionDAO;
    private PolicyDAO policyDAO;
    private ApiDAO apiDAO;
    private LabelDAO labelDAO;
    private ApplicationDAO applicationDAO;
    private APIGateway apiGateway;


    public GatewaysApiServiceImpl(APISubscriptionDAO apiSubscriptionDAO, PolicyDAO policyDAO, ApiDAO apiDAO,
                                  LabelDAO labelDAO, ApplicationDAO applicationDAO, APIGateway apiGateway) {
        this.apiSubscriptionDAO = apiSubscriptionDAO;
        this.policyDAO = policyDAO;
        this.apiDAO = apiDAO;
        this.labelDAO = labelDAO;
        this.applicationDAO = applicationDAO;
        this.apiGateway = apiGateway;
    }

    /**
     * Register gateway
     *
     * @param body        RegistrationDTO
     * @param contentType Content-Type header value
     * @return Registration summary details
     * @throws NotFoundException If failed to register gateway
     */
    @Override
    public Response gatewaysRegisterPost(RegistrationDTO body, String contentType, Request request)
            throws NotFoundException {

        try {
            LabelInfoDTO labelInfoDTO = body.getLabelInfo();

            if (labelInfoDTO != null) {
                APIMgtAdminService adminService =  new APIMgtAdminServiceImpl(apiSubscriptionDAO, policyDAO, apiDAO,
                        labelDAO, applicationDAO, apiGateway);
                String overwriteLabels = labelInfoDTO.getOverwriteLabels();
                List<Label> labels = MappingUtil.convertToLabels(labelInfoDTO.getLabelList());
                adminService.registerGatewayLabels(labels, overwriteLabels);
                RegistrationSummary registrationSummary = adminService.getRegistrationSummary();
                return Response.ok().entity(MappingUtil.toRegistrationSummaryDTO(registrationSummary)).build();
            } else {
                String errorMessage = "Label information cannot be null";
                APIMgtResourceNotFoundException e = new APIMgtResourceNotFoundException(errorMessage,
                        ExceptionCodes.LABEL_INFORMATION_CANNOT_BE_NULL);
                HashMap<String, String> paramList = new HashMap<String, String>();
                org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
                log.error(errorMessage, e);
                return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
            }

        } catch (APIManagementException e) {
            String errorMessage = "Error while registering the gateway";
            HashMap<String, String> paramList = new HashMap<String, String>();
            org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
    }
}
