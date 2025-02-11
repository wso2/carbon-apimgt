package org.wso2.carbon.apimgt.rest.api.publisher.v1.impl;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.*;

import org.apache.cxf.jaxrs.ext.MessageContext;

import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantAPIPayloadResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatQueryDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantChatResponseDTO;
import org.wso2.carbon.apimgt.rest.api.publisher.v1.dto.DesignAssistantGenAPIPayloadDTO;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import java.io.InputStream;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DesignAssistantApiServiceImpl implements DesignAssistantApiService {
    public Response designAssistantApiPayloadGen(DesignAssistantGenAPIPayloadDTO designAssistantGenAPIPayloadDTO, MessageContext messageContext) {
        String sessionId = designAssistantGenAPIPayloadDTO.getSessionId();
        try {
            String generatedPayload = sendSessionIdToPythonBackend(sessionId);
            DesignAssistantAPIPayloadResponseDTO responseDTO = new DesignAssistantAPIPayloadResponseDTO();
            responseDTO.setGeneratedPayload(generatedPayload);

            return Response.ok(responseDTO).build();
        } catch (Exception e) {
            System.err.println("Failed to send Session Id to Python backend: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing request: " + e.getMessage())
                    .build();
        }
    }

    public String sendSessionIdToPythonBackend (String sessionId) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(new GeneratePayloadRequest(sessionId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8000/generate-api-payload"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    public Response designAssistantChat(DesignAssistantChatQueryDTO designAssistantChatQueryDTO, MessageContext messageContext) {
        String text = designAssistantChatQueryDTO.getText();
        String sessionId = designAssistantChatQueryDTO.getSessionId();
        try {
            String chatResponseJson = sendTextSessionIdToPythonBackend(text, sessionId);
            ObjectMapper objectMapper = new ObjectMapper();
            DesignAssistantChatResponseDTO responseDTO = objectMapper.readValue(chatResponseJson, DesignAssistantChatResponseDTO.class);

            return Response.ok(responseDTO).build();
        } catch (Exception e) {
            System.err.println("Failed to send Session Id to Python backend: " + e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .entity("Error processing request: " + e.getMessage())
                    .build();
        }
    }

    public String sendTextSessionIdToPythonBackend (String text, String sessionId) throws URISyntaxException, IOException, InterruptedException {
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();
        String jsonPayload = objectMapper.writeValueAsString(new ChatRequest(text, sessionId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("http://localhost:8000/chat"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        return response.body();
    }

    private static class GeneratePayloadRequest {
        public String sessionId;

        public GeneratePayloadRequest(String sessionId) {
            this.sessionId = sessionId;
        }
    }

    private static class ChatRequest {
        public String text;
        public String sessionId;

        public ChatRequest(String text, String sessionId) {
            this.text = text;
            this.sessionId = sessionId;
        }
    }
}
