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

import com.google.gson.Gson;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.velocity.VelocityContext;

import java.util.Map;

/**
 * This is a utility class with a bunch of methods to help in Template.
 */
public class TemplateUtilContext extends ConfigContextDecorator {

    public TemplateUtilContext(ConfigContext context) {
        super(context);
    }

    @Override
    public VelocityContext getContext() {
        VelocityContext context = super.getContext();

        context.put("util", this);

        return context;
    }

    public String escapeXml(String url) {
        return StringEscapeUtils.escapeXml(StringEscapeUtils.unescapeXml(url)).trim();
    }

    /**
     * This function converts a JSON string to a Map and this is used when rendering the templates
     *
     * @param jsonString the JSON string to be converted
     * @return a Map representation of the JSON string
     */
    public Map jsonStringToMap(String jsonString) {
        return new Gson().fromJson(jsonString, Map.class);
    }
}
