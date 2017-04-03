/*
 *
 *   Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import org.wso2.carbon.apimgt.core.models.Workflow;

/**
 * Provides access to the Workflow data layer
 */
public interface WorkflowDAO {
    /**
     * Add workflow entry
     * @param workflow workflow data
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    void addWorkflowEntry(Workflow workflow) throws APIMgtDAOException;

    /**
     * Update workflow entry with the given status
     * @param workflow workflow data
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    void updateWorkflowStatus(Workflow workflow) throws APIMgtDAOException;    
    
    /**
     * Returns a workflow object for a given external workflow reference.
     *
     * @param workflowReference
     * @return Workflow workflow data
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    public Workflow retrieveWorkflow(String workflowReference) throws APIMgtDAOException;

    /**
     * Get the external reference id for a given subscription id. 
     * @param subscriptionId subscription id
     * @return String external reference id
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    String getExternalWorkflowReferenceForSubscription(String subscriptionId) throws APIMgtDAOException;

    /**
     * Get the external reference id for a given application id. 
     * @param appId application id
     * @return String external reference id
     * @throws APIMgtDAOException if API Manager core level exception occurred
     */
    String getExternalWorkflowReferenceForApplication(String appId) throws APIMgtDAOException;
}
