package org.wso2.carbon.apimgt.rest.api.store.v1.impl;


import org.wso2.carbon.apimgt.api.APIConsumer;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.StoreSettings;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.wso2.carbon.apimgt.rest.api.store.v1.SettingsApiService;
import org.wso2.carbon.apimgt.rest.api.util.utils.RestApiUtil;

import javax.ws.rs.core.Response;

public class SettingsApiServiceImpl extends SettingsApiService {

    private static final Log log = LogFactory.getLog(SettingsApiServiceImpl.class);

    @Override
    public Response settingsGet(){
        try {
            APIConsumer apiConsumer = RestApiUtil.getLoggedInUserConsumer();
            StoreSettings storeSettings = apiConsumer.getStoreSettings();
            return Response.ok().entity(storeSettings).build();
        } catch (APIManagementException e) {
            String errorMessage = "Error while retrieving Settings";
            RestApiUtil.handleInternalServerError(errorMessage, e, log);
        }
        return null;
    }
}
