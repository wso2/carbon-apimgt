/*
 *  Copyright (c) 2005-2011, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
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

package org.wso2.carbon.apimgt.internal.service.utils;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.wso2.carbon.apimgt.impl.APIConstants;
import org.wso2.carbon.apimgt.impl.utils.APIMgtDBUtil;
import org.wso2.carbon.apimgt.internal.service.dto.BlockConditionsDTO;
import org.wso2.carbon.apimgt.internal.service.dto.IPLevelDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTDTO;
import org.wso2.carbon.apimgt.internal.service.dto.RevokedJWTListDTO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Database utility to retrieve allow list,keyTemplates and Revoked Tokens.
 */
public final class BlockConditionDBUtil {

    private static final Log log = LogFactory.getLog(BlockConditionDBUtil.class);


    private static final String GET_GLOBAL_POLICY_KEY_TEMPLATES = " SELECT KEY_TEMPLATE FROM AM_POLICY_GLOBAL";

    private BlockConditionDBUtil() {
    }

    public static BlockConditionsDTO getBlockConditions() {

        List<String> api = new ArrayList<>();
        List<String> application = new ArrayList<>();
        List<IPLevelDTO> ip = new ArrayList<>();
        List<String> user = new ArrayList<>();
        List<String> custom = new ArrayList<>();
        String sqlQuery = "select * from AM_BLOCK_CONDITIONS";
        List<String> subscription = new ArrayList<>();
        try (Connection conn = APIMgtDBUtil.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String type = rs.getString("TYPE");
                        String value = rs.getString("BLOCK_CONDITION");
                        String enabled = rs.getString("ENABLED");
                        String tenantDomain = rs.getString("DOMAIN");
                        int conditionId = rs.getInt("CONDITION_ID");
                        if (Boolean.parseBoolean(enabled)) {
                            switch (type) {
                                case APIConstants.BLOCKING_CONDITIONS_API:
                                    api.add(value);
                                    break;
                                case APIConstants.BLOCKING_CONDITIONS_APPLICATION:
                                    application.add(value);
                                    break;
                                case APIConstants.BLOCKING_CONDITIONS_IP:
                                case APIConstants.
                                        BLOCK_CONDITION_IP_RANGE:
                                    IPLevelDTO ipLevelDTO = new IPLevelDTO();
                                    ipLevelDTO.setTenantDomain(tenantDomain);
                                    ipLevelDTO.setId(conditionId);
                                    JsonElement ipLevelJson = new JsonParser().parse(value);
                                    if (ipLevelJson instanceof JsonPrimitive) {
                                        JsonPrimitive fixedIp = (JsonPrimitive) ipLevelJson;
                                        ipLevelDTO.setFixedIp(fixedIp.getAsString());
                                        ipLevelDTO.setInvert(Boolean.FALSE);
                                        ipLevelDTO.setType(APIConstants.BLOCKING_CONDITIONS_IP);
                                    } else if (ipLevelJson instanceof JsonObject) {
                                        JsonObject ipBlockingJson = (JsonObject) ipLevelJson;
                                        if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_FIXED_IP)) {
                                            ipLevelDTO.setType(APIConstants.BLOCKING_CONDITIONS_IP);
                                            ipLevelDTO.setFixedIp(
                                                    ipBlockingJson.get(APIConstants.BLOCK_CONDITION_FIXED_IP).getAsString());
                                        }
                                        if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_START_IP)) {
                                            ipLevelDTO.setType(APIConstants.BLOCK_CONDITION_IP_RANGE);
                                            ipLevelDTO.setStartingIp(
                                                    ipBlockingJson.get(APIConstants.BLOCK_CONDITION_START_IP).getAsString());
                                        }
                                        if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_ENDING_IP)) {
                                            ipLevelDTO.setEndingIp(
                                                    ipBlockingJson.get(APIConstants.BLOCK_CONDITION_ENDING_IP).getAsString());
                                        }
                                        if (ipBlockingJson.has(APIConstants.BLOCK_CONDITION_INVERT)) {
                                            ipLevelDTO.setInvert(
                                                    ipBlockingJson.get(APIConstants.BLOCK_CONDITION_INVERT).getAsBoolean());
                                        }
                                    }
                                    ip.add(ipLevelDTO);
                                    break;
                                case APIConstants.BLOCKING_CONDITIONS_USER:
                                    user.add(value);
                                    break;
                                case "CUSTOM":
                                    custom.add(value);
                                    break;
                                case APIConstants.BLOCKING_CONDITIONS_SUBSCRIPTION:
                                    subscription.add(value);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        }
        BlockConditionsDTO blockConditionsDTO = new BlockConditionsDTO();
        blockConditionsDTO.setApi(api);
        blockConditionsDTO.setApplication(application);
        blockConditionsDTO.setIp(ip);
        blockConditionsDTO.setUser(user);
        blockConditionsDTO.setCustom(custom);
        blockConditionsDTO.setSubscription(subscription);
        return blockConditionsDTO;
    }

    public static BlockConditionsDTO getBlockConditionsDTO() {

        return getBlockConditions();

    }

    public static Set<String> getKeyTemplates() {

        return getGlobalPolicyKeyTemplates();

    }

    /**
     * Retrieves global policy key templates for the given tenantID
     *
     * @return list of KeyTemplates
     */
    public static Set<String> getGlobalPolicyKeyTemplates() {

        Set<String> keyTemplates = new HashSet<>();

        String sqlQuery = GET_GLOBAL_POLICY_KEY_TEMPLATES;
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    keyTemplates.add(rs.getString("KEY_TEMPLATE"));
                }
            }
        } catch (SQLException e) {
            log.error("Error while executing SQL", e);
        }
        return keyTemplates;
    }

    /**
     * Fetches all revoked JWTs from DB.
     *
     * @return list fo revoked JWTs
     */
    public static RevokedJWTListDTO getRevokedJWTs() {

        RevokedJWTListDTO revokedJWTListDTO = new RevokedJWTListDTO();
        String sqlQuery = "SELECT SIGNATURE,EXPIRY_TIMESTAMP FROM AM_REVOKED_JWT";
        try (Connection conn = APIMgtDBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sqlQuery);) {
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String signature = rs.getString("SIGNATURE");
                    Long expiryTimestamp = rs.getLong("EXPIRY_TIMESTAMP");
                    RevokedJWTDTO revokedJWTDTO = new RevokedJWTDTO();
                    revokedJWTDTO.setJwtSignature(signature);
                    revokedJWTDTO.setExpiryTime(expiryTimestamp);
                    revokedJWTListDTO.add(revokedJWTDTO);
                }
            }
        } catch (SQLException e) {
            log.error("Error while fetching revoked JWTs from database. ", e);
        }
        return revokedJWTListDTO;
    }
}
