/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.mappings;

import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.dto.LabelListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * Mapping CustomPolicy and rest api CustomPolicyDTO object.
 */
public class LabelMappingUtil {

    /**
     * Converts an array of label model objects into REST API DTO objects.
     *
     * @param labelList An array of Label model objects
     * @return A List DTO of Label DTOs derived from the array of model objects
     */
    public static LabelListDTO fromLabelArrayToListDTO(List<Label> labelList) {
        LabelListDTO listDTO = new LabelListDTO();
        List<LabelDTO> labelDTOList = new ArrayList<>();
        if (labelList != null) {
            for (Label label : labelList) {
                LabelDTO dto = fromLabelToDTO(label);
                labelDTOList.add(dto);
            }
        }
        listDTO.setCount(labelDTOList.size());
        listDTO.setList(labelDTOList);
        return listDTO;
    }

    /**
     * Converts a single Label model object into DTO object.
     *
     * @param label Label  model object
     * @return DTO object derived from the label model object
     */
    public static LabelDTO fromLabelToDTO(Label label) {
        LabelDTO labelDTO = new LabelDTO();
        labelDTO.accessUrls(label.getAccessUrls());
        labelDTO.description(label.getDescription());
        labelDTO.setName(label.getName());
        labelDTO.labelUUID(label.getId());
        labelDTO.setType(label.getType());
        return labelDTO;
    }

    /**
     * convert label model object using Label DTO object
     *
     * @param dto    Label DTO object
     * @return Updated Policy object with common fields
     */
    public static Label fromDTOTLabel(LabelDTO dto) {
       return new  Label.Builder().name(dto.getName()).id(dto.getLabelUUID()).type(dto.getType()).
               accessUrls(dto.getAccessUrls()).build();
    }


}
