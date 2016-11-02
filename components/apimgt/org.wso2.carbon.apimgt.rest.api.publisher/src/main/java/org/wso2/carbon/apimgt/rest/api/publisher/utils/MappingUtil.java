/***********************************************************************************************************************
 *
 *  *
 *  *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *  *
 *  *   WSO2 Inc. licenses this file to you under the Apache License,
 *  *   Version 2.0 (the "License"); you may not use this file except
 *  *   in compliance with the License.
 *  *   You may obtain a copy of the License at
 *  *
 *  *     http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  *  Unless required by applicable law or agreed to in writing,
 *  *  software distributed under the License is distributed on an
 *  *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  *  KIND, either express or implied.  See the License for the
 *  *  specific language governing permissions and limitations
 *  *  under the License.
 *  *
 *
 */

package org.wso2.carbon.apimgt.rest.api.publisher.utils;


import org.wso2.carbon.apimgt.core.models.API;
import org.wso2.carbon.apimgt.core.models.BusinessInformation;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.APIDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.dto.API_businessInformationDTO;

public class MappingUtil {

    /**
     * This method converts the API Object from models into APIDTO object.
     * @param api
     * @return
     */
    public static APIDTO toAPIDto(API api) {
        APIDTO apidto = new APIDTO();
        apidto.setId(api.getId());
        apidto.setName(api.getName());
        apidto.version(api.getVersion());
        apidto.setContext(api.getContext());
        apidto.setDescription(api.getDescription());
        apidto.setEndpointConfig(api.getEndpointConfig());
        BusinessInformation businessInformation = api.getBusinessInformation();
        API_businessInformationDTO apiBusinessInformationDTO = new API_businessInformationDTO();
        apiBusinessInformationDTO.setBusinessOwner(businessInformation.getBusinessOwner());
        apiBusinessInformationDTO.setBusinessOwnerEmail(businessInformation.getBusinessOwnerEmail());
        apiBusinessInformationDTO.setTechnicalOwner(businessInformation.getTechnicalOwner());
        apiBusinessInformationDTO.setTechnicalOwnerEmail(businessInformation.getTechnicalOwnerEmail());
        apidto.setBusinessInformation(apiBusinessInformationDTO);
        return apidto;
    }

    /**
     * This method converts the API model object from the DTO object.
     * @param apidto
     * @return
     */
    public static API toAPI(APIDTO apidto) {
        API api = new API(apidto.getProvider(),apidto.getVersion(),apidto.getName());
        api.setId(apidto.getId());
        api.setContext(apidto.getContext());
        api.setDescription(apidto.getDescription());
        api.setEndpointConfig(apidto.getEndpointConfig());
        BusinessInformation businessInformation = new BusinessInformation();
        API_businessInformationDTO apiBusinessInformationDTO = apidto.getBusinessInformation();
        businessInformation.setBusinessOwner(apiBusinessInformationDTO.getBusinessOwner());
        businessInformation.setBusinessOwnerEmail(apiBusinessInformationDTO.getBusinessOwnerEmail());
        businessInformation.setTechnicalOwner(apiBusinessInformationDTO.getTechnicalOwner());
        businessInformation.setTechnicalOwnerEmail(apiBusinessInformationDTO.getTechnicalOwnerEmail());
        api.setBusinessInformation(businessInformation);
        return api;
    }
}
