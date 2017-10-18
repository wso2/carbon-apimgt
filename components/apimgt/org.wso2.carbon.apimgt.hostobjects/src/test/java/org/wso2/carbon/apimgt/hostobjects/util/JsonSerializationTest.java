/*
*  Copyright (c) 2005-2015, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
*
*  WSO2 Inc. licenses this file to you under the Apache License,
*  Version 2.0 (the "License"); you may not use this file except
*  in compliance with the License.
*  You may obtain a copy of the License at
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
package org.wso2.carbon.apimgt.hostobjects.util;


import io.swagger.models.*;
import org.junit.Assert;
import org.junit.Test;

public class JsonSerializationTest {

    @Test
    public void testSerializeASpecWithPathReferences() throws Exception {

        Swagger swagger = new Swagger()
                .host("petstore.swagger.io")
                .consumes("application/json")
                .produces("application/json");

        final RefPath expectedPath = new RefPath("http://my.company.com/paths/health.json");
        swagger.path("/health", expectedPath);
        String swaggerJson = Json.mapper().writeValueAsString(swagger);
        Swagger rebuilt = Json.mapper().readValue(swaggerJson, Swagger.class);
        final Path path = rebuilt.getPath("/health");
        final RefPath actualPath = (RefPath) path;
        Assert.assertEquals(actualPath, expectedPath);
    }

    @Test
    public void testSerializeASpecWithResponseReferences() throws Exception {
        Swagger swagger = new Swagger()
                .host("petstore.swagger.io")
                .consumes("application/json")
                .produces("application/json");
        final RefResponse expectedResponse = new RefResponse("http://my.company.com/paths/health.json");
        swagger.path("/health", new Path().get(new Operation().response(200, expectedResponse)));
        String swaggerJson = Json.mapper().writeValueAsString(swagger);
        Swagger rebuilt = Json.mapper().readValue(swaggerJson, Swagger.class);
        Assert.assertEquals(rebuilt.getPath("/health").getGet().getResponses().get("200"), expectedResponse);

    }
}