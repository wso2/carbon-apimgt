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
import org.apache.commons.lang.StringEscapeUtils;

/**
 * This is a utility class with a bunch of methods to help in Template
 */
public class TemplateUtilContext extends ConfigContextDecorator {

    public TemplateUtilContext(ConfigContext context) {
        super(context);
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context =  super.getContext();

        context.put("util",this);

        return context;
    }

}
