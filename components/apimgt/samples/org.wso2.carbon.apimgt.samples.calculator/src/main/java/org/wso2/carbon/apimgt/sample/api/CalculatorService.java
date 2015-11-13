/*
 * Copyright (c) 2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.sample.api;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/")
public class CalculatorService {

    @GET
    @Path("/add")
    public Response getAddition(@QueryParam("x") double x, @QueryParam("y") double y) {
        double answer = x + y;
        return Response.ok("{\"answer\": \"" + answer + "\"}", MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/subtract")
    public Response getSubtraction(@QueryParam("x") double x, @QueryParam("y") double y) {
        double answer = x - y;
        return Response.ok("{\"answer\": \"" + answer + "\"}", MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/multiply")
    public Response getMultiplication(@QueryParam("x") double x, @QueryParam("y") double y) {
        double answer = x * y;
        return Response.ok("{\"answer\": \"" + answer + "\"}", MediaType.APPLICATION_JSON).build();
    }

    @GET
    @Path("/divide")
    public Response getDivision(@QueryParam("x") double x, @QueryParam("y") double y) {
        Response response;
        double answer;
        if (y == 0) {
            response = Response.status(Response.Status.BAD_REQUEST).type(MediaType.APPLICATION_JSON_TYPE)
                    .entity("{\"error\":\"Can't divide by 0 (zero)\"}").build();
        } else {
            answer = x / y;
            return Response.ok("{\"answer\": \"" + answer + "\"}", MediaType.APPLICATION_JSON).build();

        }
        return response;
    }

}
