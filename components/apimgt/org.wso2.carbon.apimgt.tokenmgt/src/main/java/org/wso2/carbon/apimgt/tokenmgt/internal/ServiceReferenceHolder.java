/*
 *  Copyright WSO2 Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package org.wso2.carbon.apimgt.tokenmgt.internal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIManagerConfigurationService;
import org.wso2.carbon.event.output.adapter.core.OutputEventAdapterService;

public class ServiceReferenceHolder {

    private static final Log log = LogFactory.getLog(ServiceReferenceHolder.class);
    private static final ServiceReferenceHolder instance = new ServiceReferenceHolder();

    private APIManagerConfigurationService amConfigurationService;

    private ServiceReferenceHolder() {

    }

    public static ServiceReferenceHolder getInstance() {
        return instance;
    }

    public APIManagerConfigurationService getAPIManagerConfigurationService() {
        if (log.isDebugEnabled()) {
            log.debug("Retrieving API Manager Configuration Service");
        }
        return amConfigurationService;
    }

    public void setAPIManagerConfigurationService(APIManagerConfigurationService amConfigurationService) {
        if (log.isDebugEnabled()) {
            log.debug("Setting API Manager Configuration Service");
        }
        this.amConfigurationService = amConfigurationService;
    }

}
