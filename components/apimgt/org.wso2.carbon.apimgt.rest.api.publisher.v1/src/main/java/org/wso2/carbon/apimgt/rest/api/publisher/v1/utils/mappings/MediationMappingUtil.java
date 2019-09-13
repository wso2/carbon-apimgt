/*
 * Copyright (c) 2019 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.publisher.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Mediation;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationInfoDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.MediationListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class is responsible for mapping APIM core mediation sequence related objects into
 * REST API mediation related DTOs
 */
public class MediationMappingUtil {

    /**
     * Converts a List mediation objects into a DTO
     *
     * @param mediation a list of mediation objects
     * @param limit     max number of objects returned
     * @param offset    starting index
     * @return MediationListDTO object containing MediationInfoDTO
     */
    public static MediationListDTO fromMediationListToDTO(List<Mediation> mediation, int offset,
                                                          int limit) {

        MediationListDTO mediationListDTO = new MediationListDTO();
        List<MediationInfoDTO> mediationDTOs = mediationListDTO.getList();
        if (mediationDTOs == null) {
            mediationDTOs = new ArrayList<>();
            mediationListDTO.setList(mediationDTOs);
        }
        //identifying the proper start and end indexes
        int size = mediation.size();
        int start = offset < size && offset >= 0 ? offset : Integer.MAX_VALUE;
        int end = offset + limit - 1 <= size - 1 ? offset + limit - 1 : size - 1;

        for (int i = start; i <= end; i++) {
            mediationDTOs.add(fromMediationInfoToDTO(mediation.get(i)));
        }
        mediationListDTO.setCount(mediationDTOs.size());
        return mediationListDTO;
    }

    /**
     * Converts an Mediation object into MediationInfoDTO
     *
     * @param mediation Mediation object
     * @return MediationInfoDTO object corresponding to the given Mediation object
     */
    public static MediationInfoDTO fromMediationInfoToDTO(Mediation mediation) {

        MediationInfoDTO mediationInfoDTO = new MediationInfoDTO();
        mediationInfoDTO.setId(mediation.getUuid());
        mediationInfoDTO.setName(mediation.getName());
        mediationInfoDTO.setType(MediationInfoDTO.TypeEnum.valueOf(mediation.getType().toUpperCase()));
        return mediationInfoDTO;
    }

    /**
     * Converts an Mediation object into MediationDTO
     *
     * @param mediation Mediation object
     * @return MediationDTO object correspond to the given Mediation object
     */
    public static MediationDTO fromMediationToDTO(Mediation mediation) {

        MediationDTO mediationDTO = new MediationDTO();
        mediationDTO.setId(mediation.getUuid());
        mediationDTO.setName(mediation.getName());
        mediationDTO.setType(MediationDTO.TypeEnum.valueOf(mediation.getType().toUpperCase()));
        return mediationDTO;
    }
}
