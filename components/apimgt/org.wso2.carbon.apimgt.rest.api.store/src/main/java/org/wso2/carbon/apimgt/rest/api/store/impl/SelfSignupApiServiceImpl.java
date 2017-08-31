/*
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

package org.wso2.carbon.apimgt.rest.api.store.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.core.api.APIStore;
import org.wso2.carbon.apimgt.core.exception.APIManagementException;
import org.wso2.carbon.apimgt.core.util.APIMgtConstants;
import org.wso2.carbon.apimgt.rest.api.common.dto.ErrorDTO;
import org.wso2.carbon.apimgt.rest.api.common.util.RestApiUtil;
import org.wso2.carbon.apimgt.rest.api.store.NotFoundException;
import org.wso2.carbon.apimgt.rest.api.store.SelfSignupApiService;
import org.wso2.carbon.apimgt.rest.api.store.dto.UserDTO;
import org.wso2.carbon.apimgt.rest.api.store.mappings.MiscMappingUtil;
import org.wso2.msf4j.Request;

import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * Implementation of Self Sign Up service
 */
public class SelfSignupApiServiceImpl extends SelfSignupApiService {

    private static final Logger log = LoggerFactory.getLogger(SelfSignupApiServiceImpl.class);

    @Override
    public Response selfSignupPost(UserDTO body, Request request) throws NotFoundException {
        try {
            APIStore apiStore = RestApiUtil.getConsumer();
            apiStore.selfSignUp(MiscMappingUtil.fromUserDTOToUser(body));
        } catch (APIManagementException e) {
            String errorMessage = "Error occurred while user signup: " + body.getUsername();
            Map<String, String> paramList = new HashMap<>();
            paramList.put(APIMgtConstants.ExceptionsConstants.USERNAME, body.getUsername());
            ErrorDTO errorDTO = RestApiUtil.getErrorDTO(e.getErrorHandler(), paramList);
            errorDTO.setDescription(e.getMessage());
            log.error(errorMessage, e);
            return Response.status(e.getErrorHandler().getHttpStatusCode()).entity(errorDTO).build();
        }
        body.setPassword(null);
        return Response.ok(body).build();
    }
}
