/*
 *
 *  Copyright (c) 2022, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
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

package org.wso2.carbon.apimgt.gateway;

import java.util.List;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigPropertyDTO;
import org.wso2.carbon.logging.correlation.CorrelationLogConfigurable;
import org.wso2.carbon.logging.correlation.bean.ImmutableCorrelationLogConfig;
import org.wso2.carbon.logging.correlation.internal.CorrelationLogManager;

/**
 * Correlation Config Manager to configure correlation components and
 * invoke the internal API.
 */
public class CorrelationConfigManager {
    private static final Log log = LogFactory.getLog(CorrelationConfigManager.class);
    private static final CorrelationConfigManager correlationConfigManager = new CorrelationConfigManager();


    public static CorrelationConfigManager getInstance() {
        return correlationConfigManager;
    }


    public void updateCorrelationConfigs(List<CorrelationConfigDTO> correlationConfigDTOList) {
        for (CorrelationConfigDTO correlationConfigDTO: correlationConfigDTOList) {
            String componentName = correlationConfigDTO.getName();
            String enabled       = correlationConfigDTO.getEnabled();
            List<CorrelationConfigPropertyDTO> correlationConfigPropertyDTOList =
                    correlationConfigDTO.getProperties();
            String[] deniedThreads = new String[0];
            for (CorrelationConfigPropertyDTO correlationConfigPropertyDTO: correlationConfigPropertyDTOList) {
                if (correlationConfigPropertyDTO.getName().equals("deniedThreads")) {
                    deniedThreads = correlationConfigPropertyDTO.getValue();
                }
            }

            CorrelationLogConfigurable service =  CorrelationLogManager.getLogServiceInstance(componentName);
            if (service != null) {
                service.onConfigure(new ImmutableCorrelationLogConfig(
                        Boolean.parseBoolean(enabled),
                        deniedThreads,
                        false
                ));
            }
        }
    }
}
