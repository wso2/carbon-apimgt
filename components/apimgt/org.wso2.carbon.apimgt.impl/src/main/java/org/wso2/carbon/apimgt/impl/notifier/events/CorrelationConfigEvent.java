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

package org.wso2.carbon.apimgt.impl.notifier.events;

import org.wso2.carbon.apimgt.impl.dto.CorrelationConfigDTO;
import java.util.List;
/**
 * An Event Object which holds data related to correlation config update.
 */
public class CorrelationConfigEvent extends Event {

    private List<CorrelationConfigDTO> correlationConfigDTOList;


    public CorrelationConfigEvent(List<CorrelationConfigDTO> correlationConfigDTOList, String type) {
        this.correlationConfigDTOList = correlationConfigDTOList;
        this.type = type;
    }

    public List<CorrelationConfigDTO> getCorrelationConfigDTOList() {
        return correlationConfigDTOList;
    }

    public void setCorrelationConfigDTOList(List<CorrelationConfigDTO> correlationConfigDTOList) {
        this.correlationConfigDTOList = correlationConfigDTOList;
    }

}
