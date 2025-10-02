/*
 *  Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.solace.notifiers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.dao.ApiMgtDAO;
import org.wso2.carbon.apimgt.impl.dto.SolaceConfig;
import org.wso2.carbon.apimgt.impl.notifier.ApplicationNotifier;
import org.wso2.carbon.apimgt.impl.notifier.events.ApplicationEvent;
import org.wso2.carbon.apimgt.impl.notifier.events.Event;
import org.wso2.carbon.apimgt.impl.notifier.exceptions.NotifierException;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.apimgt.solace.api.v2.SolaceV2ApiHolder;

/**
 * Handles Solace App Registration upon WSO2 API Manager DevPortal Application events.
 */
public class SolaceApplicationNotifier extends ApplicationNotifier {

    protected ApiMgtDAO apiMgtDAO;
    private static final Log log = LogFactory.getLog(SolaceApplicationNotifier.class);


    @Override
    public boolean publishEvent(Event event) throws NotifierException {
        try {
            SolaceConfig solaceConfig = APIUtil.getSolaceConfig();
            if (solaceConfig != null && solaceConfig.isEnabled()) {
                apiMgtDAO = ApiMgtDAO.getInstance();
                if (APIConstants.EventType.APPLICATION_DELETE.name().equals(event.getType())) {
                    deleteAppRegistrationIfPresent(event);
                }
                /*
                Application Creation event is not handled here - it is handled along with subscription creation.
                We don't want to create an app registration unnecessarily, if the dev portal app is not going to be
                subscribed to a Solace API.
                 */
            }
            return true;
        } catch (APIManagementException e) {
            throw new NotifierException("Error while processing application event", e);
        }
    }

    private void deleteAppRegistrationIfPresent(Event event) throws APIManagementException {
        String applicationUuid = ((ApplicationEvent) event).getUuid();
        if (SolaceV2ApiHolder.getInstance().isAppRegistrationExists(applicationUuid)) {
            SolaceV2ApiHolder.getInstance().deleteAppRegistration(applicationUuid);
        }
    }

}
