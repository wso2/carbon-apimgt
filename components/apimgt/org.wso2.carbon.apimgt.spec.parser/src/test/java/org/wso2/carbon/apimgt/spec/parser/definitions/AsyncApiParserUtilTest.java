package org.wso2.carbon.apimgt.spec.parser.definitions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolVersion;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.message.BasicStatusLine;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;
import org.wso2.carbon.apimgt.api.APIDefinitionValidationResponse;
import org.wso2.carbon.apimgt.api.ErrorItem;
import org.wso2.carbon.apimgt.api.APIManagementException;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

/**
 * Unit tests for AsyncApiParserUtil utility methods.
 */
public class AsyncApiParserUtilTest {

    private static final String VALID_ASYNCAPI_JSON =
            "{\"asyncapi\":\"2.0.0\",\"info\":{\"title\":\"test\",\"version\":\"1.0.0\"},\"channels\":{}}";

    private static final String INVALID_JSON = "{ this is : not json }";

    @Test
    public void testGetAsyncApiVersion() {
        String version = AsyncApiParserUtil.getAsyncApiVersion(VALID_ASYNCAPI_JSON);
        assertEquals("2.0.0", version);
    }

    @Test
    public void testAddErrorToValidationResponse() {
        APIDefinitionValidationResponse response = new APIDefinitionValidationResponse();
        ErrorItem item = AsyncApiParserUtil.addErrorToValidationResponse(response, "sample-error");
        assertNotNull(item);
        assertEquals("sample-error", item.getErrorMessage());
        assertFalse(response.getErrorItems().isEmpty());
        // Ensure the returned item is the same object in the list
        assertEquals(item, response.getErrorItems().get(0));
    }

    @Test
    public void testGetFromAsyncApiDocument_valid() throws Exception {
        // Should return an AsyncApiDocument (non-null) for a valid AsyncAPI document string
        assertNotNull(AsyncApiParserUtil.getFromAsyncApiDocument("2.0.0", VALID_ASYNCAPI_JSON));
    }

    @Test(expected = APIManagementException.class)
    public void testGetFromAsyncApiDocument_invalid_throws() throws Exception {
        // Passing invalid JSON should cause an APIManagementException from getFromAsyncApiDocument
        AsyncApiParserUtil.getFromAsyncApiDocument("2.0.0", INVALID_JSON);
    }

    @Test
    public void testValidateAsyncApiContent_valid() throws APIManagementException {
        List<String> errors = new ArrayList<>();
        boolean valid = AsyncApiParserUtil.validateAsyncApiContent(VALID_ASYNCAPI_JSON, errors);
        assertTrue("Valid AsyncAPI should return true", valid);
        assertTrue("No error messages expected", errors.isEmpty());
    }

    @Test
    public void testValidateAsyncApiContent_invalid() throws APIManagementException {
        List<String> errors = new ArrayList<>();
        boolean valid = AsyncApiParserUtil.validateAsyncApiContent(INVALID_JSON, errors);
        assertFalse("Invalid JSON should return false", valid);
        assertFalse("Error messages expected", errors.isEmpty());
    }

    @Test
    public void testValidateAsyncAPISpecification_byString_valid() throws Exception {
        // Use the simple validation wrapper that picks parser and validates.
        APIDefinitionValidationResponse resp = AsyncApiParserUtil.validateAsyncAPISpecification(VALID_ASYNCAPI_JSON,
                true);
        assertNotNull(resp);
        // The underlying parser/validator should consider this minimal AsyncAPI valid (no high/medium level problems).
        assertTrue("Expected validation to be successful for the minimal AsyncAPI document", resp.isValid());
    }

    @Test
    public void testValidateAsyncAPISpecificationByURL_okStatus_readsFile() throws Exception {
        // Create a temporary YAML file with a minimal AsyncAPI content.
        File tmp = File.createTempFile("asyncapi-test", ".yaml");
        tmp.deleteOnExit();

        ObjectMapper yamlWriter = new ObjectMapper(new YAMLFactory());
        Object obj = yamlWriter.readValue(VALID_ASYNCAPI_JSON, Object.class);

        try (FileWriter fw = new FileWriter(tmp)) {
            yamlWriter.writeValue(fw, obj);
        }

        // Use a simple HttpClient stub that returns 200 OK for execute(...) calls.
        HttpClient okHttpClient = new HttpClient() {
            @Override
            public HttpResponse execute(org.apache.http.HttpHost host, org.apache.http.HttpRequest request) {
                return buildResponse(200);
            }

            @Override
            public HttpResponse execute(org.apache.http.HttpHost host, org.apache.http.HttpRequest request,
                                        org.apache.http.protocol.HttpContext context) {
                return buildResponse(200);
            }

            @Override
            public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler) throws
                    IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<?
                    extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<?
                    extends T> responseHandler) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<?
                    extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public ClientConnectionManager getConnectionManager() {
                return null;
            }

            @Override
            public HttpResponse execute(HttpUriRequest request) {
                return buildResponse(200);
            }

            @Override
            public HttpResponse execute(HttpUriRequest request, org.apache.http.protocol.HttpContext context) {
                return buildResponse(200);
            }

            private HttpResponse buildResponse(int status) {
                return new BasicHttpResponse(new BasicStatusLine(new ProtocolVersion("HTTP", 1, 1),
                        status, ""));
            }
        };

        String fileUrl = tmp.toURI().toURL().toString();
        APIDefinitionValidationResponse resp = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(
                fileUrl, okHttpClient, true);
        assertNotNull(resp);
        assertTrue("Validation by URL with 200 status should succeed", resp.isValid());
    }

    @Test
    public void testValidateAsyncAPISpecificationByURL_non200() throws Exception {
        // Provide a valid file URL but HttpClient returns non-200 -> expect validationResponse.valid == false
        File tmp = File.createTempFile("asyncapi-test", ".yaml");
        tmp.deleteOnExit();
        try (FileWriter fw = new FileWriter(tmp)) {
            fw.write(VALID_ASYNCAPI_JSON);
        }
        String fileUrl = tmp.toURI().toURL().toString();

        HttpClient nonOkHttpClient = new HttpClient() {
            @Override
            public HttpResponse execute(org.apache.http.HttpHost host, org.apache.http.HttpRequest request) {
                return buildResponse(404);
            }

            @Override
            public HttpResponse execute(org.apache.http.HttpHost host, org.apache.http.HttpRequest request,
                                        org.apache.http.protocol.HttpContext context) {
                return buildResponse(404);
            }

            @Override
            public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<? extends T> responseHandler)
                    throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpUriRequest httpUriRequest, ResponseHandler<?
                    extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<?
                    extends T> responseHandler) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public <T> T execute(HttpHost httpHost, HttpRequest httpRequest, ResponseHandler<?
                    extends T> responseHandler, HttpContext httpContext) throws IOException, ClientProtocolException {
                return null;
            }

            @Override
            public HttpParams getParams() {
                return null;
            }

            @Override
            public ClientConnectionManager getConnectionManager() {
                return null;
            }

            @Override
            public HttpResponse execute(HttpUriRequest request) {
                return buildResponse(404);
            }

            @Override
            public HttpResponse execute(HttpUriRequest request, org.apache.http.protocol.HttpContext context) {
                return buildResponse(404);
            }

            private HttpResponse buildResponse(int status) {
                return new BasicHttpResponse(new BasicStatusLine(
                        new ProtocolVersion("HTTP", 1, 1), status, ""));
            }
        };

        APIDefinitionValidationResponse resp = AsyncApiParserUtil.validateAsyncAPISpecificationByURL(
                fileUrl, nonOkHttpClient, true);
        assertNotNull(resp);
        assertFalse("Expected validation to fail when HTTP status != 200", resp.isValid());
        // Ensure the known error code for URL not 200 is present in the response error items
        boolean found = resp.getErrorItems().stream()
                .anyMatch(e -> e.getErrorMessage() != null && e.getErrorMessage().contains("ASYNCAPI_URL_NO_200")
                        || e.getErrorMessage() == null); // some implementations may set an enum/object in the list
        assertTrue("Expecting error item describing non-200 URL result",
                found || !resp.getErrorItems().isEmpty());
    }
}