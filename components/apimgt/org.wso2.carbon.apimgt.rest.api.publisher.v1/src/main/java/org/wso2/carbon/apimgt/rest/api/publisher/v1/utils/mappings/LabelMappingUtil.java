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

import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.LabelListDTO;

import java.util.ArrayList;
import java.util.List;

public class LabelMappingUtil {

    /**
     * Convert list of Label to LabelListDTO
     *
     * @param label List of labels
     * @return LabelListDTO list containing label data
     */
    public static LabelListDTO fromLabelListToLabelListDTO(List<Label> label) {
        LabelListDTO labelListDTO = new LabelListDTO();
        labelListDTO.setCount(label.size());
        labelListDTO.setList(fromLabelListToLabelDTOList(label));
        return labelListDTO;
    }

    /**
     * Converts label List to LabelDTO List.
     *
     * @param labels list of labels
     * @return LabelDTO list
     */
    private static List<LabelDTO> fromLabelListToLabelDTOList(List<Label> labels) {
        List<LabelDTO> labelDTOs = new ArrayList<>();
        for (Label label : labels) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setName(label.getName());
            labelDTO.setDescription(label.getDescription());
            labelDTO.setAccessUrls(label.getAccessUrls());
            labelDTOs.add(labelDTO);
        }
        return labelDTOs;
    }

}
