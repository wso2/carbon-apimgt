package org.wso2.carbon.apimgt.rest.api.store.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigManager;
import org.wso2.carbon.apimgt.impl.alertmgt.AlertConfigurator;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.impl.dto.AlertTypeDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.AlertTypesApiService;
import org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertTypesListDTO;
import org.wso2.carbon.apimgt.rest.api.store.v1.mappings.AlertsMappingUtil;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;


public class AlertTypesApiServiceImpl implements AlertTypesApiService {
    private static final String AGENT = "subscriber";
    private static final Log log = LogFactory.getLog(AlertTypesApiServiceImpl.class);

    public Response getStoreAlertTypes(MessageContext messageContext) {
        try {
            AlertConfigurator storeAlertConfigurator = AlertConfigManager.getInstance().getAlertConfigurator(AGENT);
            List<AlertTypeDTO> alertTypes = storeAlertConfigurator.getSupportedAlertTypes(AGENT);
            AlertTypesListDTO alertTypesListDTO = new AlertTypesListDTO();
            alertTypesListDTO.setCount(alertTypes.size());
            List<org.wso2.carbon.apimgt.rest.api.store.v1.dto.AlertTypeDTO> storeAlertTypes = new ArrayList<>();
            for (AlertTypeDTO alertTypeDTO : alertTypes) {
                storeAlertTypes.add(AlertsMappingUtil.alertTypeToAlertTypeDTO(alertTypeDTO));
            }
            alertTypesListDTO.setAlerts(storeAlertTypes);
            return Response.status(Response.Status.OK).entity(alertTypesListDTO).build();
        } catch (APIManagementException e) {
            RestApiUtil
                    .handleInternalServerError("Internal Server Error Occurred while retrieving alert types", e, log);
        } catch (AlertManagementException e) {
            RestApiUtil.handleInternalServerError("Analytics not enabled", e, log);
        }
        return Response.status(Response.Status.NO_CONTENT).build();
    }
}
