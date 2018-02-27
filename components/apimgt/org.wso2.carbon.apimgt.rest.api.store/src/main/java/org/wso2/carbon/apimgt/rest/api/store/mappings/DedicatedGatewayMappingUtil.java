/*
 * Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.DedicatedGateway;
import org.wso2.carbon.apimgt.rest.api.store.dto.DedicatedGatewayDTO;

public class DedicatedGatewayMappingUtil {


    /**
     * This method maps the the DedicatedGateway object to DedicatedGatewayDTO
     *
     * @param dedicatedGateway DedicatedGateway object
     * @return Dedicated Gateway Object
     */
    public static DedicatedGatewayDTO toDedicatedGatewayDTO(DedicatedGateway dedicatedGateway) {

        DedicatedGatewayDTO dedicatedGatewayDTO = new DedicatedGatewayDTO();
        dedicatedGatewayDTO.setIsEnabled(dedicatedGateway.isEnabled());
        return dedicatedGatewayDTO;
    }

    /**
     * This method maps the the DedicatedGatewayDTO object to DedicatedGateway Object
     *
     * @param dedicatedGatewayDTO contains data of DedicatedGateway
     * @param apiId               UUID of the API
     * @param username            Username
     * @return Dedicated Gateway Object
     */
    public static DedicatedGateway fromDTOtoDedicatedGateway(DedicatedGatewayDTO dedicatedGatewayDTO, String apiId,
                                                             String username) {

        DedicatedGateway dedicatedGateway = new DedicatedGateway();
        dedicatedGateway.setApiId(apiId);
        dedicatedGateway.setUpdatedBy(username);
        if (dedicatedGatewayDTO.getIsEnabled() != null) {
            dedicatedGateway.setEnabled(dedicatedGatewayDTO.getIsEnabled());
        } else {
            dedicatedGateway.setEnabled(false);
        }
        return dedicatedGateway;

    }
}
