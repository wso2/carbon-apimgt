package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AdminAlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertMgtConstants;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertTypesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import java.util.List;

import javax.ws.rs.core.Response;

public class AlertTypesApiServiceImpl implements AlertTypesApiService {

    private static final Log log = LogFactory.getLog(AlertTypesApiServiceImpl.class);

    @Override public Response getAdminAlertTypes(MessageContext messageContext) throws APIManagementException {
        try {
            AdminAlertConfigurator adminAlertConfigurator = (AdminAlertConfigurator) AlertConfigManager.getInstance()
                    .getAlertConfigurator(AlertMgtConstants.ADMIN_DASHBOARD_AGENT);
            List<org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO> alertTypes = adminAlertConfigurator
                    .getSupportedAlertTypes();
            AlertTypesListDTO alertTypesListDTO = AlertsMappingUtil.fromAlertTypesListToAlertTypeListDTO(alertTypes);

            return Response.status(Response.Status.OK).entity(alertTypesListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Internal Server Error Occurred while retrieving alert types", e, log);
        } catch (AlertManagementException e) {
            log.warn("API Manager Analytics is not enabled", e);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
