/*
 *  Copyright (c) 2018, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */

package org.wso2.carbon.apimgt.hybrid.gateway.common.api;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.api.model.API;
import org.wso2.carbon.apimgt.api.model.APIIdentifier;
import org.wso2.carbon.apimgt.hybrid.gateway.common.dao.OnPremiseGatewayDAO;
import org.wso2.carbon.apimgt.hybrid.gateway.common.exception.OnPremiseGatewayException;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.executors.APIExecutor;
import org.wso2.carbon.apimgt.impl.utils.APIUtil;
import org.wso2.carbon.context.CarbonContext;
import org.wso2.carbon.governance.api.generic.GenericArtifactManager;
import org.wso2.carbon.governance.api.generic.dataobjects.GenericArtifact;

import org.wso2.carbon.registry.core.exceptions.RegistryException;
import org.wso2.carbon.registry.core.jdbc.handlers.RequestContext;

import java.util.Map;

/**
 * This class consists methods that will create, prototype, publish, block, deprecate and
 * retire  an API to API Manager.
 * <p/>
 * This executor used to publish a service to API store as a API.
 *
 */
public class APIExecutionHandler extends APIExecutor {

    Log log = LogFactory.getLog(APIExecutionHandler.class);

    /**
     * This method is called when the execution class is initialized.
     * All the execution classes are initialized only once.
     *
     * @param parameterMap Static parameter map given by the user.
     *                     These are the parameters that have been given in the
     *                     lifecycle configuration as the parameters of the executor.
     */
    public void init(Map parameterMap) {
    }

    /**
     * @param context      The request context that was generated from the registry core.
     *                     The request context contains the resource, resource path and other
     *                     variables generated during the initial call.
     * @param currentState The current lifecycle state.
     * @param targetState  The target lifecycle state.
     * @return Returns whether the execution was successful or not.
     */
    public boolean execute(RequestContext context, String currentState, String targetState) {
        try {
            if (log.isDebugEnabled()) {
                log.debug("Executing custom API executor for capturing API publish/re-publish events.");
            }

            String domain = CarbonContext.getThreadLocalCarbonContext().getTenantDomain();
            GenericArtifactManager artifactManager = APIUtil
                    .getArtifactManager(context.getSystemRegistry(), APIConstants.API_KEY);
            String artifactId = context.getResource().getUUID();
            GenericArtifact apiArtifact = artifactManager.getGenericArtifact(artifactId);
            API api = APIUtil.getAPI(apiArtifact);

            // Add API publish/Re-publish events to database
            APIIdentifier apiId = api.getId();
            OnPremiseGatewayDAO onPremiseGatewayDao = new OnPremiseGatewayDAO();
            onPremiseGatewayDao.addAPIPublishEvent(domain, apiId.toString());

            if (log.isDebugEnabled()) {
                log.debug("Successfully captured publish/re-publish event of API: " + apiId);
            }
        } catch (RegistryException e) {
            log.error("Failed to get the generic artifact while executing APIExecutor. ", e);
        } catch (APIManagementException e) {
            log.error("Failed to get artifact manager. ", e);
        } catch (OnPremiseGatewayException e) {
            log.error("Failed to record Publish/Re-publish event.", e);
        }
        return super.execute(context, currentState, targetState);
    }
}

