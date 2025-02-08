/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.rest.api.admin.v1.utils.mappings;

import org.wso2.carbon.apimgt.api.model.Label;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelDTO;
import org.wso2.carbon.apimgt.rest.api.admin.v1.dto.LabelListDTO;

import java.util.ArrayList;
import java.util.List;

/**
 * This class used for mapping utility to Label related operations
 */
public class LabelMappingUtil {

    /**
     * Converts a LabelDTO to Label
     *
     * @param labelDTO LabelDTO
     * @return Label label object
     */
    public static Label fromLabelDTOToLabel(LabelDTO labelDTO) {
        Label label = new Label();

        label.setLabelId(labelDTO.getId());
        label.setName(labelDTO.getName());
        label.setDescription(labelDTO.getDescription());
        return label;
    }

    /**
     * Converts a Label to LabelDTO
     *
     * @param label label object
     * @return LabelDTO label data
     */
    public static LabelDTO fromLabelToLabelDTO(Label label) {
        LabelDTO labelDTO = new LabelDTO();

        labelDTO.setId(label.getLabelId());
        labelDTO.setName(label.getName());
        labelDTO.setDescription(label.getDescription());
        return labelDTO;
    }

    /**
     * Convert list of Labels to LabelListDTO
     *
     * @param labels List of labels
     * @return LabelListDTO list containing label data
     */
    public static LabelListDTO fromLabelListToLabelListDTO(List<Label> labels) {
        LabelListDTO labelListDTO = new LabelListDTO();
        labelListDTO.setCount(labels.size());
        labelListDTO.setList(fromLabelListToLabelDTOList(labels));
        return labelListDTO;
    }

    /**
     * Converts label List to LabelDTO List.
     *
     * @param labels List of labels
     * @return List<LabelDTO> list of label data
     */
    private static List<LabelDTO> fromLabelListToLabelDTOList(List<Label> labels) {
        List<LabelDTO> labelDTOs = new ArrayList<>();
        for (Label label : labels) {
            LabelDTO labelDTO = new LabelDTO();
            labelDTO.setId(label.getLabelId());
            labelDTO.setName(label.getName());
            labelDTO.setDescription(label.getDescription());
            labelDTOs.add(labelDTO);
        }
        return labelDTOs;
    }
}
