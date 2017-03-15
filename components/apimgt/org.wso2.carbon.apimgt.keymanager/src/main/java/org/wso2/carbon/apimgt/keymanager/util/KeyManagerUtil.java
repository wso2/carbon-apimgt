/*
 * Copyright (c) 2017, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.wso2.carbon.apimgt.keymanager.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wso2.carbon.apimgt.keymanager.dto.OAuth2IntrospectionResponse;
import org.wso2.carbon.apimgt.keymanager.dto.OAuthApplication;
import org.wso2.carbon.apimgt.keymanager.dto.OAuthTokenResponse;
import org.wso2.carbon.apimgt.keymanager.exception.KeyManagerException;
import org.wso2.carbon.kernel.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * This class provides utility methods for key manager service.
 *
 */
public class KeyManagerUtil {

    private static final Logger log = LoggerFactory.getLogger(KeyManagerUtil.class);

    private static Map<String, String> userMap = new HashMap();
    private static Map<String , List<String>> userScopesMap = new HashMap<>();
    private static Map<String, OAuthTokenResponse> tokenMap = new HashMap<>();
    private static Map<String, OAuthTokenResponse> refreshTokenMap = new HashMap<>();

    /**
     * Extracts the clientId and secret info from the HTTP Authorization Header
     *
     * @param authorizationHeader "Basic " + base64encode(username + ":" + password)
     * @return String array with client id and client secret.
     */
    public static String[] extractCredentialsFromAuthzHeader(String authorizationHeader) throws KeyManagerException {
        String[] splitValues = authorizationHeader.trim().split(" ");
        if (splitValues.length == 2) {
            byte[] decodedBytes = Base64.getDecoder().decode(splitValues[1].trim());
            if (decodedBytes != null) {
                String userNamePassword = null;
                try {
                    userNamePassword = new String(decodedBytes, "UTF-8");
                    return userNamePassword.split(":");
                } catch (UnsupportedEncodingException e) {
                    log.error("Error while decoding authorization header", e);
                }

            }
        }
        String errMsg = "Error decoding authorization header. Space delimited \"<authMethod> <base64Hash>\" "
                + "format violated.";
        throw new KeyManagerException(errMsg);
    }

    /**
     * Generate access token.
     *
     *
     */
    public static boolean getLoginAccessToken(OAuthTokenResponse oAuthTokenResponse, String username, String password,
            Long validityPeriod) {
        if (userMap.containsKey(username) && password.equals(userMap.get(username))) {
            oAuthTokenResponse.setExpiresTimestamp(getExpiresTime(validityPeriod));
            oAuthTokenResponse.setExpiresIn(validityPeriod);
            oAuthTokenResponse.setToken(UUID.randomUUID().toString());
            oAuthTokenResponse.setRefreshToken(UUID.randomUUID().toString());
            oAuthTokenResponse.setScopes(userScopesMap.get(username));
            tokenMap.put(oAuthTokenResponse.getToken(), oAuthTokenResponse);
            refreshTokenMap.put(oAuthTokenResponse.getRefreshToken(), oAuthTokenResponse);
            return true;
        }
        return false;
    }

    public static boolean getRefreshedAccessToken (OAuthTokenResponse oAuthTokenResponse, String refreshToken, long
            validityPeriod) {
        if (refreshTokenMap.containsKey(refreshToken)) {
            oAuthTokenResponse.setExpiresTimestamp(getExpiresTime(validityPeriod));
            oAuthTokenResponse.setExpiresIn(validityPeriod);
            oAuthTokenResponse.setToken(UUID.randomUUID().toString());
            oAuthTokenResponse.setRefreshToken(UUID.randomUUID().toString());
            oAuthTokenResponse.setScopes(refreshTokenMap.get(refreshToken).getScopes());
            tokenMap.put(oAuthTokenResponse.getToken(), oAuthTokenResponse);
            refreshTokenMap.put(oAuthTokenResponse.getRefreshToken(), oAuthTokenResponse);
            return true;
        }
        return false;
    }


    public static boolean validateToken(String accessToken, OAuth2IntrospectionResponse oAuth2IntrospectionResponse) {
        if (tokenMap.containsKey(accessToken)) {
            long expireTime = tokenMap.get(accessToken).getExpiresTimestamp();
            if (new Timestamp(System.currentTimeMillis()).getTime() <= expireTime) {
                OAuthTokenResponse oAuthTokenResponse = tokenMap.get(accessToken);
                oAuth2IntrospectionResponse.setActive(true);
                StringBuilder builder = new StringBuilder();
                oAuthTokenResponse.getScopes().forEach(scope -> {
                            builder.append(scope);
                            builder.append(" ");

                        }
                    );
                oAuth2IntrospectionResponse.setScope(builder.toString());
                oAuth2IntrospectionResponse.setExp(oAuthTokenResponse.getExpiresIn());
                oAuth2IntrospectionResponse.setIat(System.currentTimeMillis());
                return true;
            }
        }
        return false;
    }

    public static boolean validateUser(String userName, String password) {
        if (userMap.containsKey(userName) && password.equals(userMap.get(userName))) {
            return true;
        }
        return false;
    }

    /**
     * Method to return expires time for access token.
     *
     *
     */
    public static long getExpiresTime(Long validityPeriod) {
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        return (timestamp.getTime() + validityPeriod * 1000);
    }

    public static void addUsersAndScopes() {
        List<String> adminScopes = new ArrayList<>();
        adminScopes.add("apim:api_view");
        adminScopes.add("apim:api_create");
        adminScopes.add("apim:api_publish");
        adminScopes.add("apim:tier_view");
        adminScopes.add("apim:tier_manage");
        adminScopes.add("apim:subscription_view");
        adminScopes.add("apim:subscription_block");
        adminScopes.add("apim:subscribe");
        List<String> subsciberScopes = new ArrayList<>();
        subsciberScopes.add("apim:subscribe");
        userMap.put("admin", "admin");
        userMap.put("subscriber", "subscriber");
        userMap.put("John", "John");
        userMap.put("Smith", "Smith");
        userScopesMap.put("admin", adminScopes);
        userScopesMap.put("subscriber", subsciberScopes);
        userScopesMap.put("John", adminScopes);
        userScopesMap.put("Smith", adminScopes);
    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DMI_NONSERIALIZABLE_OBJECT_WRITTEN")
    public static void backUpOauthData(Map<String, OAuthApplication> applications, Map<String, OAuthApplication>
            appsByClientId) {
        String backUpPath = Utils.getCarbonHome() + File.separator + "database" + File.separator;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(backUpPath + "applications.data");

            oos = new ObjectOutputStream(fos);
            oos.writeObject(applications);
            oos.close();
            fos = new FileOutputStream(backUpPath + "appsByClientId.data");

            oos = new ObjectOutputStream(fos);
            oos.writeObject(appsByClientId);
        } catch (FileNotFoundException e) {
            log.error("Error while backing up token data", e);
        } catch (IOException e) {
            log.error("Error while backing up token data", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }

        }

    }

    @edu.umd.cs.findbugs.annotations.SuppressFBWarnings("DMI_NONSERIALIZABLE_OBJECT_WRITTEN")
    public static void backUpTokenData() {
        String backUpPath = Utils.getCarbonHome() + File.separator + "database" + File.separator;
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(backUpPath + "token.data");

            oos = new ObjectOutputStream(fos);
            oos.writeObject(tokenMap);
            oos.close();
            fos = new FileOutputStream(backUpPath + "refresh.data");

            oos = new ObjectOutputStream(fos);
            oos.writeObject(refreshTokenMap);
            oos.close();
        } catch (FileNotFoundException e) {
            log.error("Error while backing up token data", e);
        } catch (IOException e) {
            log.error("Error while backing up token data", e);
        } finally {
            if (oos != null) {
                try {
                    oos.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }

        }

    }



    public static Map<String, OAuthApplication> getBackedUpData(String fileName) {
        String backUpPath = Utils.getCarbonHome() + File.separator + "database" + File.separator + fileName;
        File file = new File(backUpPath);
        if (!file.exists()) {
            return new HashMap<>();
        }
        ObjectInputStream ois = null;
        Map<String, OAuthApplication> applications = null;
        try {
            FileInputStream fis = new FileInputStream(backUpPath);
            ois = new ObjectInputStream(fis);
            applications = (Map) ois.readObject();
        } catch (FileNotFoundException e) {
            log.error("Error while getting backed up token data", e);
        } catch (IOException e) {
            log.error("Error while getting backed up token data", e);
        } catch (ClassNotFoundException e) {
            log.error("Error while getting backed up token data", e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }

        }
        return applications;
    }

    public static void getBackedUpTokenData() {
        getBackedUpTokenData("token.data");
        getBackedUpTokenData("refresh.data");
    }

    private static void getBackedUpTokenData(String fileName) {
        String backUpPath = Utils.getCarbonHome() + File.separator + "database" + File.separator + fileName;
        File file = new File(backUpPath);
        if (!file.exists()) {
            return;
        }
        ObjectInputStream ois = null;
        try {
            FileInputStream fis = new FileInputStream(backUpPath);
            ois = new ObjectInputStream(fis);
            if (fileName.contains("token")) {
                tokenMap = (Map) ois.readObject();
            } else {
                refreshTokenMap = (Map) ois.readObject();
            }
        } catch (FileNotFoundException e) {
            log.error("Error while getting backed up token data", e);
        } catch (IOException e) {
            log.error("Error while getting backed up token data", e);
        } catch (ClassNotFoundException e) {
            log.error("Error while getting backed up token data", e);
        } finally {
            if (ois != null) {
                try {
                    ois.close();
                } catch (IOException e) {
                    log.error("Error while closing the stream", e);
                }
            }

        }
    }
}
