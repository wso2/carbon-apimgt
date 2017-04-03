/*
* Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.wso2.carbon.apimgt.rest.api.store.mappings;

import org.wso2.carbon.apimgt.core.models.Label;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.store.dto.LabelListDTO;

import java.util.ArrayList;
import java.util.List;

public class LabelMappingUtil {

    /**
     * Convert List of labels to LabelListDTO
     *
     * @param labels List of Labels
     * @return LabelListDTO
     */
    public static LabelListDTO toLabelListDTO(List<Label> labels) {
        LabelListDTO labelListDTO = new LabelListDTO();
        labelListDTO.setCount(labels.size());
        labelListDTO.setList(toLabelDTO(labels));
        return labelListDTO;
    }

    /**
     * Converts labels to list of LabelDTO
     *
     * @param labels List of Labels
     * @return List of LabelDTOs
     */
    private static List<LabelDTO> toLabelDTO(List<Label> labels) {
        List<LabelDTO> labelDTOs = new ArrayList<>();
        for (Label label : labels) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setLabelId(label.getId());
            labelDTO.setName(label.getName());
            labelDTO.setAccessUrls(label.getAccessUrls());
            labelDTOs.add(labelDTO);
        }
        return labelDTOs;
    }
}
