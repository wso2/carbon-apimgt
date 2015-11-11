/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * you may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.rest.api.dcr.web;

import org.wso2.carbon.apimgt.rest.api.dcr.web.dto.RegistrationProfile;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public interface RegistrationService {

    enum ErrorCode {
        INVALID_URI("invalid_redirect_uri"), INVALID_CLIENT_METADATA("invalid_client_metadata");

        private String value;
        private ErrorCode(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * This method is used to register an Oauth application.
     *
     * @param profile contains the necessary attributes that are
     *                needed in order to register an app.
     * @return Status 200 if success including consumerKey and consumerSecret.
     */
    @POST
    Response register(RegistrationProfile profile);

    /**
     * This method is used to remove already registered Oauth application.
     *
     * @param applicationName name of the application.
     * @param userId name of the application owner.
     * @param consumerKey provided consumerKey for the registered application.
     * @return Status 200 if success.
     */
    @DELETE
    public Response unRegister(@QueryParam("applicationName") String applicationName,
                               @QueryParam("userId") String userId,
                               @QueryParam("consumerKey") String consumerKey);

}