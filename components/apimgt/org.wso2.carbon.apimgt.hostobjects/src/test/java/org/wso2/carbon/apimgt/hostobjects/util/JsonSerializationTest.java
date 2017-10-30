/*
*  Copyright (c) 2005-2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.swagger.models.*;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

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

        ObjectNode obj = Mockito.mock(ObjectNode.class);
        Mockito.when(obj.get("name")).thenReturn(jsonNode);
        Mockito.when(obj.get("namespace")).thenReturn(jsonNode);
        Mockito.when(obj.get("prefix")).thenReturn(jsonNode);
        Mockito.when(obj.get("attribute")).thenReturn(jsonNode);
        Mockito.when(obj.get("wrapped")).thenReturn(jsonNode);
        Mockito.when(obj.get("xml")).thenReturn(obj);
        Assert.assertNotNull(propertyDeserializer.getXml(obj));
    }

    @Test
    public void testDeserializeArray() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        String array = "{\n" +
                "   \"properties\": {\n" +
                "      \"type\": {\n" +
                "         \"type\": \"string\",\n" +
                "         \"default\": \"BBANDS\"\n" +
                "      },\n" +
                "      \"computeOn\": {\n" +
                "         \"type\": \"array\",\n" +
                "         \"items\": {\n" +
                "            \"type\": \"string\"\n" +
                "         },\n" +
                "         \"default\": [\n" +
                "            \"close\"\n" +
                "         ]\n" +
                "      },\n" +
                "      \"parameters\": {\n" +
                "         \"type\": \"object\",\n" +
                "         \"properties\": {\n" +
                "            \"timeperiod\": {\n" +
                "               \"type\": \"integer\",\n" +
                "               \"format\": \"int32\",\n" +
                "               \"default\": 5\n" +
                "            },\n" +
                "            \"nbdevup\": {\n" +
                "               \"type\": \"integer\",\n" +
                "               \"format\": \"int32\",\n" +
                "               \"default\": 2\n" +
                "            },\n" +
                "            \"nbdevdn\": {\n" +
                "               \"type\": \"integer\",\n" +
                "               \"format\": \"int32\",\n" +
                "               \"default\": 2\n" +
                "            },\n" +
                "            \"matype\": {\n" +
                "               \"type\": \"integer\",\n" +
                "               \"format\": \"int32\",\n" +
                "               \"default\": 0\n" +
                "            }\n" +
                "         }\n" +
                "      }\n" +
                "   }\n" +
                "}";
        JsonNode jsonNode = mapper.readTree(array);
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
    public void testDeserializeArrays() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        String array = "\"indicators\": {\n" +
                "                \"type\": \"array\",\n" +
                "                \"items\": {\n" +
                "                    \"$ref\": \"#/definitions/Indicator\"\n" +
                "                }\n" +
                "            }";
        JsonNode jsonNode = mapper.readTree(array);
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        PropertyDeserializer propertyDeserializer = new PropertyDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        try {
            propertyDeserializer.propertyFromNode(jsonNode);
            propertyDeserializer.deserialize(jsonParser, deserializationContext);
            propertyDeserializer.getXml(jsonNode);
        } catch (JsonProcessingException e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testDeserializePropertyFromNodeArray() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Pet.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        String arrayJson = "{\"parameters\": [\n" +
                "{\n" +
                "  \"name\": \"petId\",\n" +
                "  \"in\": \"path\",\n" +
                "  \"description\": \"ID of pet to update\",\n" +
                "  \"required\": true,\n" +
                "  \"type\": \"integer\",\n" +
                "  \"format\": \"int64\"\n" +
                "},\n" +
                "{\n" +
                "\"name\": \"additionalMetadata\",\n" +
                "\"in\": \"formData\",\n" +
                "\"description\": \"Additional data to pass to server\",\n" +
                "\"required\": false,\n" +
                "\"type\": \"string\"\n" +
                "},\n" +
                "{\n" +
                "\"name\": \"file\",\n" +
                "\"in\": \"formData\",\n" +
                "\"description\": \"file to upload\",\n" +
                "\"required\": false,\n" +
                "\"type\": \"file\"\n" +
                "}\n" +
                "]}";
        JsonNode root = mapper.readTree(arrayJson.getBytes());
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        PropertyDeserializer propertyDeserializer = new PropertyDeserializer();
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        try {
            propertyDeserializer.propertyFromNode(jsonNode);
            propertyDeserializer.deserialize(jsonParser, deserializationContext);
        } catch (JsonProcessingException e) {
            Assert.assertFalse(true);
        }
    }


    @Test
    public void testDeserializeModel() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "ModelTest.json";
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
                        + File.separator + "Parameter.json";
        String headerNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"header\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        String queryNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"query\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        String bodyNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"body\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        String cookieNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"cookie\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        String pathNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"path\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        String formDataNode = "{\n" +
                "      \"name\": \"api_key\",\n" +
                "      \"in\": \"formData\",\n" +
                "      \"description\": \"\",\n" +
                "      \"required\": false,\n" +
                "      \"type\": \"string\"\n" +
                "    }";
        Map<String, String> stringMap = new HashMap<String, String>();
        stringMap.put("Header", headerNode);
        stringMap.put("Body", bodyNode);
        stringMap.put("Path", pathNode);
        stringMap.put("FormData", formDataNode);
        stringMap.put("Cookie", cookieNode);
        stringMap.put("Query", queryNode);

        for (String nodeString : stringMap.values()) {
            JsonParser jsonParser = new JsonFactory().createParser(new File(path));
            ObjectMapper mapper = new ObjectMapper();
            JsonNode root = mapper.readTree(nodeString.getBytes());
            JsonNode jsonNode = root;
            ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
            Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
            jsonParser.setCodec(objectCodec);
            ParameterDeserializer parameterDeserializer = new ParameterDeserializer();
            DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
            try {
                parameterDeserializer.deserialize(jsonParser, deserializationContext);
            } catch (JsonProcessingException e) {
                Assert.assertFalse(true);
            }
        }
    }

    @Test
    public void testDeserializeSecurityDef() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Security.json";
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
        } catch (JsonProcessingException e) {
            Assert.assertFalse(true);
        }
    }

    @Test
    public void testJson() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "Security.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        Json json = new Json();
        Assert.assertNotNull(json.responseMapper());
        Assert.assertNotNull(json.pretty(jsonNode));
    }

    @Test
    public void testResponse() throws Exception {
        String path =
                "src" + File.separator + "test" + File.separator + "resources" + File.separator + "swagger"
                        + File.separator + "ModelTest.json";
        JsonParser jsonParser = new JsonFactory().createParser(new File(path));
        ObjectMapper mapper = new ObjectMapper();
        JsonNode root = mapper.readTree(new File(path));
        JsonNode jsonNode = root;
        ObjectCodec objectCodec = Mockito.mock(ObjectCodec.class);
        Mockito.when(objectCodec.readTree(jsonParser)).thenReturn(jsonNode);
        jsonParser.setCodec(objectCodec);
        DefaultDeserializationContext deserializationContext = Mockito.mock(DefaultDeserializationContext.class);
        ResponseDeserializer responseDeserializer = new ResponseDeserializer();
        try {
            responseDeserializer.deserialize(jsonParser, deserializationContext);
        } catch (JsonProcessingException e) {
            Assert.assertFalse(true);
        }
    }
}