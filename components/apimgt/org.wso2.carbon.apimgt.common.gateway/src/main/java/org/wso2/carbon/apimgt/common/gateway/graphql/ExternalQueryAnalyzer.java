package org.wso2.carbon.apimgt.common.gateway.graphql;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.wso2.carbon.apimgt.common.gateway.dto.ExternalQueryAnalyzerResponseDTO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;

/**
 * External Query Analyzer for analyzing GraphQL queries for potential vulnerabilities.
 * This class communicates with an external service to detect threats in GraphQL queries.
 */
public class ExternalQueryAnalyzer {
    private static final Log log = LogFactory.getLog(ExternalQueryAnalyzer.class);

    public ExternalQueryAnalyzerResponseDTO analyseQuery(String query) {
        if (log.isDebugEnabled()) {
            log.debug("Analyzing query with external service");
        }

        ExternalQueryAnalyzerResponseDTO externalQueryAnalyzerResponseDTO = new ExternalQueryAnalyzerResponseDTO();

        try {
            HttpURLConnection connection = getHttpURLConnection(query);

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                try (BufferedReader br = new BufferedReader(
                        new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8))) {
                    StringBuilder response = new StringBuilder();
                    String responseLine;
                    while ((responseLine = br.readLine()) != null) {
                        response.append(responseLine.trim());
                    }

                    JSONObject fullResponse = new JSONObject(response.toString());
                    JSONObject results = fullResponse.getJSONObject("results");

                    boolean anyVulDetected = false;

                    Iterator<String> keys = results.keys();
                    while (keys.hasNext()) {
                        String key = keys.next();
                        JSONObject threatObject = results.getJSONObject(key);

                        if (threatObject.has("detected") && threatObject.getBoolean("detected")) {
                            externalQueryAnalyzerResponseDTO.addVulToList(threatObject.getString("threat"));
                            anyVulDetected = true;
                        }
                    }
                    externalQueryAnalyzerResponseDTO.setVulnerable(anyVulDetected);

                    if (log.isDebugEnabled()) {
                        log.debug("External service response: " + response);
                    }
                }
            } else {
                log.error("External service returned error code: " + responseCode);
            }
            connection.disconnect();

        } catch (IOException e) {
            log.error("Error while calling external query analyzer service", e);
        }

        return externalQueryAnalyzerResponseDTO;
    }

    private static HttpURLConnection getHttpURLConnection(String query) throws IOException {
        URL url = new URL("http://127.0.0.1:8000/detect");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setDoOutput(true);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("query", query);
        String jsonPayload = jsonObject.toString();

        try (OutputStream os = connection.getOutputStream()) {
            byte[] input = jsonPayload.getBytes(StandardCharsets.UTF_8);
            os.write(input, 0, input.length);
        }

        return connection;
    }
}
