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

package org.wso2.carbon.apimgt.impl.template;

import org.apache.velocity.VelocityContext;
import org.wso2.carbon.apimgt.api.APIManagementException;

/**
 *
 */
public abstract class ConfigContextDecorator extends ConfigContext {
    protected final ConfigContext context;

    public ConfigContextDecorator(ConfigContext context) {
        this.context = context;
    }

    public void validate() throws APITemplateException, APIManagementException { // implementing methods of the abstract class
        context.validate();
    }

    public VelocityContext getContext() {
        return context.getContext();
    }
}
