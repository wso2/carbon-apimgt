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
     * Returns matched label
     *
     * @param labelID The {@link String} ID of the label
     * @return {@link Label} Label
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Label getLabelByID(String labelID) throws APIMgtDAOException;

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
     * @return Label updated Label
     * @throws APIMgtDAOException if error occurs while accessing data layer
     */
    Label updateLabel(Label updatedLabel) throws APIMgtDAOException;

    /**
     * Retrieve all label types
     *
     * @return list of label types
     * @throws APIMgtDAOException if error occurs while retrieving access urls
     */
     List<Label> getLabelsByType(String type) throws APIMgtDAOException;

    /**
     * Retrieve the label given the label name and label type
     *
     * @param  name label name
     * @param type  type of the label
     * @return the label Id
     * @throws APIMgtDAOException if error occurs while retrieving access urls
     */
    String getLabelIdByNameAndType(String name, String type) throws APIMgtDAOException;


}
