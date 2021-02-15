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

package org.wso2.carbon.apimgt.rest.api.publisher.v1.common.template;

import org.apache.velocity.VelocityContext;

import java.util.List;

/**
 * Set the handler context
 */
public class HandlerConfigContex extends ConfigContextDecorator {

    private List<HandlerConfig> handlers;

    public HandlerConfigContex(ConfigContext context, List<HandlerConfig> handlers) {
        super(context);
        this.handlers = handlers;
    }

    //@todo: validate the handler config.

    @Override
    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("handlers", this.handlers);

        return context;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
