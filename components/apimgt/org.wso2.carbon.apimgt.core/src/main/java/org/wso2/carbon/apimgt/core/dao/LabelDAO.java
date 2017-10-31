/*
 *
 *   Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *   WSO2 Inc. licenses this file to you under the Apache License,
 *   Version 2.0 (the "License"); you may not use this file except
 *   in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.core.dao;

import org.wso2.carbon.apimgt.core.exception.APIMgtDAOException;
import org.wso2.carbon.apimgt.core.models.Label;

import java.util.List;

/**
 * Provides access to the Label data layer
 */
public interface LabelDAO {

    /**
     * Returns all the available labels
     *
     * @return {@code List<Label>} List of labels
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<Label> getLabels() throws APIMgtDAOException;

    /**
     * Add labels if not exist
     *
     * @param labels The {@code List<Label>}List of labels
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void addLabels(List<Label> labels) throws APIMgtDAOException;

    /**
     * Returns matched label
     *
     * @param labelName The {@link String} Name of the label
     * @return {@link Label} Label
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Label getLabelByName(String labelName) throws APIMgtDAOException;

    /**
     * Returns matched labels
     *
     * @param labelNames The {@code List<String>} Label names
     * @return {@code List<Label>} List of labels
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    List<Label> getLabelsByName(List<String> labelNames) throws APIMgtDAOException;

    /**
     * Remove label
     *
     * @param labelName The {@link String} Name of the label
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void deleteLabel(String labelName) throws APIMgtDAOException;

    /**
     * Update the label
     *
     * @param updatedLabel Updated Label
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    void updateLabel(Label updatedLabel) throws APIMgtDAOException;

}
