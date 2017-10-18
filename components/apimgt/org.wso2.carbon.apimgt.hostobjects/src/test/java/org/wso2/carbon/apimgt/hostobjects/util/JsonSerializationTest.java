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


import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.deser.DefaultDeserializationContext;
import io.swagger.models.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;

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

    @Test
    public void testDeserializePetStoreFile() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        PropertyDeserializer propertyDeserializer = new PropertyDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        Assert.assertNotNull(propertyDeserializer.propertyFromNode(jsonNode));
        propertyDeserializer.deserialize(jsonParser, deserializationContext);
        propertyDeserializer.getXml(jsonNode);
    }

    @Test
    public void testDeserializeModel() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        ModelDeserializer modelDeserializer = new ModelDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        Assert.assertNotNull(modelDeserializer.deserialize(jsonParser, deserializationContext));
    }

    @Test
    public void testDeserializeParameter() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        ParameterDeserializer parameterDeserializer = new ParameterDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        try {
            parameterDeserializer.deserialize(jsonParser, deserializationContext);
        }catch (JsonProcessingException e){
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testDeserializeSecurityDef() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        SecurityDefinitionDeserializer securityDefinitionDeserializer = new SecurityDefinitionDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        try {
            securityDefinitionDeserializer.deserialize(jsonParser, deserializationContext);
        }catch (JsonProcessingException e){
            Assert.assertFalse(true);
        }
    }
}