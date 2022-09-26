package org.wso2.carbon.apimgt.rest.api.admin.v1.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.cxf.jaxrs.ext.MessageContext;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.alertmgt.exception.AlertManagementException;
import org.wso2.carbon.apimgt.rest.api.admin.v1.AlertTypesApiService;
import org.wso2.carbon.apimgt.rest.api.admin.v1.common.impl.AlertTypesCommonImpl;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.AlertTypesListDTO;

import javax.ws.rs.core.Response;

public class AlertTypesApiServiceImpl implements AlertTypesApiService {

    private static final Log log = LogFactory.getLog(AlertTypesApiServiceImpl.class);

    @Override
    public Response getAdminAlertTypes(MessageContext messageContext) throws APIManagementException {
        try {
            AlertTypesListDTO alertTypesListDTO = AlertTypesCommonImpl.getAdminAlertTypes();
            return Response.status(Response.Status.OK).entity(alertTypesListDTO).build();
        } catch (AlertManagementException e) {
            log.warn("API Manager Analytics is not enabled", e);
            return Response.status(Response.Status.NO_CONTENT).build();
        }
    }
}
