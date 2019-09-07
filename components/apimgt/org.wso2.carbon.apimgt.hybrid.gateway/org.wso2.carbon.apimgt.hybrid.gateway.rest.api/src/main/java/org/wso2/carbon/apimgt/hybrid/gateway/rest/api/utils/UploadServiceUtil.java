/*
 * Copyright (c) 2018 WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.hybrid.gateway.rest.api.utils;

import com.google.gson.JsonObject;
import javax.ws.rs.core.Response;

/**
 * This class provides the functions utilized to upload the usage data file.
 */
public final class UploadServiceUtil {

    public static Response getJsonResponse(Response.Status status, String msg, String description) {
        JsonObject responseObj = new JsonObject();
        responseObj.addProperty(UploadServiceConstants.MESSAGE, msg);
        responseObj.addProperty(UploadServiceConstants.DESCRIPTION, description);
        return Response.status(status)
                .entity(responseObj.toString())
                .build();
    }

}

