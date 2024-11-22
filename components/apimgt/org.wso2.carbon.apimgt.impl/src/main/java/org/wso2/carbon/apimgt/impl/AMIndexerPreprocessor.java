/*
 *  Copyright (c) 2024, WSO2 LLc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 LLc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.registry.core.Resource;
import org.wso2.carbon.registry.core.exceptions.RegistryException;

public class AMIndexerPreprocessor  {

    private static final Log log = LogFactory.getLog(AMIndexerPreprocessor.class);

    public void processResource(Resource resource) throws RegistryException {
        if (APIConstants.API_RXT_MEDIA_TYPE.equals(resource.getMediaType())) {
            if (log.isDebugEnabled()) {
                log.debug("Analyzing API in " + resource.getPath());
            }
            if (!resource.getProperties().isEmpty() && (resource.getProperties().get("visible_organizations") == null)) {
                if (log.isDebugEnabled()) {
                    log.debug("Update property for the resource in " + resource.getPath());
                }
                resource.setProperty("visible_organizations", "all");
            }
        }
    }

}
