/*
 *
 *  Copyright (c) 2022, WSO2 LLC. (http://www.wso2.com) All Rights Reserved.
 *
 *  WSO2 LLC. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  n compliance with the License.
 *  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 */

package org.wso2.carbon.apimgt.impl.correlation;

import org.osgi.service.component.annotations.Component;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;

/**
 * The log service component for method correlation configurations.
 */
@Component(
        immediate = true,
        service = CorrelationLogConfigurable.class
)
public class MethodCallsCorrelationLogService implements CorrelationLogConfigurable {
    @Override public String getName() {
        return "method-calls";
    }

    @Override public ImmutableCorrelationLogConfig getConfiguration() {
        return new ImmutableCorrelationLogConfig(MethodCallsCorrelationConfigDataHolder.isEnable(),
                new String[0], false);
    }

    @Override public void onConfigure(ImmutableCorrelationLogConfig immutableCorrelationLogConfig) {
        MethodCallsCorrelationConfigDataHolder.setEnable(immutableCorrelationLogConfig.isEnable());
    }
}
