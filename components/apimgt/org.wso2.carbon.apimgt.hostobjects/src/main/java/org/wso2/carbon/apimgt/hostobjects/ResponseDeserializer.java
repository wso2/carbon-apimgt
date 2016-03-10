package org.wso2.carbon.apimgt.hostobjects;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import io.swagger.models.RefResponse;
import io.swagger.models.Response;

import java.io.IOException;

public class ResponseDeserializer extends JsonDeserializer<Response> {
    @Override
    public Response deserialize(JsonParser jp, DeserializationContext ctxt)
            throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        JsonNode sub = node.get("$ref");

        if (sub != null) {
            return Json.mapper().convertValue(node, RefResponse.class);
        } else {
            Response response = Json.responseMapper().convertValue(node, Response.class);
            return response;
        }
    }
}