/*
 * Copyright (c) 2021, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigAttribute;
import org.wso2.carbon.logging.correlation.CorrelationLogService;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogConstants;
import org.wso2.carbon.logging.correlation.utils.CorrelationLogUtil;

import java.util.Map;

@Component(
        immediate = true,
        service = CorrelationLogService.class
)
public class CorrelationLogServiceImpl implements CorrelationLogService {
    private static boolean correlationLogEnabled;
    private static boolean logAllMethods;

    @Activate
    protected void activate(ComponentContext context) {
    }

    @Deactivate
    protected void deactivate(ComponentContext context) {
    }

    @Override
    public String getName() {
        return "apimgt";
    }

    @Override
    public void reconfigure(Map<String, Object> properties) {
        correlationLogEnabled = (Boolean) properties.get(CorrelationLogConstants.ENABLE) &&
                CorrelationLogUtil.isComponentAllowed(this.getName(),
                        (String) properties.get(CorrelationLogConstants.COMPONENTS));
        logAllMethods = (boolean) properties.get(CorrelationLogConstants.LOG_ALL_METHODS);
    }

    @Override
    public CorrelationLogConfigAttribute[] getConfigDescriptor() {
        return new CorrelationLogConfigAttribute[] {
                new CorrelationLogConfigAttribute(CorrelationLogConstants.LOG_ALL_METHODS,
                        "Log all methods within the given component", Boolean.class.getName(), false)
        };
    }

    public static boolean isCorrelationLogEnabled() {
        return correlationLogEnabled;
    }

    public static boolean getLogAllMethods() {
        return logAllMethods;
    }
}
