/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* WSO2 Inc. licenses this file to you under the Apache License,
* Version 2.0 (the "License"); you may not use this file except
* in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied. See the License for the
* specific language governing permissions and limitations
* under the License.
*
*/
package org.wso2.carbon.apimgt.core.template;

import org.apache.velocity.VelocityContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * used to define the template context
 */
public abstract class ConfigContext {
    private static final Logger log = LoggerFactory.getLogger(ConfigContext.class);

    /**
     * Method used to perform pre validations before template generation
     *
     * @throws APITemplateException in case of validation failure
     */
    public abstract void validate() throws APITemplateException;

    /**
     * Get the velocity context which holds the data to pass into the template
     *
     * @return VelocityContext object
     */
    public abstract VelocityContext getContext();

    protected void handleException(String msg) throws Exception {
        log.error(msg);
        throw new Exception(msg);
    }
}
