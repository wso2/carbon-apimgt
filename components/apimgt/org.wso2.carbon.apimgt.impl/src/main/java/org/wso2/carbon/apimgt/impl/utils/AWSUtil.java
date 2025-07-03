/*
 * Copyright (c) 2025, WSO2 LLC. (http://www.wso2.com).
 *
 * WSO2 LLC. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.impl.utils;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.util.EntityUtils;
import org.wso2.carbon.apimgt.api.APIManagementException;
import org.wso2.carbon.apimgt.impl.APIConstants;

/**
 * Utility class for generating AWS Signature Version 4 headers.
 * This class provides methods to create the necessary headers for authenticating requests to AWS services.
 */
public class AWSUtil {


    /**
     * Generates the AWS Signature Version 4 headers for authenticating requests.
     * This method constructs the signature based on the provided request parameters and AWS credentials.
     *
     * @param host         The hostname of the API endpoint.
     * @param method       The HTTP method of the request (e.g., "POST").
     * @param service      The AWS service identifier (e.g., "bedrock").
     * @param uri          The URI path of the API endpoint.
     * @param queryString  The query string parameters of the request (can be null or empty).
     * @param payload      The request payload as a String (can be null or empty for GET requests).
     * @param accessKey    The AWS access key ID.
     * @param secretKey    The AWS secret access key.
     * @param region       The region of the AWS service.
     * @param sessionToken The AWS session token, if using temporary credentials (can be null or empty).
     * @return A {@code Map<String, String>} AWS S4 Auth headers.
     * @throws APIManagementException If an error occurs during the signature generation process, such as
     *                                problems with hashing or HMAC calculation.
     */
    public static Map<String, String> generateAWSSignature(String host, String method, String service, String uri,
                                                           String queryString, String payload, String accessKey,
                                                           String secretKey, String region, String sessionToken,
                                                           Map<String, String> incomingHeaders)
            throws APIManagementException {

        if (StringUtils.isBlank(accessKey) || StringUtils.isBlank(secretKey) || StringUtils.isBlank(region)) {
            throw new APIManagementException("Missing required fields: 'accessKey', 'secretKey', 'region'");
        }

        try {
            // Step 1: Create date stamps
            ZonedDateTime now = ZonedDateTime.now(ZoneOffset.UTC);
            String amzDate = DateTimeFormatter.ofPattern(APIConstants.AWSConstants.AMZ_DATE_FORMAT).format(now);
            String dateStamp = DateTimeFormatter.ofPattern(APIConstants.AWSConstants.DATE_FORMAT).format(now);

            // Step 2: Create canonical headers
            Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
            headers.putAll(incomingHeaders);
            headers.put(APIConstants.AWSConstants.HOST_HEADER, host);
            headers.put(APIConstants.AWSConstants.AMZ_DATE_HEADER, amzDate);
            if (sessionToken != null && !sessionToken.isEmpty()) {
                headers.put(APIConstants.AWSConstants.AMZ_SECURITY_TOKEN_HEADER, sessionToken);
            }

            String payloadHash = payload != null ? getSha256Digest(payload) : getSha256Digest("");
            if (payload != null && !payload.isEmpty()) {
                // Add content-type and x-amz-content-sha25 for POST request with JSON payload
                headers.put(APIConstants.HEADER_CONTENT_TYPE, APIConstants.APPLICATION_JSON_MEDIA_TYPE);
                headers.put(APIConstants.AWSConstants.AMZ_CONTENT_SHA_HEADER, payloadHash);
            }

            // Build canonical headers string and signed headers list
            StringBuilder canonicalHeaders = new StringBuilder();
            StringBuilder signedHeaders = new StringBuilder();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                canonicalHeaders.append(entry.getKey().toLowerCase()).append(":").append(entry.getValue()).append("\n");
                signedHeaders.append(entry.getKey().toLowerCase()).append(";");
            }
            // Remove trailing semicolon
            if (signedHeaders.length() > 0) {
                signedHeaders.setLength(signedHeaders.length() - 1);
            }

            // Step 3: Create canonical request
            // For STS GET requests, we need to sort and encode query parameters
            String canonicalQueryString = "";
            if (queryString != null && !queryString.isEmpty()) {
                canonicalQueryString = createCanonicalQueryString(queryString);
            }

            String canonicalRequest =
                    method + "\n" + uri + "\n" + canonicalQueryString + "\n" + canonicalHeaders + "\n" + signedHeaders +
                            "\n" + payloadHash;

            // Step 4: Create string to sign
            String algorithm = APIConstants.AWSConstants.AWS4_ALGORITHM;
            // String region = "ap-southeast-2";
            String credentialScope =
                    dateStamp + "/" + region + "/" + service + "/" + APIConstants.AWSConstants.AWS4_REQUEST;
            String stringToSign =
                    algorithm + "\n" + amzDate + "\n" + credentialScope + "\n" + getSha256Digest(canonicalRequest);

            // Step 5: Calculate signature
            byte[] signingKey = getSignatureKey(secretKey, dateStamp, region, service);
            String signature = hexFromBytes(hmacSHA256(stringToSign, signingKey));

            // Step 6: Create authorization header
            String authorizationHeader =
                    algorithm + " " + APIConstants.AWSConstants.AWS4_CREDENTIAL + "=" + accessKey + "/" +
                            credentialScope + ", " + APIConstants.AWSConstants.AWS4_SIGNED_HEADERS + "=" +
                            signedHeaders + ", " + APIConstants.AWSConstants.AWS4_SIGNATURE + "=" + signature;

            // Create result map with all required headers
            Map<String, String> authHeaders = new HashMap<>(headers);
            authHeaders.put(APIConstants.AUTHORIZATION_HEADER_DEFAULT, authorizationHeader);

            return authHeaders;
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new APIManagementException("Error generating AWS Signature", e);
        }
    }

    /**
     * Generates AWS Signature Version 4 headers using AssumeRole.
     * @param host The hostname of the API endpoint.
     * @param method The HTTP method of the request (e.g., "POST").
     * @param service The AWS service identifier (e.g., "bedrock").
     * @param uri The URI path of the API endpoint.
     * @param queryString The query string parameters of the request (can be null or empty).
     * @param payload The request payload as a String (can be null or empty for GET requests).
     * @param accessKey The AWS access key ID.
     * @param secretKey The AWS secret access key.
     * @param region The region of the AWS service.
     * @param sessionToken The AWS session token, if using temporary credentials (can be null or empty).
     * @param roleArn The ARN of the IAM role to assume.
     * @param roleRegion The region of the IAM role (can be null or empty, defaults to AWS default region).
     * @param roleExternalId The external ID for the IAM role (can be null or empty).
     * @param incomingHeaders The incoming headers from the request, which will be used to build the signature.
     * @return A {@code Map<String, String>} containing the AWS Signature Version 4 headers.
     * @throws APIManagementException If an error occurs during the signature generation process, such as
     */

    public static Map<String, String> generateAWSSignatureUsingAssumeRole(String host, String method, String service,
                                                                          String uri, String queryString,
                                                                          String payload, String accessKey,
                                                                          String secretKey, String region,
                                                                          String sessionToken, String roleArn,
                                                                          String roleRegion, String roleExternalId,
                                                                          Map<String, String> incomingHeaders)
            throws APIManagementException {

        try {
            // Set up STS endpoint and parameters
            String stsRegion =
                    roleRegion != null && !roleRegion.isEmpty() ? roleRegion : APIConstants.AWSConstants.DEFAULT_REGION;
            String stsHost =
                    APIConstants.AWSConstants.STS + "." + stsRegion + "." + APIConstants.AWSConstants.AWS_DOMAIN;
            String stsMethod = APIConstants.HTTP_GET;
            String stsUri = "/";

            // Create session name based on current date
            LocalDate now = LocalDate.now();
            String sessionName =
                    String.format(APIConstants.AWSConstants.SESSION_FORMAT, now.getYear(), now.getMonthValue(),
                            now.getDayOfMonth());

            // Build query string for AssumeRole
            StringBuilder stsQueryBuilder = new StringBuilder();
            stsQueryBuilder.append(APIConstants.AWSConstants.ASSUME_ROLE_QUERY_ACTION);
            stsQueryBuilder.append(APIConstants.AWSConstants.ASSUME_ROLE_QUERY_VERSION);
            stsQueryBuilder.append(APIConstants.AWSConstants.ASSUME_ROLE_QUERY_ROLE_ARN_KEY)
                    .append(URLEncoder.encode(roleArn, StandardCharsets.UTF_8.name()));
            stsQueryBuilder.append(APIConstants.AWSConstants.ASSUME_ROLE_QUERY_ROLE_SESSION_KEY)
                    .append(URLEncoder.encode(sessionName, StandardCharsets.UTF_8.name()));
            if (roleExternalId != null && !roleExternalId.isEmpty()) {
                stsQueryBuilder.append(APIConstants.AWSConstants.ASSUME_ROLE_QUERY_ROLE_EXTERNAL_ID_KEY)
                        .append(URLEncoder.encode(roleExternalId, StandardCharsets.UTF_8.name()));
            }
            String stsQueryString = stsQueryBuilder.toString();

            // Generate signature for STS call
            Map<String, String> stsHeaders =
                    generateAWSSignature(stsHost, stsMethod, APIConstants.AWSConstants.STS, stsUri, stsQueryString,
                            "", accessKey, secretKey, stsRegion, sessionToken, incomingHeaders);

            // Call STS API to assume role
            String url = APIConstants.HTTPS_PROTOCOL_URL_PREFIX + stsHost + stsUri + "?" + stsQueryString;
            HttpClient httpClient = APIUtil.getHttpClient(url);
            HttpGet get = new HttpGet(url);
            // Add headers to the request
            for (Map.Entry<String, String> header : stsHeaders.entrySet()) {
                get.setHeader(header.getKey(), header.getValue());
            }

            try (CloseableHttpResponse response = APIUtil.executeHTTPRequestWithRetries(get, httpClient)) {
                int statusCode = response.getStatusLine().getStatusCode();
                String responseBody = EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8);

                if (statusCode != HttpStatus.SC_OK) {
                    throw new APIManagementException("Failed to assume role: " + statusCode + ": " + responseBody);
                }

                // Parse the XML response
                String tempAccessKey = extractXmlValue(responseBody, APIConstants.AWSConstants.ASSUME_ROLE_ACCESS_KEY);
                String tempSecretKey = extractXmlValue(responseBody, APIConstants.AWSConstants.ASSUME_ROLE_SECRET_KEY);
                String tempSessionToken =
                        extractXmlValue(responseBody, APIConstants.AWSConstants.ASSUME_ROLE_SESSION_TOKEN);

                if (tempAccessKey == null || tempSecretKey == null || tempSessionToken == null) {
                    throw new APIManagementException("Failed to extract credentials from AssumeRole response");
                }

                // Use temporary credentials to generate the final signature
                return generateAWSSignature(host, method, service, uri, queryString, payload, tempAccessKey,
                        tempSecretKey, region, tempSessionToken, incomingHeaders);
            }
        } catch (IOException e) {
            throw new APIManagementException("Error generating AWS Signature with assume role", e);
        }
    }

    /**
     * Sorts the given URL-encoded query string parameters into canonical form.
     *
     * <p><b>Note:</b> This method assumes that the input query string is already URL-encoded.
     * It does not perform URL encoding on parameter names or values.</p>
     *
     * @param encodedQueryString The pre-encoded query string
     * @return Canonical query string sorted by parameter names
     */
    private static String createCanonicalQueryString(String encodedQueryString) {
        return Arrays.stream(encodedQueryString.split("&"))
                .sorted(Comparator.comparing(param -> param.split("=", 2)[0])).collect(Collectors.joining("&"));
    }

    // Helper method to extract values from XML
    private static String extractXmlValue(String xml, String tagName) {
        Pattern pattern = Pattern.compile("<" + tagName + ">(.*?)</" + tagName + ">");
        Matcher matcher = pattern.matcher(xml);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    // Helper method for SHA-256 hashing
    private static String getSha256Digest(String input) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance(APIConstants.SHA_256);
        byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
        return hexFromBytes(digest);
    }

    // Helper method to convert bytes to hex
    private static String hexFromBytes(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }

    // Helper method to get signature key
    public static byte[] getSignatureKey(String key, String dateStamp, String regionName, String serviceName)
            throws NoSuchAlgorithmException, InvalidKeyException {
        byte[] kSecret = (APIConstants.AWSConstants.AWS4 + key).getBytes(StandardCharsets.UTF_8);
        byte[] kDate = hmacSHA256(dateStamp, kSecret);
        byte[] kRegion = hmacSHA256(regionName, kDate);
        byte[] kService = hmacSHA256(serviceName, kRegion);
        return hmacSHA256(APIConstants.AWSConstants.AWS4_REQUEST, kService);
    }

    // Helper method for HMAC-SHA256
    public static byte[] hmacSHA256(String data, byte[] key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(APIConstants.AWSConstants.HMAC_SHA_256);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, APIConstants.AWSConstants.HMAC_SHA_256);
        mac.init(secretKeySpec);
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

}
